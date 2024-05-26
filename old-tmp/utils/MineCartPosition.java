package me.loloed.bot.api.blade.impl.util;

import me.loloed.bot.api.blade.state.impl.StateVector;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.blade.state.value.FixedValue;
import me.loloed.bot.api.material.Material;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.Vector;
import me.loloed.bot.api.world.World;

public record MineCartPosition(Vector position, double confidence) {
    public static final InjectableUtils.InjectedKey<MineCartPosition> MINECART_POSITION = InjectableUtils.newKey(StateVector.class, parent -> StateKey.producingKey(parent.getBaseName() + "#minecart_position", MineCartPosition.class)
            .setProducer((bot, state) -> new FixedValue<>(produce(parent.toRealVector(bot, state), bot.getHeadPosition(), bot.getWorld()))));

    public static MineCartPosition produce(Vector target, Vector botHeadPos, World world) {
        Vector bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        if (target == null) return null;
        for (int x = target.getBlockX() - 4; x < target.getBlockX() + 4; x++) {
            for (int y = target.getBlockY() - 4; y < target.getBlockY() + 4; y++) {
                for (int z = target.getBlockZ() - 4; z < target.getBlockZ() + 4; z++) {
                    Vector currentPos = Vector.fromBlockPos(x, y, z);
                    Material block = world.getBlock(currentPos);
                    if (!block.hasFlag(MaterialFlag.RAIL) && !block.hasFlag(MaterialFlag.AIR)) continue;
                    if (!world.getBlock(currentPos.down()).hasFlag(MaterialFlag.SOLID)) continue;
                    if (target.getBlockX() == x && target.getBlockY() == y && target.getBlockZ() == z) continue;
                    // if (world.rayCast(botHeadPos, currentPos) == null) continue;
                    double score = estimateScore(world, target, currentPos, block);
                    if (bestPos == null || score > bestScore) {
                        bestPos = currentPos;
                        bestScore = score;
                    }
                }
            }
        }
        return new MineCartPosition(bestPos, bestScore);
    }

    public static double estimateScore(World world, Vector entityPos, Vector blockPos, Material block) {
        double estimate = 0.0f;
        estimate += 1.0 - Math.abs(entityPos.getX() - blockPos.getX());
        estimate += 1.0 - entityPos.distanceSquared(blockPos);
        estimate -= world.rayCast(entityPos, blockPos) == null ? 0.0 : 1.0;
        estimate += block.hasFlag(MaterialFlag.RAIL) ? 1.2 : 0;
        return estimate;
    }
}
