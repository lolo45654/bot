package blade.debug;

import java.util.List;

public record BladeDebug(List<DebugFrame> frames) {
    public void addTick(DebugFrame tick) {
        frames.add(tick);
    }
}
