package fm.ghinwa.previewaudioplayer.implementation.util;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TimeUnitConverterUtilTest {

    @Test
    public void testTimeUnitConverter_whenSourceIsBigger() {
        final TimeUnit sourceTimeUnit = TimeUnit.SECONDS;
        final TimeUnit resultTimeUnit = TimeUnit.MILLISECONDS;
        long result = TimeUnitConverterUtil.toResultTimeUnitLong(1.5f, sourceTimeUnit, resultTimeUnit);

        assertEquals(result, 1500);
    }

    @Test
    public void testTimeUnitConverter_whenSourceIsSmaller() {
        final TimeUnit sourceTimeUnit = TimeUnit.MILLISECONDS;
        final TimeUnit resultTimeUnit = TimeUnit.SECONDS;
        long result = TimeUnitConverterUtil.toResultTimeUnitLong(1500.5f, sourceTimeUnit, resultTimeUnit);

        assertEquals(result, 1);
    }
}