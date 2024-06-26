package blade.impl;

import blade.Bot;
import blade.impl.util.MineCartPosition;
import blade.util.blade.ConfigKey;
import net.minecraft.world.entity.LivingEntity;

public class ConfigKeys {
    /**
     * 0 through 1, 1 being the best.
     */
    public static final ConfigKey<Float> DIFFICULTY = ConfigKey.key("difficulty", 0.5f);

    public static float getDifficultyReversed(Bot bot) {
        return 0 - bot.getBlade().get(DIFFICULTY) + 1;
    }

    public static float getDifficultyReversedCubic(Bot bot) {
        float d = getDifficultyReversed(bot);
        return d * d;
    }

    public static final ConfigKey<LivingEntity> TARGET = ConfigKey.key("target", null);

    public static final ConfigKey<MineCartPosition> MINE_CART_POSITION = ConfigKey.key("mine_cart_position", null);
}
