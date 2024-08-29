package blade.impl.util;

import blade.Bot;
import blade.impl.ConfigKeys;
import blade.util.ClipUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.Optional;

public record CrystalPosition(BlockPos obsidian, Vec3 placeAgainst, AABB crystalAABB, double confidence) {
    public static CrystalPosition get(Bot bot, CrystalPosition previous){
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (target == null) return null;
        return produce(previous, target, bot.getVanillaPlayer().getEyePosition(), bot.getVanillaPlayer().level());
    }

    public static CrystalPosition produce(CrystalPosition previous, LivingEntity target, Vec3 botHeadPos, Level world) {
        CrystalPosition best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        if (target == null) return null;
        BlockPos targetBlock = BlockPos.containing(target.position());
        for (int x = targetBlock.getX() - 2; x < targetBlock.getX() + 2; x++) {
            for (int y = targetBlock.getY() - 3; y < targetBlock.getY(); y++) {
                for (int z = targetBlock.getZ() - 2; z < targetBlock.getZ() + 2; z++) {
                    BlockPos obsidian = new BlockPos(x, y, z);
                    MutableObject<Vec3> placeAgainst = new MutableObject<>();
                    MutableBoolean crystalPlaced = new MutableBoolean();
                    Vec3 crystalBottom = Vec3.atBottomCenterOf(obsidian.above());
                    AABB crystalAABB = new AABB(crystalBottom.x - 1.0, crystalBottom.y, crystalBottom.z - 1.0, crystalBottom.x + 1.0, crystalBottom.y + 2.0, crystalBottom.z + 1.0);;
                    CrystalPosition position = new CrystalPosition(obsidian, null, crystalAABB, 0.0);
                    if (!isPossible(world, botHeadPos, target, position, placeAgainst, crystalPlaced)) continue;
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
            if (!isPossible(world, botHeadPos, target, previous, placeAgainst, crystalPlaced)) return best;
            double previousConfidence = getScore(world, target, previous.obsidian, world.getBlockState(previous.obsidian), crystalPlaced.booleanValue());
            if (previousConfidence - bestScore > -0.4) return new CrystalPosition(previous.obsidian, placeAgainst.getValue(), previous.crystalAABB, previousConfidence);
        }
        return best;
    }

    public static Vec3 getPlaceAgainst(Level world, LivingEntity target, Vec3 headPos, BlockPos blockPos) {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (Direction.AxisDirection offset : Direction.AxisDirection.values()) {
                Direction direction = Direction.fromAxisAndDirection(axis, offset);
                BlockPos placeAgainstPos = blockPos.relative(direction, 1);
                BlockState state = world.getBlockState(placeAgainstPos);
                if (state.isAir()) continue;
                if (state.hasBlockEntity()) continue;
                VoxelShape shape = state.getShape(world, placeAgainstPos).move(placeAgainstPos.getX(), placeAgainstPos.getY(), placeAgainstPos.getZ());
                Optional<Vec3> point = shape.closestPointTo(Vec3.atCenterOf(blockPos));

                if (point.isEmpty()) continue;
                if (ClipUtil.getHitResult(world, headPos, point.get().relative(direction.getOpposite(), 0.01), null, e -> e != target && (!(e instanceof EndCrystal) || e.position().equals(Vec3.atBottomCenterOf(blockPos.above()))), s -> s != world.getBlockState(blockPos)).getType() != HitResult.Type.MISS)
                    continue;
                return point.get();
            }
        }
        return null;
    }

    public static boolean isPossible(Level world, Vec3 headPos, LivingEntity target, CrystalPosition position, MutableObject<Vec3> placeAgainst, MutableBoolean crystalPlaced) {
        BlockPos obsidian = position.obsidian;
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

        placeAgainst.setValue(getPlaceAgainst(world, target, headPos, obsidian));
        if (placeAgainst.getValue() == null) return false;
        return ClipUtil.getHitResult(world, headPos, Vec3.atCenterOf(obsidian.above()), null, e -> e != target, s -> true).getType() == HitResult.Type.MISS;
    }

    public static double getScore(Level world, LivingEntity target, BlockPos pos, BlockState state, boolean crystalPlaced) {
        double score = 0.0f;
        // score += 1.0 - Math.min(target.distanceToSqr(Vec3.atBottomCenterOf(pos.above())) / 12, 1);
        score += state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK) ? 1.0 : 0;
        score += crystalPlaced ? 1.0 : 0;
        score += Explosion.getSeenPercent(Vec3.atCenterOf(pos.above()), target);
        // TODO score -= getExposure(crystal, bot) * 2
        score += Math.min((target.getY() - pos.getY()) / 2, 1);
        return score / 5;
    }
}
