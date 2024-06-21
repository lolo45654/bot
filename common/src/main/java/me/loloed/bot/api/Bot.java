package me.loloed.bot.api;

import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.event.BotLifecycleEvents;
import me.loloed.bot.api.inventory.BotClientInventory;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.platform.Platform;
import me.loloed.bot.api.scheduler.BotScheduler;
import me.loloed.bot.api.util.ClientSimulator;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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

    public Bot(Player vanillaPlayer, Platform platform) {
        this.isClient = platform.isClient();
        this.vanillaPlayer = vanillaPlayer;
        this.platform = platform;
        this.scheduler = new BotScheduler(platform.getExecutor());
        this.inventory = isClient ? new BotClientInventory(this) : new BotInventory(this);
        if (isClient) clientSimulator = null;
        else clientSimulator = new ClientSimulator((ServerPlayer) vanillaPlayer);
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
        scheduler.tick(this);
        blade.tick();
        if (clientSimulator != null) clientSimulator.tick();
        if (jumped) {
            vanillaPlayer.setJumping(false);
        }
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
        vanillaPlayer.setJumping(true);
        jumped = true;
    }

    public void attack() {
        if (isClient) {
            Platform.ClientPlatform client = platform.getClient();
            KeyMapping.click(client.getKey(Minecraft.getInstance().options.keyAttack));
            return;
        }
        clientSimulator.attack();
    }

    public void interact() {
        if (isClient) {
            Platform.ClientPlatform client = platform.getClient();
            KeyMapping.click(client.getKey(Minecraft.getInstance().options.keyUse));
            return;
        }
        clientSimulator.use();
    }

    public void attack(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyAttack.setDown(should);
            return;
        }
        clientSimulator.setAttack(should);
    }

    public void interact(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyUse.setDown(should);
            return;
        }
        clientSimulator.setUse(should);
    }

    public void setSneak(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyShift.setDown(should);
            return;
        }
        vanillaPlayer.setShiftKeyDown(should);
    }

    public void setMoveLeft(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyLeft.setDown(should);
            return;
        }
        clientSimulator.setLeft(should);
    }

    public void setMoveRight(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyRight.setDown(should);
            return;
        }
        clientSimulator.setRight(should);
    }

    public void setMoveForward(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyUp.setDown(should);
            return;
        }
        clientSimulator.setForward(should);
    }

    public void setMoveBackward(boolean should) {
        if (isClient) {
            Minecraft.getInstance().options.keyDown.setDown(should);
            return;
        }
        clientSimulator.setBack(should);
    }

    public void setYaw(float yaw) {
        vanillaPlayer.setYRot(yaw);
    }

    public void setPitch(float pitch) {
        vanillaPlayer.setXRot(pitch);
    }

    public void setSprint(boolean sprint) {
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
}
