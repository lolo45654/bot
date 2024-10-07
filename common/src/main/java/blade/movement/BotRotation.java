package blade.movement;

import blade.Bot;
import blade.utils.Rotation;

public class BotRotation {
    private static final float YAW_MODIFIER = 1.1f;
    private static final float PITCH_MODIFIER = 0.6f;

    protected Rotation lastRotation = null;
    protected Rotation target = null;
    protected float speed = 0.0f; // in milliseconds how long it should take to reach the target yaw & pitch
    protected long lastUpdate = System.currentTimeMillis();

    public void update(Bot bot) {
        if (target == null || speed <= 0.0f) return;
        long update = System.currentTimeMillis();
        long diffUpdate = update - lastUpdate;
        float delta = Math.min(diffUpdate / (speed + 1), 1);
        lastUpdate = update;

        float currYaw = bot.getVanillaPlayer().getYRot();
        float currPitch = bot.getVanillaPlayer().getXRot();
        if (lastRotation == null) lastRotation = new Rotation(currYaw, currPitch);
        float lastDiffYaw = currYaw - lastRotation.yaw();
        float lastDiffPitch = currPitch - lastRotation.pitch();
        float diffYaw = target.yaw() - currYaw;
        if (diffYaw > 180) {
            diffYaw -= 360;
        } else if (diffYaw < -180) {
            diffYaw += 360;
        }
        float diffPitch = target.pitch() - currPitch;

        float turnYaw = (diffYaw * delta + lastDiffYaw * 0.05f) * YAW_MODIFIER;
        float turnPitch = (diffPitch * delta + lastDiffPitch * 0.05f) * PITCH_MODIFIER;
        bot.getVanillaPlayer().turn(turnYaw, turnPitch);
        lastRotation = new Rotation(currYaw, currPitch);
    }

    public void setTarget(float targetYaw, float targetPitch, float speed) {
        this.target = new Rotation(targetYaw, targetPitch);
        this.speed = speed;
    }
}
