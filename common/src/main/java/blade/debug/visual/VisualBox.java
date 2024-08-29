package blade.debug.visual;

import net.minecraft.world.phys.AABB;

public record VisualBox(AABB box, float red, float green, float blue, float alpha) implements VisualDebug { // TODO fabric impl doesn't work
}
