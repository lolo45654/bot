package blade.util;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public class ShapeUtil {
    public static boolean intersects(AABB box1, AABB box2) {
        return Shapes.joinIsNotEmpty(Shapes.create(box1), Shapes.create(box2), BooleanOp.AND);
    }
}
