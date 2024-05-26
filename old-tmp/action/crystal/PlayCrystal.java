package me.loloed.bot.api.blade.impl.action.crystal;

import me.loloed.bot.api.blade.BladePlannedAction;
import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.planner.score.ScorePlanner;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.value.LessThanValue;
import me.loloed.bot.api.material.MaterialFlag;

public class PlayCrystal extends AStarAction {
    private final ScorePlanner scorePlanner = new ScorePlanner() {{
        addAction(new HitEnemy());
        addAction(new PlaceObsidian());
        addAction(new Pearl());
    }};
    private ScoreAction lastAction = null;

    @Override
    public void prepare() {
        StateEntity target = EntityTarget.TARGET.getDefaultValue();
        ProducingKey<Double> distanceSquared = target.getPosition().distanceSquared(expectations.getBot().getPosition());
        expectations.set(distanceSquared, new LessThanValue<>(3.0 * 3.0));
        expectProduced(distanceSquared);

        ProducingKey<Boolean> hasCrystal = expectations.getInventory().hasItem(MaterialFlag.CRYSTAL);
        ProducingKey<Boolean> hasObsidian = expectations.getInventory().hasItem(MaterialFlag.CRYSTAL_PLACEABLE);
        ProducingKey<Boolean> hasAnchor = expectations.getInventory().hasItem(MaterialFlag.RESPAWN_ANCHOR);
        ProducingKey<Boolean> hasGlowstone = expectations.getInventory().hasItem(MaterialFlag.CHARGE_RESPAWN_ANCHOR);
        expectations.set(hasCrystal, true);
        expectations.set(hasObsidian, true);
        expectations.set(hasAnchor, true);
        expectations.set(hasGlowstone, true);
        expectProduced(hasCrystal);
        expectProduced(hasObsidian);
        expectProduced(hasAnchor);
        expectProduced(hasGlowstone);

        results.set(target.isDead(), true);
    }

    @Override
    public void onTick() {
        Double difficulty = Difficulty.DIFFICULTY.getValue(state);
        scorePlanner.setTemperature((1 - difficulty) * 0.8);
        scorePlanner.refreshAll(bot, state);
        BladePlannedAction<ScoreAction> plan = scorePlanner.plan(state, parentDebugPlanner);
        plan.tick(lastAction, bot.getBlade().getFrame());
    }

    @Override
    public double getCost() {
        return 7;
    }

    @Override
    public void onRelease(AStarAction next) {
        super.onRelease(next);
        if (lastAction != null) {
            lastAction.onRelease(null);
            lastAction = null;
        }
    }
}
