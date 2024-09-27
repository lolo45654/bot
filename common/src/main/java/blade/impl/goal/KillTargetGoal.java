package blade.impl.goal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.planner.score.ScoreState;
import blade.utils.blade.BladeGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.function.Supplier;

public class KillTargetGoal extends BladeGoal {
    private final Supplier<LivingEntity> target;
    private double prevReward = 0;

    public KillTargetGoal(Supplier<LivingEntity> target) {
        this.target = target;
    }

    @Override
    public double getScore(ScoreState state, ScoreState difference) {
        double score = 0;
        score += -difference.getValue(StateKeys.TARGET_HEALTH) * 4;
        return score;
    }

    @Override
    public double getReward() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        if (target == null) return 0;
        double reward = 0;
        float ourHealthRatio = bot.getVanillaPlayer().getHealth() / bot.getVanillaPlayer().getMaxHealth();
        float targetHealthRatio = target.getHealth() / target.getMaxHealth();
        reward += (targetHealthRatio - ourHealthRatio) * 3;
        reward += (double) target.invulnerableTime / target.invulnerableDuration;
        reward += Math.min(target.distanceToSqr(bot.getVanillaPlayer()) / 48, 1);

        double r = reward - prevReward;
        prevReward = reward;
        return r;
    }

    @Override
    public void tick() {
        bot.getBlade().set(ConfigKeys.TARGET, target.get());
    }

    @Override
    public String toString() {
        return String.format("kill_entity[target=%s]", Optional.ofNullable(bot.getBlade().get(ConfigKeys.TARGET)).map(Entity::getScoreboardName).orElse("null"));
    }
}
