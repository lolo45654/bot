package me.loloed.bot.api.blade.impl.action.travel;

import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.value.AdaptingValue;
import me.loloed.bot.api.blade.state.value.LessThanValue;
import me.loloed.bot.api.entity.Entity;
import me.loloed.bot.api.util.Position;
import me.loloed.bot.api.util.Vector;

import java.util.Optional;

public class MoveTowards extends AStarAction {
    private final ProducingKey<Double> distanceSquared = EntityTarget.TARGET.getDefaultValue().getPosition().distanceSquared(results.getBot().getPosition());
    private Vector previousPos = null;

    @Override
    public void prepare() {
        results.set(distanceSquared, new AdaptingValue<>(0.0, that -> {
            if (!(that instanceof LessThanValue<Double> lessThan)) return 0.0;
            Double val = lessThan.getValue();
            return distanceSquared.getOptionalValue(state).orElse(0.0) > val ? val - 1.0 : val + 1.0;
        }));
        expectProduced(distanceSquared);
    }

    @Override
    public void onTick() {
        StateEntity target = EntityTarget.TARGET.get(state).getValue();
        Optional<Entity> entityOpt = target.toRealEntity(bot, state);
        if (entityOpt.isEmpty()) return;
        bot.lookAt(entityOpt.get().getBoundingBox().getClosestPosition(bot.getHeadPosition()));
        bot.interact(false);
        bot.attack(false);
        bot.setMoveForward(true);
        bot.setSprint(true);
        Position currentPosition = bot.getPosition();
        if (previousPos != null && (previousPos.getX() == currentPosition.getX() || previousPos.getZ() == currentPosition.getZ())) {
            bot.jump();
        }
        previousPos = currentPosition;
    }

    @Override
    public double getCost() {
        return distanceSquared.getOptionalValue(state).orElse(0.0) / 8;
    }

    @Override
    public void onRelease(AStarAction next) {
        super.onRelease(next);
        bot.setMoveForward(false);
        bot.setSprint(false);
        previousPos = null;
    }
}
