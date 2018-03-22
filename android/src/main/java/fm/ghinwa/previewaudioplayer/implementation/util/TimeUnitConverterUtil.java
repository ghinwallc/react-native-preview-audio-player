package fm.ghinwa.previewaudioplayer.implementation.util;


import java.util.concurrent.TimeUnit;

public class TimeUnitConverterUtil {
    private TimeUnitConverterUtil() {
        throw new AssertionError();
    }

    public static long toResultTimeUnitLong(float timeInSourceUnit, TimeUnit sourceTimeUnit, TimeUnit resultUnit) {
        if (resultUnit.compareTo(sourceTimeUnit) < 0) {
            long multiplier = resultUnit.convert(1, sourceTimeUnit);
            return (long) (timeInSourceUnit * multiplier);
        } else {
            long divider = sourceTimeUnit.convert(1, resultUnit);
            return (long) (timeInSourceUnit / divider);
        }
    }

    public static float toResultTimeUnitFloat(long timeInSourceUnit, TimeUnit sourceTimeUnit, TimeUnit resultUnit) {
        if (resultUnit.compareTo(sourceTimeUnit) < 0) {
            long multiplier = resultUnit.convert(1, sourceTimeUnit);
            return (float) timeInSourceUnit * multiplier;
        } else {
            long divider = sourceTimeUnit.convert(1, resultUnit);
            return (float) timeInSourceUnit / divider;
        }
    }

}
