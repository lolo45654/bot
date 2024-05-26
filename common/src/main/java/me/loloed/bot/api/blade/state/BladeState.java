package me.loloed.bot.api.blade.state;

import me.loloed.bot.api.Bot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BladeState {
    public static final Set<StateKey> KEYS = new HashSet<>();

    private final Map<StateKey, Double> values = new HashMap<>();

    public BladeState() {
        updateValues();
    }

    public void updateValues() {
        for (StateKey key : KEYS) {
            if (values.containsKey(key)) continue;
            values.put(key, 0.0);
        }
    }

    public double getValue(StateKey key) {
        KEYS.add(key);
        return values.computeIfAbsent(key, o -> 0.0);
    }

    public void setValue(StateKey key, double value) {
        KEYS.add(key);
        values.put(key, value);
    }

    public void reset() {
        values.clear();
    }

    public BladeState copy() {
        BladeState copy = new BladeState();
        copy.values.putAll(values);
        return copy;
    }

    public BladeState difference(BladeState other) {
        BladeState diff = new BladeState();
        for (StateKey key : KEYS) {
            diff.values.put(key, getValue(key) - other.getValue(key));
        }
        return diff;
    }

    public void produce(Bot bot) {
        for (StateKey key : KEYS) {
            Double produced = key.produce(bot);
            if (produced == null) continue;
            values.put(key, produced);
        }
    }
}
