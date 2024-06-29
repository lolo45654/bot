package blade.util;

import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClipUtil {
    public static boolean hasLineOfSightPerformant(Level world, Vec3 start, Vec3 end) {
        return world.isBlockInLine(new ClipBlockStateContext(start, end, state -> !state.isAir())).getType() == HitResult.Type.MISS;
    }
}
