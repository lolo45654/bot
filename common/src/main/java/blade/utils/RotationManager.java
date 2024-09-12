package blade.utils;

import blade.Bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RotationManager {
    protected Rotation lastRotation = null;
    protected Rotation lastTarget = null;
    protected Rotation target = null;
    protected List<Rotation> controlPoints = new ArrayList<>();
    protected float speed = 0.0f; // in milliseconds how long it should take to reach the target yaw & pitch
    protected float time = 0.0f;
    protected long lastUpdate = System.currentTimeMillis();

    public void update(Bot bot) {
        if (lastRotation == null || lastTarget == null || target == null || speed <= 0.0f) return;
        if (time > 1.0f) {
            time = 0.0f;
            target = null;
            return;
        }
        long update = System.currentTimeMillis();
        float delta = (update - lastUpdate) / (speed + 1);
        time += delta;
        time = Math.clamp(time, 0.0f, 1.0f);
        lastUpdate = update;

        float currYaw = bot.getVanillaPlayer().getYRot();
        float currPitch = bot.getVanillaPlayer().getXRot();
        float height = Math.abs(target.pitch() - currPitch);
        float width = Math.min(Math.abs((target.yaw() > 0 ? target.yaw() - 180 : target.yaw() + 180) - (currYaw > 0 ? currYaw - 180 : currYaw + 180)), Math.abs(target.yaw() - currYaw));
        Rotation lowerCorner = new Rotation(Math.min(currYaw, target.yaw()), Math.min(currPitch, target.pitch()));

        controlPoints.removeIf(rotation -> rotation.yaw() < lowerCorner.yaw() || rotation.yaw() > lowerCorner.yaw() + width || rotation.pitch() < lowerCorner.pitch() || rotation.pitch() > lowerCorner.pitch() + height);

        List<Rotation> points = new ArrayList<>();
        points.add(new Rotation(currYaw, currPitch));
        points.addAll(controlPoints);
        points.add(target);
        Rotation rotation = BotMath.rotationBezier(points, delta);

        bot.setYaw(rotation.yaw());
        bot.setPitch(rotation.pitch());
    }

    public void setTarget(float currYaw, float currPitch, float targetYaw, float targetPitch, float speed) {
        target = new Rotation(targetYaw, targetPitch);
        lastRotation = new Rotation(currYaw, currPitch);
        lastTarget = target;
        this.speed = speed;

        if (!controlPoints.isEmpty()) return;
        float height = Math.abs(target.pitch() - currPitch);
        float width = Math.min(Math.abs((target.yaw() > 0 ? target.yaw() - 180 : target.yaw() + 180) - (currYaw > 0 ? currYaw - 180 : currYaw + 180)), Math.abs(target.yaw() - currYaw));
        Rotation lowerCorner = new Rotation(Math.min(currYaw, target.yaw()), Math.min(currPitch, target.pitch()));
        if (height < 5 || width < 5) return;
        controlPoints.add(new Rotation(ThreadLocalRandom.current().nextFloat(lowerCorner.yaw(), lowerCorner.yaw() + width), ThreadLocalRandom.current().nextFloat(lowerCorner.pitch(), lowerCorner.pitch() + height)));
    }
}
