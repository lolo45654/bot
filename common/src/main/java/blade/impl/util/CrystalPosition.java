package blade.impl.util;

import blade.Bot;
import blade.impl.ConfigKeys;
import blade.utils.BlockUtils;
import blade.utils.ClipUtils;
import blade.utils.ExplosionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;

public record CrystalPosition(BlockPos obsidian, Vec3 placeAgainst, AABB crystalAABB, double confidence) {
    public static CrystalPosition get(Bot bot, CrystalPosition previous) {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (target == null) return null;
        return produce(bot.getVanillaPlayer(), previous, target);
    }

    public static CrystalPosition produce(LivingEntity bot, CrystalPosition previous, LivingEntity target) {
        if (target == null) return null;
        final Level world = bot.level();
        final Vec3 botHeadPos = bot.getEyePosition();
        final float reach = (float) AttackUtil.getEntityInteractionRange(bot);
        CrystalPosition best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos targetBlock = BlockPos.containing(target.position());
        for (int x = targetBlock.getX() - 2; x < targetBlock.getX() + 2; x++) {
            for (int y = targetBlock.getY() - 3; y < targetBlock.getY() + 2; y++) {
                for (int z = targetBlock.getZ() - 2; z < targetBlock.getZ() + 2; z++) {
                    BlockPos obsidian = new BlockPos(x, y, z);
                    MutableObject<Vec3> placeAgainst = new MutableObject<>();
                    MutableBoolean crystalPlaced = new MutableBoolean();
                    Vec3 crystalBottom = Vec3.atBottomCenterOf(obsidian.above());
                    AABB crystalAABB = new AABB(crystalBottom.x - 1.0, crystalBottom.y, crystalBottom.z - 1.0, crystalBottom.x + 1.0, crystalBottom.y + 2.0, crystalBottom.z + 1.0);;
                    if (!isPossible(world, botHeadPos, target, obsidian, reach, placeAgainst, crystalPlaced)) continue;
                    double score = getScore(world, target, obsidian, world.getBlockState(obsidian), crystalPlaced.booleanValue());
                    if (score > bestScore) {
                        best = new CrystalPosition(obsidian, placeAgainst.getValue(), crystalAABB, score);
                        bestScore = score;
                    }
                }
            }
        }
        if (previous != null && best != null) {
            MutableObject<Vec3> placeAgainst = new MutableObject<>();
            MutableBoolean crystalPlaced = new MutableBoolean();
            if (!isPossible(world, botHeadPos, target, previous.obsidian, reach, placeAgainst, crystalPlaced)) return best;
            double previousConfidence = getScore(world, target, previous.obsidian, world.getBlockState(previous.obsidian), crystalPlaced.booleanValue());
            if (previousConfidence - bestScore > -0.2) return new CrystalPosition(previous.obsidian, placeAgainst.getValue(), previous.crystalAABB, previousConfidence);
        }
        return best;
    }

    public static boolean isPossible(Level world, Vec3 botHeadPos, LivingEntity target, BlockPos obsidian, float reach, MutableObject<Vec3> placeAgainst, MutableBoolean crystalPlaced) {
        int x = obsidian.getX();
        int y = obsidian.getY();
        int z = obsidian.getZ();
        BlockState state = world.getBlockState(obsidian);
        if (state.isAir()) {
            List<Entity> entitiesBlocking = world.getEntities(null, new AABB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
            if (!entitiesBlocking.isEmpty()) return false;
        } else if (!state.is(Blocks.BEDROCK) && !state.is(Blocks.OBSIDIAN)) return false;

        if (!world.getBlockState(obsidian.above()).isAir()) return false;

        List<Entity> entitiesBlocking = world.getEntities(null, new AABB(x, y + 1.0, z, x + 1.0, y + 3.0, z + 1.0));
        crystalPlaced.setFalse();
        for (Entity entity : entitiesBlocking) {
            if (!(entity instanceof EndCrystal)) return false;
            crystalPlaced.setTrue();
        }

        placeAgainst.setValue(BlockUtils.getPlaceAgainst(world, target, botHeadPos, obsidian));
        if (placeAgainst.getValue() == null) return false;
        Vec3 crystal = Vec3.atCenterOf(obsidian.above());
        return botHeadPos.distanceToSqr(crystal) < reach * reach && ClipUtils.getHitResult(world, botHeadPos, crystal, null, e -> e != target, s -> true).getType() == HitResult.Type.MISS;
    }

    public static double getScore(Level world, LivingEntity target, BlockPos obsidianPos, BlockState obsidianState, boolean crystalPlaced) {
        double score = 0.0f;
        // score += 1.0 - Math.min(target.distanceToSqr(Vec3.atBottomCenterOf(pos.above())) / 12, 1);
        score += obsidianState.is(Blocks.OBSIDIAN) || obsidianState.is(Blocks.BEDROCK) ? 1.0 : 0;
        score += crystalPlaced ? 1.0 : 0;
        Vec3 explosionCenter = Vec3.atCenterOf(obsidianPos.above());
        score += ExplosionUtils.getCrystalDamage(explosionCenter, target, Explosion.getSeenPercent(explosionCenter, target)) / target.getMaxHealth();
        // TODO score -= getExposure(crystal, bot) * 2
        score += Math.min((target.getY() - obsidianPos.getY()) / 2, 1);
        return score / 5;
    }
}
