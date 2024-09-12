package blade;

import blade.inventory.BotClientInventory;
import blade.inventory.BotInventory;
import blade.platform.ClientPlatform;
import blade.platform.Platform;
import blade.scheduler.BotScheduler;
import blade.utils.ClientSimulator;
import blade.utils.RotationManager;
import blade.utils.blade.BladeAction;
import blade.utils.fake.FakePlayer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.util.Random;

@SuppressWarnings("UnusedReturnValue")
public class Bot {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    protected final Player vanillaPlayer;
    protected final Platform platform;
    protected final BotScheduler scheduler;
    protected final Random random = new Random();
    protected final BotInventory inventory;
    protected final ClientSimulator clientSimulator;
    protected final RotationManager rotationManager = new RotationManager();
    public final boolean isClient;
    protected final BladeMachine blade = new BladeMachine(this);
    protected boolean jumped = false;
    protected boolean debug = false;

    public Bot(Player vanillaPlayer, Platform platform) {
        this.isClient = platform.isClient();
        this.vanillaPlayer = vanillaPlayer;
        this.platform = platform;
        this.scheduler = new BotScheduler(this, platform.getExecutor());
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
        try {
            tick();
        } catch (Throwable t) {
            LOGGER.warn("Failed ticking bot", t);
        }
    }

    protected void tick() {
        if (jumped) {
            if (isClient) {
                Minecraft.getInstance().options.keyJump.setDown(false);
            } else {
                vanillaPlayer.setJumping(false);
            }
        }
        scheduler.tick();
        blade.tick();
        updateRotation();
        if (clientSimulator != null) clientSimulator.tick();
    }

    public void updateRotation() {
        rotationManager.update(this);
    }

    public Random getRandom() {
        return random;
    }

    public BladeMachine getBlade() {
        return blade;
    }

    public void setRotationTarget(float targetYaw, float targetPitch, float speed) {
        rotationManager.setTarget(vanillaPlayer.getYRot(), vanillaPlayer.getXRot(), targetYaw, targetPitch, speed);
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
            InputConstants.Key key = ((ClientPlatform) platform).getKey(Minecraft.getInstance().options.keyUse);
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
        vanillaPlayer.setYRot(yaw);
    }

    public void setPitch(float pitch) {
        if (inventory.hasInventoryOpen()) return;
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

    public boolean isDead() {
        return vanillaPlayer.isDeadOrDying();
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

    public Logger createLogger(String name) {
        if (!debug) return NOPLogger.NOP_LOGGER;
        return LoggerFactory.getLogger("BOT-" + name);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        for (BladeAction action : blade.getActions()) {
            action.logger = createLogger(action.getClass().getSimpleName());
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void copy(Bot otherBot) {
        blade.copy(otherBot.getBlade());
        debug = otherBot.debug;
    }
}
