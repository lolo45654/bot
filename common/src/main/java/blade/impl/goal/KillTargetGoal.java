package blade.impl.goal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.state.BladeState;
import blade.util.blade.BladeGoal;
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
