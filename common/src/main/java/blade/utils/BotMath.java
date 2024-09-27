package blade.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
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

    public static Rotation rotationBezier(List<Rotation> points, float t) {
        if (points.size() < 2) {
            throw new IllegalArgumentException("Too little points.");
        }

        points = new ArrayList<>(points);
        List<Rotation> nextPoints = new ArrayList<>();
        while (points.size() > 1) {
            nextPoints.clear();
            for (int i = 0; i < points.size() - 1; i++) {
                Rotation current = points.get(i);
                Rotation next = points.get(i + 1);
                nextPoints.add(new Rotation(interpolateYaw(current.yaw(), next.yaw(), t), interpolate(current.pitch(), next.pitch(), t)));
            }
            points.clear();
            points.addAll(nextPoints);
        }

        return points.getFirst();
    }

    public static float interpolateYaw(float p1, float p2, float t) {
        if (Math.abs(p1 - p2) > 180) {
            // p1 = -150
            // p2 = 160
            // t = 0.5
            // raw interpolation = 185
            // wrapped interpolation = -175
            // return -175
            return ((1 - t) * (p1 % 360) + t * (p2 % 360) + 180) % 360 - 180;
        }
        return (1 - t) * p1 + t * p2;
    }

    public static float interpolate(float p1, float p2, float t) {
        return (1 - t) * p1 + t * p2;
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static double sigmoidDerivative(double x) {
        double sigmoid = sigmoid(x);
        return sigmoid * (1.0 - sigmoid);
    }
}
