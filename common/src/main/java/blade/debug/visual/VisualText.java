package blade.debug.visual;

import net.minecraft.world.phys.Vec3;

public record VisualText(Vec3 pos, String text) implements VisualDebug {
}
