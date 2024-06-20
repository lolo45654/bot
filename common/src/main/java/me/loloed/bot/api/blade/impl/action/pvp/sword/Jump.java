package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public class Jump extends ScoreAction implements Sword {
    @Override
    public void onTick() {
        float time = (float) (ConfigKeys.getDifficultyReversed(bot) * 1.2);
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        Vec3 closestPoint = BotMath.getClosestPoint(bot.getVanillaPlayer().getEyePosition(), target.getBoundingBox());
        bot.setMoveForward(false);
        bot.jump();
        Vec3 direction = bot.getVanillaPlayer().position().subtract(closestPoint);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 2);
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        return getSwordScore(bot) +
                (-target.hurtTime + 20) / 10.0 +
                ThreadLocalRandom.current().nextDouble() * 0.6;
    }
}
