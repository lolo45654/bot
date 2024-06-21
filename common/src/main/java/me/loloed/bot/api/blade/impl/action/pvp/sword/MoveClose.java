package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MoveClose extends ScoreAction implements Sword {
    private Vec3 previousPos = null;

    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        Vec3 currentPosition = bot.getVanillaPlayer().position();
        bot.interact(false);
        bot.attack(false);
        bot.setMoveForward(true);
        bot.setSprint(true);
        if (previousPos != null && (previousPos.x() == currentPosition.x() || previousPos.z() == currentPosition.z())) {
            bot.jump();
        }
        previousPos = currentPosition;
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 closestPoint = BotMath.getClosestPoint(eyePos, target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(eyePos);
        return getSwordScore(bot) +
                (distSq <= 3 * 3 ? -8 : (Math.min(distSq / 3, 6)));
    }
}
