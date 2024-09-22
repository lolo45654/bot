package blade.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class BlockUtils {
    public static Vec3 getPlaceAgainst(Level world, LivingEntity target, Vec3 botHeadPos, BlockPos obsidian) {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (Direction.AxisDirection offset : Direction.AxisDirection.values()) {
                Direction direction = Direction.fromAxisAndDirection(axis, offset);
                BlockPos placeAgainstPos = obsidian.relative(direction, 1);
                BlockState placeAgainstState = world.getBlockState(placeAgainstPos);
                if (placeAgainstState.isAir()) continue;
                if (placeAgainstState.hasBlockEntity()) continue;
                VoxelShape placeAgainstShape = placeAgainstState.getShape(world, placeAgainstPos).move(placeAgainstPos.getX(), placeAgainstPos.getY(), placeAgainstPos.getZ());
                Optional<Vec3> closestPos = placeAgainstShape.closestPointTo(Vec3.atCenterOf(obsidian));

                if (closestPos.isEmpty()) continue;
                if (ClipUtils.getHitResult(world, botHeadPos, closestPos.get().relative(direction.getOpposite(), 0.01), null, e -> e != target && (!(e instanceof EndCrystal) || e.position().equals(Vec3.atBottomCenterOf(obsidian.above()))), s -> s != world.getBlockState(obsidian)).getType() != HitResult.Type.MISS)
                    continue;
                return closestPos.get();
            }
        }
        return null;
    }
}
