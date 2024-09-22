package blade.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

import java.util.function.Predicate;

public class ClipUtils {
    public static boolean hasLineOfSight(Level world, Vec3 start, Vec3 end, Predicate<BlockState> tester) {
        return world.isBlockInLine(new ClipBlockStateContext(start, end, state -> !state.isAir() && tester.test(state))).getType() == HitResult.Type.MISS;
    }

    public static boolean hasLineOfSight(Level world, Vec3 start, Vec3 end) {
        return hasLineOfSight(world, start, end, s -> true);
    }

    public static HitResult getHitResult(Level world, Vec3 start, Vec3 end, Entity producer, Predicate<Entity> tester, Predicate<BlockState> blockTester) {
        BlockHitResult result = world.isBlockInLine(new ClipBlockStateContext(start, end, s -> !s.isAir() && blockTester.test(s)));
        if (result.getType() != HitResult.Type.MISS) {
            end = result.getLocation();
        }
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(world, producer, start, end, new AABB(BlockPos.containing(start)).expandTowards(end.subtract(start)).inflate(1.0f), tester, 1.0f);
        return entityHit != null ? entityHit : result;
    }

    public static HitResult getHitResult(Level world, Vec3 start, Vec3 end, Entity producer) {
        return getHitResult(world, start, end, producer, e -> true, s -> true);
    }

    public static HitResult getHitResult(Level world, Vec3 start, Vec3 end) {
        return getHitResult(world, start, end, null);
    }
}
