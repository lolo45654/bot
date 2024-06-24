package me.loloed.bot.api.blade.impl.goal;

import me.loloed.bot.api.blade.BladeGoal;
import me.loloed.bot.api.blade.ConfigKey;
import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.impl.StateKeys;
import me.loloed.bot.api.blade.state.BladeState;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

public class KillTargetGoal extends BladeGoal {
    private Supplier<LivingEntity> target;

    public KillTargetGoal(Supplier<LivingEntity> target) {
        super("kill_entity");
        this.target = target;
    }

    @Override
    public double getScore(BladeState state, BladeState difference) {
        double score = 0;
        score += -difference.getValue(StateKeys.TARGET_HEALTH) * 4;
        return score;
    }

    @Override
    public void tick() {
        LivingEntity target = this.target.get();
        if (target == null) return;
        bot.getBlade().set(ConfigKeys.TARGET, target);
    }
}
