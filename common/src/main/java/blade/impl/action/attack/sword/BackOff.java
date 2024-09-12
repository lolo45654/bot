package blade.impl.action.attack.sword;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isAttackSatisfied;
import static blade.impl.action.attack.Attack.lookAtEnemy;

public class BackOff extends BladeAction implements Sword {
    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        bot.setMoveBackward(true);
        bot.setMoveForward(false);
    }

    @Override
    public boolean isSatisfied() {
        return isAttackSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = target.getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, bot.getVanillaPlayer().getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        double range = 3.0;
        if (target instanceof Player player) range = player.entityInteractionRange();
        range -= ConfigKeys.getDifficultyReversedCubic(bot) * 1.5;

        return state.getValue(StateKeys.SWORD_MODE) +
                state.getValue(StateKeys.RECENTLY_HIT_ENEMY) / 2 +
                Math.min((range - Math.min(distSq / (range * range), range)) / range, 1.0) +
                bot.getRandom().nextDouble() * 0.3;
    }

    @Override
    public void onRelease(BladeAction next) {
        super.onRelease(next);
        bot.setMoveBackward(false);
        bot.setMoveForward(false);
    }
}
