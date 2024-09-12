package blade.debug.visual;

import net.minecraft.world.phys.AABB;

public record VisualBox(AABB box, float red, float green, float blue, float alpha) implements VisualDebug {
    public static final float DEFAULT_ALPHA = 0.2f;

    public VisualBox(AABB box, float red, float green, float blue) {
        this(box, red, green, blue, DEFAULT_ALPHA);
    }
}
