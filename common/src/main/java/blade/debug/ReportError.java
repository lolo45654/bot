package blade.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ReportError {
    public static ReportError from(Throwable throwable, ErrorOccurrence occurrence) {
        ReportError error = new ReportError();
        error.setMessage(throwable.getMessage());
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        throwable.printStackTrace();
        error.setStacktrace(writer.toString());
        error.setOccurrence(occurrence);
        return error;
    }

    public static ReportError from(Throwable throwable) {
        return from(throwable, ErrorOccurrence.OTHER);
    }

    public static void wrap(Runnable runnable, DebugFrame frame, ErrorOccurrence occurrence) {
        try {
            runnable.run();
        } catch (Throwable throwable) {
            frame.addError(from(throwable, occurrence));
        }
    }

    private String message;
    private String stacktrace;
    private ErrorOccurrence occurrence;

    public String getMessage() {
        return message;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }

    public ErrorOccurrence getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(ErrorOccurrence occurrence) {
        this.occurrence = occurrence;
    }
}
