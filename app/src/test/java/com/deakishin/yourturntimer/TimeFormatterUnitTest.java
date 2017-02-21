package com.deakishin.yourturntimer;

import com.deakishin.yourturntimer.modellayer.TimeFormatter;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Local unit test for TimeFormatter class.
 */
public class TimeFormatterUnitTest {

    @Test
    public void format_test() throws Exception {
        assertEquals(TimeFormatter.format(30000), "0:30.0");
        assertEquals(TimeFormatter.format(0), "0:00.0");
        assertEquals(TimeFormatter.format(43600, true), "0:43.6");
        assertEquals(TimeFormatter.format(43600, false), "0:43");
        assertEquals(TimeFormatter.format(124600), "2:04");
        assertEquals(TimeFormatter.format(7222000), "2:00:22");
    }

    @Test
    public void getTimeValues_test() throws Exception {
        assertArrayEquals(TimeFormatter.getTimeValues(30000), new long[]{0, 0, 30});
        assertArrayEquals(TimeFormatter.getTimeValues(0), new long[]{0, 0, 0});
        assertArrayEquals(TimeFormatter.getTimeValues(43600), new long[]{0, 0, 43});
        assertArrayEquals(TimeFormatter.getTimeValues(124600), new long[]{0, 2, 4});
        assertArrayEquals(TimeFormatter.getTimeValues(7222000), new long[]{2, 0, 22});
    }

    @Test
    public void getTimeMilliSec_test() throws Exception {
        assertEquals(TimeFormatter.getTimeMilliSec(0, 0, 30), 30000);
        assertEquals(TimeFormatter.getTimeMilliSec(0, 0, 0), 0);
        assertEquals(TimeFormatter.getTimeMilliSec(0, 0, 43), 43000);
        assertEquals(TimeFormatter.getTimeMilliSec(0, 2, 4), 124000);
        assertEquals(TimeFormatter.getTimeMilliSec(2, 0, 22), 7222000);
    }
}