package me.loloed.bot.api.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class BotMath {
    public static final double PI2 = Math.PI * 2;

    public static float[] getRotationForBow(Vec3 bot, Vec3 target, int chargedTicks) {
        float velocity = chargedTicks / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;

        Vec3 vec = target.subtract(bot);
        double x = vec.x;
        double z = vec.y;
        double theta = Math.atan2(-x, z);
        return new float[] { (float) Math.toDegrees((theta + 6.283185307179586) % 6.283185307179586), getPitchRotation(bot, target, velocity) };
    }

    private static float getPitchRotation(Vec3 bot, Vec3 target, float velocity) {
        double relativeX = target.x - bot.x;
        double relativeY = target.y - bot.y;
        double relativeZ = target.z - bot.z;

        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = velocity * velocity;
        return (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));
    }

    public static Pair<float[], Integer> getBowChargeTicks(Vec3 bot, Vec3 target) {
        for (int ticks = 5; ticks < 24; ticks++) {
            float[] rotation = getRotationForBow(bot, target, ticks);
            if (!Float.isNaN(rotation[1])) return new Pair<>(rotation, ticks);
        }
        return null;
    }

    public static double linearGradient(double value, Map<Double, Double> gradient) {
        double min = 0.0;
        double start = 0.0;
        double max = 1.0;
        double end = 1.0;
        for (Map.Entry<Double, Double> entry : gradient.entrySet()) {
            if (entry.getKey() <= value) {
                min = entry.getKey();
                start = entry.getValue();
            }
            if (entry.getKey() >= value) {
                max = entry.getKey();
                end = entry.getValue();
                break;
            }
        }

        double percent = 1 / (max - min) * (value - min);
        return end * percent + start * (1 - percent);
    }

    public static float getYaw(Vec3 vec) {
        if (vec.x == 0.0 && vec.z == 0.0) return 0.0F;
        double theta = Math.atan2(-vec.x, vec.z);
        return (float) Math.toDegrees((theta + PI2) % PI2);
    }

    public static float getPitch(Vec3 vec) {
        if (vec.x == 0.0 && vec.z == 0.0) return 0.0F;
        double xz = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
        return (float) Math.toDegrees(Math.atan(-vec.y / xz));
    }

    public static Vec3 getClosestPoint(Vec3 from, AABB boundingBox) {
        return new Vec3(Mth.clamp(from.x, boundingBox.minX, boundingBox.maxX), Mth.clamp(from.y, boundingBox.minY, boundingBox.maxY), Mth.clamp(from.z, boundingBox.minZ, boundingBox.maxZ));
    }
}
