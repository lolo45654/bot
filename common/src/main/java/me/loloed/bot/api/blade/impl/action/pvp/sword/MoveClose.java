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
        float time = (float) (ConfigKeys.getDifficultyReversed(bot) * 1.2);
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 closestPoint = BotMath.getClosestPoint(bot.getVanillaPlayer().getEyePosition(), target.getBoundingBox());
        Vec3 currentPosition = bot.getVanillaPlayer().position();
        Vec3 direction = currentPosition.subtract(closestPoint);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 2);

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
        Vec3 closestPoint = BotMath.getClosestPoint(bot.getVanillaPlayer().getEyePosition(), target.getBoundingBox());
        double distSq = closestPoint.distanceToSqr(bot.getVanillaPlayer().getEyePosition());
        return getSwordScore(bot) +
                (distSq <= 3 * 3 ? -8 : (Math.min(distSq / 6, 4)));
    }
}
