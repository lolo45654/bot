package me.loloed.bot.api.blade.debug.planner;

import me.loloed.bot.api.blade.debug.DebugPlanner;
import me.loloed.bot.api.blade.planner.score.ScoreAction;
import me.loloed.bot.api.blade.planner.score.ScorePlanner;

import java.util.HashMap;
import java.util.Map;

public class ScoreDebug extends DebugPlanner {
    private double temperature;
    private ScoreAction actionTaken;
    private Map<ScoreAction, ScorePlanner.Score> scores = new HashMap<>();

    public Map<ScoreAction, ScorePlanner.Score> getScores() {
        return scores;
    }

    public void setScores(Map<ScoreAction, ScorePlanner.Score> scores) {
        this.scores = scores;
    }

    public void addScore(ScoreAction action, ScorePlanner.Score score) {
        scores.put(action, score);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public ScoreAction getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(ScoreAction actionTaken) {
        this.actionTaken = actionTaken;
    }
}
