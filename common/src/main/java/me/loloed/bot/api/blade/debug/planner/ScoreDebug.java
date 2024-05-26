package me.loloed.bot.api.blade.debug.planner;

import me.loloed.bot.api.blade.debug.DebugPlanner;
import me.loloed.bot.api.blade.planner.score.ScoreAction;

import java.util.HashMap;
import java.util.Map;

public class ScoreDebug extends DebugPlanner {
    private double temperature;
    private Map<ScoreAction, Double> scores = new HashMap<>();

    public Map<ScoreAction, Double> getScores() {
        return scores;
    }

    public void setScores(Map<ScoreAction, Double> scores) {
        this.scores = scores;
    }

    public void addScore(ScoreAction action, double score) {
        scores.put(action, score);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
