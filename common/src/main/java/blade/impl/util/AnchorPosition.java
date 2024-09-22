package blade.impl.util;

import blade.Bot;
import blade.impl.ConfigKeys;
import blade.utils.BlockUtils;
import blade.utils.ExplosionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;

public record AnchorPosition(BlockPos anchorPos, BlockState anchorState, Vec3 placeAgainst, double confidence) {
    public static AnchorPosition get(Bot bot, AnchorPosition previous) {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (target == null) return null;
        return produce(bot.getVanillaPlayer(), previous, target);
    }

    public static AnchorPosition produce(LivingEntity bot, AnchorPosition previous, LivingEntity target) {
        if (target == null) return null;
        final Level world = bot.level();
        final Vec3 botHeadPos = bot.getEyePosition();
        final float reach = (float) AttackUtil.getEntityInteractionRange(bot);
        AnchorPosition best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos targetBlock = BlockPos.containing(target.position());
        for (int x = targetBlock.getX() - 2; x < targetBlock.getX() + 2; x++) {
            for (int y = targetBlock.getY() - 3; y < targetBlock.getY() + 2; y++) {
                for (int z = targetBlock.getZ() - 2; z < targetBlock.getZ() + 2; z++) {
                    BlockPos anchorPos = new BlockPos(x, y, z);
                    BlockState anchorState = world.getBlockState(anchorPos);
                    MutableObject<Vec3> placeAgainst = new MutableObject<>();
                    if (!isPossible(world, botHeadPos, target, anchorPos, anchorState, reach, placeAgainst)) continue;
                    boolean charged = anchorState.is(Blocks.RESPAWN_ANCHOR) && anchorState.getValue(RespawnAnchorBlock.CHARGE) > 0;
                    double score = getScore(world, target, anchorPos, world.getBlockState(anchorPos), charged);
                    if (score > bestScore) {
                        best = new AnchorPosition(anchorPos, anchorState, placeAgainst.getValue(), score);
                        bestScore = score;
                    }
                }
            }
        }
        if (previous != null && best != null) {
            MutableObject<Vec3> placeAgainst = new MutableObject<>();
            BlockPos prevAnchorPos = previous.anchorPos;
            BlockState prevAnchorState = world.getBlockState(prevAnchorPos);
            if (!isPossible(world, botHeadPos, target, prevAnchorPos, prevAnchorState, reach, placeAgainst)) return best;
            boolean charged = prevAnchorState.is(Blocks.RESPAWN_ANCHOR) && prevAnchorState.getValue(RespawnAnchorBlock.CHARGE) > 0;
            double previousConfidence = getScore(world, target, prevAnchorPos, world.getBlockState(prevAnchorPos), charged);
            if (previousConfidence - bestScore > -0.2) return new AnchorPosition(prevAnchorPos, prevAnchorState, placeAgainst.getValue(), previousConfidence);
        }
        return best;
    }

    public static boolean isPossible(Level world, Vec3 botHeadPos, LivingEntity target, BlockPos anchorPos, BlockState anchorState, float reach, MutableObject<Vec3> placeAgainst) {
        int x = anchorPos.getX();
        int y = anchorPos.getY();
        int z = anchorPos.getZ();
        if (anchorState.isAir()) {
            List<Entity> entitiesBlocking = world.getEntities(null, new AABB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
            if (!entitiesBlocking.isEmpty()) return false;
        } else if (!anchorState.is(Blocks.RESPAWN_ANCHOR)) return false;

        placeAgainst.setValue(BlockUtils.getPlaceAgainst(world, target, botHeadPos, anchorPos));
        return placeAgainst.getValue() != null && placeAgainst.getValue().distanceToSqr(botHeadPos) < reach * reach;
    }

    public static double getScore(Level world, LivingEntity target, BlockPos anchorPos, BlockState anchorState, boolean charged) {
        double score = 0.0f;
        // score += 1.0 - Math.min(target.distanceToSqr(Vec3.atBottomCenterOf(pos.above())) / 12, 1);
        score += anchorState.is(Blocks.RESPAWN_ANCHOR) ? 1.0 : 0;
        score += charged ? 1.0 : 0;
        Vec3 explosionCenter = Vec3.atCenterOf(anchorPos.above());
        score += ExplosionUtils.getCrystalDamage(explosionCenter, target, Explosion.getSeenPercent(explosionCenter, target)) / target.getMaxHealth();
        // TODO score -= getExposure(crystal, bot) * 2
        score += Math.min((target.getY() - anchorPos.getY()) / 2, 1);
        return score / 5;
    }
}
