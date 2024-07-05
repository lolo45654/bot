package blade.impl.util;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import blade.util.ClipUtil;
import blade.util.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public record CrystalPosition(BlockPos obsidian, Vec3 placeAgainst, AABB crystalAABB, double confidence) {
    public static CrystalPosition get(Bot bot){
        BladeMachine blade = bot.getBlade();
        LivingEntity target = blade.get(ConfigKeys.TARGET);
        return produce(target.position(), target.getBoundingBox(), bot.getVanillaPlayer().getEyePosition(), bot.getVanillaPlayer().level());
    }

    public static CrystalPosition produce(Vec3 target, AABB targetAABB, Vec3 botHeadPos, Level world) {
        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        Vec3 bestPlaceAgainst = null;
        AABB bestCrystalAABB = null;
        if (target == null) return null;
        BlockPos targetBlock = BlockPos.containing(target);
        for (int x = targetBlock.getX() - 2; x < targetBlock.getX() + 2; x++) {
            for (int y = targetBlock.getY() - 1; y < targetBlock.getY() + 2; y++) {
                for (int z = targetBlock.getZ() - 2; z < targetBlock.getZ() + 2; z++) {
                    BlockPos currentPos = BlockPos.containing(x, y, z);
                    BlockState state = world.getBlockState(currentPos);
                    if (!state.is(Blocks.BEDROCK) && !state.is(Blocks.OBSIDIAN) && !state.isAir()) continue;
                    if (!world.getBlockState(currentPos.above()).isAir()) continue;
                    if (targetBlock.getX() == currentPos.getX() && targetBlock.getY() == currentPos.getY() && targetBlock.getZ() == currentPos.getZ()) continue;
                    if (ShapeUtil.intersects(targetAABB, new AABB(currentPos))) continue;
                    Vec3 placeAgainst = getPlaceAgainst(world, botHeadPos, currentPos);
                    if (placeAgainst == null) continue;
                    if (!ClipUtil.hasLineOfSightPerformant(world, botHeadPos, Vec3.atCenterOf(currentPos))) continue;
                    if (!ClipUtil.hasLineOfSightPerformant(world, botHeadPos, Vec3.atCenterOf(currentPos.above()))) continue;
                    Vec3 crystalBottom = Vec3.atBottomCenterOf(currentPos.above());
                    AABB crystalAABB = new AABB(crystalBottom.x - 1.0, crystalBottom.y, crystalBottom.z - 1.0, crystalBottom.x + 1.0, crystalBottom.y + 2.0, crystalBottom.z + 1.0);;
                    double score = getScore(world, target, currentPos, state);
                    if (bestPos == null || score > bestScore) {
                        bestPos = currentPos;
                        bestScore = score;
                        bestPlaceAgainst = placeAgainst;
                        bestCrystalAABB = crystalAABB;
                    }
                }
            }
        }
        return bestPos == null ? null : new CrystalPosition(bestPos, bestPlaceAgainst, bestCrystalAABB, bestScore);
    }

    public static Vec3 getPlaceAgainst(Level world, Vec3 headPos, BlockPos blockPos) {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (int offset : new int[]{-1, 1}) {
                BlockPos checkingBlockPos = blockPos.relative(axis, offset);
                Vec3 checkingPos = Vec3.atBottomCenterOf(checkingBlockPos);
                BlockState state = world.getBlockState(checkingBlockPos);
                if (state.isAir()) continue;
                if (state.hasBlockEntity()) continue;
                VoxelShape shape = state.getShape(world, checkingBlockPos).move(checkingBlockPos.getX(), checkingBlockPos.getY(), checkingBlockPos.getZ());
                Optional<Vec3> point = shape.closestPointTo(checkingPos);
                if (point.isEmpty()) continue;
                return point.get();
            }
        }
        return null;
    }

    public static double getScore(Level world, Vec3 targetPos, BlockPos pos, BlockState state) {
        double score = 0.0f;
        score += 12.0 - Math.min(targetPos.distanceToSqr(Vec3.atBottomCenterOf(pos.above())) / 12, 12);
        score += state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK) ? 3.0 : 0;
        return score / 6;
    }
}
