package blade;

import blade.event.BotLifecycleEvents;
import blade.inventory.BotClientInventory;
import blade.inventory.BotInventory;
import blade.platform.ClientPlatform;
import blade.platform.Platform;
import blade.scheduler.BotScheduler;
import blade.util.ClientSimulator;
import blade.util.fake.FakePlayer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.util.Random;

@SuppressWarnings("UnusedReturnValue")
public class Bot {
    protected final Player vanillaPlayer;
    protected final Platform platform;
    protected final BotScheduler scheduler;
    protected final Random random = new Random();
    protected final BotInventory inventory;
    protected final ClientSimulator clientSimulator;
    public final boolean isClient;
    protected final BladeMachine blade = new BladeMachine(this);
    protected boolean jumped = false;
    protected boolean debug = false;
    protected float prevYaw;
    protected float prevPitch;

    public Bot(Player vanillaPlayer, Platform platform) {
        this.isClient = platform.isClient();
        this.vanillaPlayer = vanillaPlayer;
        this.platform = platform;
        this.scheduler = new BotScheduler(platform.getExecutor());
        this.inventory = isClient ? new BotClientInventory(this) : new BotInventory(this);
        if (isClient) clientSimulator = null;
        else clientSimulator = new ClientSimulator((ServerPlayer) vanillaPlayer, inventory::hasInventoryOpen);
    }

    public Player getVanillaPlayer() {
        return vanillaPlayer;
    }

    public BotInventory getInventory() {
        return inventory;
    }

    public BotScheduler getScheduler() {
        return scheduler;
    }

    public void doTick() {
        if (isDestroyed()) {
            destroy();
            return;
        }
        BotLifecycleEvents.TICK_START.call(this).onTickStart(this);
        try {
            tick();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        BotLifecycleEvents.TICK_END.call(this).onTickEnd(this);
    }

    protected void tick() {
        if (isClient) {
            prevYaw = vanillaPlayer.getYRot();
            prevPitch = vanillaPlayer.getXRot();
        }

        if (jumped) {
            if (isClient) {
                Minecraft.getInstance().options.keyJump.setDown(false);
            } else {
                vanillaPlayer.setJumping(false);
            }
        }
        scheduler.tick(this);
        blade.tick();
        if (clientSimulator != null) clientSimulator.tick();
    }

    public Random getRandom() {
        return random;
    }

    public BladeMachine getBlade() {
        return blade;
    }

    public void lookRealistic(float targetYaw, float targetPitch, float time, float randomness) {
        float yaw = vanillaPlayer.getYRot();
        float pitch = vanillaPlayer.getXRot();
        float t = time * time;
        setYaw(targetYaw * t + yaw * (1 - t) + random.nextFloat() * 6f * randomness);
        setPitch(targetPitch * t + pitch * (1 - t) + random.nextFloat() * 6f * randomness);
    }

    public void jump() {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyJump.setDown(true);
        } else {
            vanillaPlayer.setJumping(true);
        }
        jumped = true;
    }

    public void attack() {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            InputConstants.Key key = ((ClientPlatform) platform).getKey(Minecraft.getInstance().options.keyAttack);
            KeyMapping.click(key);
            return;
        }
        clientSimulator.attack();
    }

    public void interact() {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            InputConstants.Key key = ((ClientPlatform) platform).getKey(Minecraft.getInstance().options.keyAttack);
            KeyMapping.click(key);
            return;
        }
        clientSimulator.use();
    }

    public void attack(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyAttack.setDown(should);
            return;
        }
        clientSimulator.setAttack(should);
    }

    public void interact(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyUse.setDown(should);
            return;
        }
        clientSimulator.setUse(should);
    }

    public void setSneak(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyShift.setDown(should);
            return;
        }
        vanillaPlayer.setShiftKeyDown(should);
    }

    public void setMoveLeft(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyLeft.setDown(should);
            return;
        }
        clientSimulator.setLeft(should);
    }

    public void setMoveRight(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyRight.setDown(should);
            return;
        }
        clientSimulator.setRight(should);
    }

    public void setMoveForward(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyUp.setDown(should);
            return;
        }
        clientSimulator.setForward(should);
    }

    public void setMoveBackward(boolean should) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keyDown.setDown(should);
            return;
        }
        clientSimulator.setBack(should);
    }

    public void setYaw(float yaw) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            double f = Minecraft.getInstance().options.sensitivity().get() * 0.6 + 0.6;
            double gcd = f * f * f * 8.0 * 0.15F;
            float deltaYaw = yaw - prevYaw;
            deltaYaw = ((int) (deltaYaw / gcd)) * (float) gcd;
            yaw = prevYaw + deltaYaw;
            prevYaw = yaw;
        }
        vanillaPlayer.setYRot(yaw);
    }

    public void setPitch(float pitch) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            double f = Minecraft.getInstance().options.sensitivity().get() * 0.6 + 0.6;
            double gcd = f * f * f * 8.0 * 0.15F;
            float deltaPitch = pitch - prevPitch;
            deltaPitch = ((int) (deltaPitch / gcd)) * (float) gcd;
            pitch = prevPitch + deltaPitch;
            prevPitch = pitch;
        }
        vanillaPlayer.setXRot(pitch);
    }

    public void setSprint(boolean sprint) {
        if (inventory.hasInventoryOpen()) return;
        if (isClient) {
            Minecraft.getInstance().options.keySprint.setDown(sprint);
            return;
        }
        vanillaPlayer.setSprinting(sprint);
    }

    public boolean isOnGround() {
        return vanillaPlayer.onGround();
    }

    public void setVelocity(Vec3 velocity) {
        vanillaPlayer.setDeltaMovement(velocity);
    }

    public boolean isDead() {
        return vanillaPlayer.isDeadOrDying();
    }

    public boolean isValid() {
        return !isDestroyed();
    }

    public boolean isDestroyed() {
        return !isClient && vanillaPlayer.isDeadOrDying();
    }

    public void destroy() {
        platform.destroyBot(this);
        if (isClient) {
            return;
        }
        if (vanillaPlayer instanceof FakePlayer) vanillaPlayer.discard();
    }

    public Platform getPlatform() {
        return platform;
    }

    public HitResult getCrossHairTarget() {
        if (isClient) {
            return Minecraft.getInstance().hitResult;
        }
        return clientSimulator.getCrossHairTarget();
    }

    public Logger getLogger(String name) {
        if (!debug) return NOPLogger.NOP_LOGGER;
        return LoggerFactory.getLogger("BOT-" + name);
    }
}
