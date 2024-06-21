package me.loloed.bot.api.blade.impl.action.pvp.sword;

import me.loloed.bot.api.blade.impl.ConfigKeys;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.util.BotMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public class STap extends ScoreAction implements Sword {
    @Override
    public void onTick() {
        lookAtEnemy(bot, tick);

        bot.setMoveBackward(true);
        bot.setMoveForward(false);
    }

    @Override
    public void getResult(BladeState result) {

    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        return getSwordScore(bot) +
                target.hurtTime / 10.0 +
                ThreadLocalRandom.current().nextDouble() * 0.6;
    }
}
