package me.loloed.bot.api.blade.impl.util;

import me.loloed.bot.api.blade.state.impl.StateVector;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.blade.state.value.FixedValue;
import me.loloed.bot.api.material.Material;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.MathUtil;
import me.loloed.bot.api.util.Position;
import me.loloed.bot.api.util.Vector;
import me.loloed.bot.api.world.World;

import java.util.HashMap;
import java.util.Map;

public record HitCrystalPosition(Vector position, double confidence) {
    public static final InjectableUtils.InjectedKey<HitCrystalPosition> HIT_CRYSTAL_POSITION = InjectableUtils.newKey(StateVector.class, parent -> StateKey.producingKey(parent.getBaseName() + "#hit_crystal_position", HitCrystalPosition.class)
            .setProducer((bot, state) -> new FixedValue<>(produce(parent.toRealVector(bot, state), bot.getPosition(), bot.getWorld()))));

    public static final InjectableUtils.InjectedKey<StateVector> HIT_CRYSTAL_STATE_POSITION = InjectableUtils.newKey(StateVector.class, parent -> StateKey.producingKey(parent.getBaseName() + "#hit_crystal_state_vector", StateVector.class)
            .setProducer((bot, state) -> {
                HitCrystalPosition produced = HIT_CRYSTAL_POSITION.getKey(parent).getValue(bot, state);
                return new FixedValue<>(StateVector.from(parent.getBaseName() + "#hit_crystal_state_vector|vector", StateKey.producingKey(parent.getBaseName() + "#hit_crystal_state_vector|vector|producer", Vector.class)
                        .setDefaultValue(new FixedValue<>(produced.position()))));
            }));

    public static HitCrystalPosition produce(Vector target, Position botPos, World world) {
        Vector bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        if (target == null) return null;
        for (int x = target.getBlockX() - 4; x < target.getBlockX() + 4; x++) {
            for (int y = target.getBlockY() - 2; y < target.getBlockY() + 2; y++) {
                for (int z = target.getBlockZ() - 4; z < target.getBlockZ() + 4; z++) {
                    Vector currentPos = Vector.fromBlockPos(x, y, z);
                    Material block = world.getBlock(currentPos);
                    if (!block.hasFlag(MaterialFlag.CRYSTAL_PLACEABLE) && !block.hasFlag(MaterialFlag.AIR)) continue;
                    if (!world.getBlock(currentPos.down()).hasFlag(MaterialFlag.SOLID)) continue;
                    if (target.getBlockX() == x && target.getBlockY() == y && target.getBlockZ() == z) continue;
                    // if (world.rayCast(botHeadPos, currentPos) == null) continue;
                    if (target.distanceSquared(currentPos) < 0.6 * 0.6 * 0.6) continue;
                    double score = estimateScore(world, target, currentPos, block, botPos);
                    if (bestPos == null || score > bestScore) {
                        bestPos = currentPos;
                        bestScore = score;
                    }
                }
            }
        }
        return new HitCrystalPosition(bestPos, bestScore);
    }

    public static double estimateScore(World world, Vector entityPos, Vector blockPos, Material block, Position botPos) {
        double estimate = 0.0f;
        estimate += convertAngleToScore(getAngle(botPos, entityPos, blockPos));
        // estimate += 1.0 - (entityPos.distanceSquared(blockPos));
        estimate -= world.rayCast(entityPos, blockPos) == null ? 0.0 : 1.0;
        estimate += block.hasFlag(MaterialFlag.CRYSTAL_PLACEABLE) ? 1.6 : 0;
        return estimate;
    }

    private static double getAngle(Vector botPos, Vector entityPos, Vector blockPos) {
        Vector botDirection = botPos.subtract(entityPos);
        Vector blockDirection = blockPos.subtract(entityPos);
        double dotProduct = botDirection.getX() * blockDirection.getX() + botDirection.getZ() * blockDirection.getZ();
        return Math.abs(Math.toDegrees(Math.acos(dotProduct)));
    }

    private static double convertAngleToScore(double angle) {
        Map<Double, Double> gradient = new HashMap<>();
        gradient.put(0.0, -1.0);
        gradient.put(0.23, 1.0);
        gradient.put(0.7, 1.0);
        gradient.put(1.0, -2.0);
        return MathUtil.linearGradient(angle / 180.0, gradient);
    }
}
