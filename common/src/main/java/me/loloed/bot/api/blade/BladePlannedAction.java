package me.loloed.bot.api.blade;

import me.loloed.bot.api.blade.debug.DebugFrame;
import me.loloed.bot.api.blade.debug.ErrorOccurrence;
import me.loloed.bot.api.blade.debug.ReportError;

public record BladePlannedAction<T extends BladeAction<T>>(T action) {
    public T tick(T previousAction, DebugFrame frame) {
        T action = action();
        if (action == null) return null;
        System.out.println("doing: " + action.getClass().getSimpleName());
        if (previousAction != null && !action.equals(previousAction)) {
            ReportError.wrap(() -> previousAction.onRelease(action), frame, ErrorOccurrence.ACTION_RELEASE);
            ReportError.wrap(previousAction::prepare, frame, ErrorOccurrence.ACTION_PREPARE);
        }

        ReportError.wrap(action::onTick, frame, ErrorOccurrence.ACTION_TICK);
        ReportError.wrap(action::postTick, frame, ErrorOccurrence.ACTION_POST_TICK);
        return action;
    }
}
