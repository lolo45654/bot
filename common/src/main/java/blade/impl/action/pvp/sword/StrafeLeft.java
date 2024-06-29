package blade.impl.action.pvp.sword;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import blade.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public class StrafeLeft extends ScoreAction implements Sword {
    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        bot.setMoveLeft(true);
        bot.setMoveRight(false);
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        double reach = getReach(bot);

        return getSwordScore(bot) +
                (distSq <= reach * reach ? -8 : (distSq > 12 * 12 ? -8 : Math.min(distSq / (12 * 12), 6))) +
                Math.min(tick / 3.0, 1.4) +
                ThreadLocalRandom.current().nextDouble() * 0.6;
    }

    @Override
    public void onRelease(ScoreAction next) {
        bot.setMoveLeft(false);
    }
}
