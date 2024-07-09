package blade.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class BotMath {
    public static final double PI2 = Math.PI * 2;

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
        if (vec.x == 0.0 && vec.z == 0.0) return vec.y > 0 ? -90 : 90;
        double xz = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
        return (float) Math.toDegrees(Math.atan(-vec.y / xz));
    }

    public static Vec3 getClosestPoint(Vec3 from, AABB boundingBox) {
        return new Vec3(Mth.clamp(from.x, boundingBox.minX, boundingBox.maxX), Mth.clamp(from.y, boundingBox.minY, boundingBox.maxY), Mth.clamp(from.z, boundingBox.minZ, boundingBox.maxZ));
    }
}
