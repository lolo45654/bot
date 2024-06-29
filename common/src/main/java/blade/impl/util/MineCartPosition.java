package blade.impl.util;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.ConfigKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public record MineCartPosition(Vec3 position, double confidence) {
    public static MineCartPosition get(Bot bot){
        BladeMachine blade = bot.getBlade();
        LivingEntity target = blade.get(ConfigKeys.TARGET);
        return produce(target.position(), bot.getVanillaPlayer().getEyePosition(), bot.getVanillaPlayer().level());
    }

    public static MineCartPosition produce(Vec3 target, Vec3 botHeadPos, Level world) {
        Vec3 bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        if (target == null) return null;
        BlockPos targetBlock = BlockPos.containing(target);
        for (int x = targetBlock.getX() - 2; x < targetBlock.getX() + 2; x++) {
            for (int y = targetBlock.getY() - 1; y < targetBlock.getY() + 1; y++) {
                for (int z = targetBlock.getZ() - 2; z < targetBlock.getZ() + 2; z++) {
                    BlockPos currentPos = BlockPos.containing(x, y, z);
                    BlockState state = world.getBlockState(currentPos);
                    if (!(state.getBlock() instanceof BaseRailBlock) && !state.isAir()) continue;
                    if (!world.getBlockState(currentPos.below()).isSolid()) continue;
                    if (targetBlock.getX() == currentPos.getX() && targetBlock.getY() == currentPos.getY() && targetBlock.getZ() == currentPos.getZ()) continue;
                    ClipContext clip = new ClipContext(botHeadPos, Vec3.atBottomCenterOf(currentPos), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty());
                    if (world.clip(clip).getType() != HitResult.Type.MISS) continue;
                    // if (world.rayCast(botHeadPos, currentPos) == null) continue;
                    double score = estimateScore(world, target, currentPos, state);
                    if (bestPos == null || score > bestScore) {
                        bestPos = Vec3.atBottomCenterOf(currentPos);
                        bestScore = score;
                    }
                }
            }
        }
        return new MineCartPosition(bestPos, bestScore);
    }

    public static double estimateScore(Level world, Vec3 targetPos, BlockPos pos, BlockState block) {
        double estimate = 0.0f;
        estimate += 1.0 - Math.abs(targetPos.x - pos.getX());
        estimate += 1.0 - targetPos.distanceToSqr(Vec3.atBottomCenterOf(pos));
        estimate += block.getBlock() instanceof BaseRailBlock ? 2.3 : 0;
        return estimate;
    }
}
