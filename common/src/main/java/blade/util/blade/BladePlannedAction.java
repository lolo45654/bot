package blade.util.blade;

import blade.debug.DebugFrame;
import blade.debug.ErrorOccurrence;
import blade.debug.ReportError;

public record BladePlannedAction<T extends BladeAction<T>>(T action) {
    public T tick(T previousAction, DebugFrame frame) {
        T action = action();
        if (action == null) return null;
        // System.out.println("doing: " + action.getClass().getSimpleName());
        if (previousAction != null && !action.equals(previousAction)) {
            ReportError.wrap(() -> previousAction.onRelease(action), frame, ErrorOccurrence.ACTION_RELEASE);
            ReportError.wrap(previousAction::prepare, frame, ErrorOccurrence.ACTION_PREPARE);
        }

        ReportError.wrap(action::onTick, frame, ErrorOccurrence.ACTION_TICK);
        ReportError.wrap(action::postTick, frame, ErrorOccurrence.ACTION_POST_TICK);
        return action;
    }
}
