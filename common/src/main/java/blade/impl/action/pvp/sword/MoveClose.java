package blade.impl.action.pvp.sword;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MoveClose extends BladeAction implements Sword {
    private Vec3 previousPos = null;

    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        Vec3 currentPosition = bot.getVanillaPlayer().position();
        bot.interact(false);
        bot.attack(false);
        bot.setMoveForward(true);
        bot.setMoveBackward(false);
        bot.setSprint(true);
        if (previousPos != null && (previousPos.x() == currentPosition.x() || previousPos.z() == currentPosition.z())) {
            bot.jump();
        }
        previousPos = currentPosition;
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void getResult(ScoreState result) {
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
                (distSq <= reach * reach ? -8 : (Math.min(distSq / 16, 6)));
    }
}
