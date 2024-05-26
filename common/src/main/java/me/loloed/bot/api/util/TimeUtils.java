package me.loloed.bot.api.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static long convertDurationToTicks(Duration duration) {
        return (long) Math.floor(TimeUnit.MILLISECONDS.convert(duration) / 20f);
    }
}
