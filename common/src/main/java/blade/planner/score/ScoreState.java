package blade.planner.score;

import blade.Bot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreState {
    private final Map<StateKey, Double> values = new HashMap<>();
    private final Set<StateKey> keys = new HashSet<>();

    public ScoreState() {
        updateValues();
    }

    public void updateValues() {
        for (StateKey key : keys) {
            if (values.containsKey(key)) continue;
            values.put(key, 0.0);
        }
    }

    public double getValue(StateKey key) {
        keys.add(key);
        return values.computeIfAbsent(key, o -> 0.0);
    }

    public void setValue(StateKey key, double value) {
        keys.add(key);
        values.put(key, value);
    }

    public void reset() {
        values.clear();
    }

    public ScoreState copy() {
        ScoreState copy = new ScoreState();
        copy.values.putAll(values);
        return copy;
    }

    public ScoreState difference(ScoreState other) {
        ScoreState diff = new ScoreState();
        for (StateKey key : keys) {
            diff.values.put(key, getValue(key) - other.getValue(key));
        }
        return diff;
    }

    public void produce(Bot bot) {
        for (StateKey key : keys) {
            Double produced = key.produce(bot);
            if (produced == null) continue;
            values.put(key, produced);
        }
    }

    public Set<StateKey> getKeys() {
        return keys;
    }
}
