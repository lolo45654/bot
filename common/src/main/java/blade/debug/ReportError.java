package blade.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

public record ReportError(String message, String stacktrace) {
    public static ReportError from(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return new ReportError(throwable.getMessage(), writer.toString());
    }
}
