package me.loloed.bot.api.blade.debug;

import java.util.ArrayList;
import java.util.List;

public class BladeDebug {
    // add json from and to for file saving
    private List<DebugFrame> frames = new ArrayList<>();

    public void addTick(DebugFrame tick) {
        frames.add(tick);
    }

    public List<DebugFrame> getFrames() {
        return frames;
    }

    public DebugFrame newFrame() {
        DebugFrame tick = new DebugFrame();
        addTick(tick);
        return tick;
    }
}
