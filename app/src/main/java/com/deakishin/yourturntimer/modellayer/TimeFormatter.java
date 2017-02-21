package com.deakishin.yourturntimer.modellayer;

import android.support.annotation.NonNull;

/**
 * Created by Dmitry Akishin on 22.01.2017.
 * Helper class for formatting time from long to String.
 */

public class TimeFormatter {

    /**
     * Converts time in milliseconds to String representation in given format:
     * hh:mm:ss or or m:ss or 0:ss.d or 0:ss (choice between last two
     * is determined by parameter addDecSec).
     *
     * @param milliSec  time in milliseconds.
     * @param addDecSec if true and time in under minute then
     *                  format would be 0:ss.d; otherwise 0:ss.
     * @return String representation of the given time.
     */
    public static String format(long milliSec, boolean addDecSec) {
        long[] values = getTimeValues(milliSec);
        long hours = values[0];
        long minutes = values[1];
        long seconds = values[2];
        long decSec = milliSec / 100 - 10 * seconds - minutes * 60 * 10 - hours * 60 * 60 * 10;

        StringBuilder sb = new StringBuilder();
        if (hours == 0) {
            if (minutes == 0) {
                sb.append("0:")
                        .append(seconds < 10 ? "0" : "").append(seconds);
                if (addDecSec) {
                    sb.append(".").append(decSec);
                }
            } else {
                sb.append(minutes).append(":")
                        .append(seconds < 10 ? "0" : "").append(seconds);
            }
        } else {
            sb.append(hours).append(":")
                    .append(minutes < 10 ? "0" : "").append(minutes).append(":")
                    .append(seconds < 10 ? "0" : "").append(seconds);
        }

        return sb.toString();
    }

    /**
     * Converts time in milliseconds to String representation in given format:
     * hh:mm:ss or 0:ss.d.
     *
     * @param milliSec time in milliseconds.
     * @return String representation of the given time.
     */
    public static String format(long milliSec) {
        return format(milliSec, true);
    }

    /**
     * Returns hours, minutes and seconds for given time.
     *
     * @param milliSec time in milliseconds.
     * @return array of long values for hours, minutes, seconds (in this order).
     */
    public static long[] getTimeValues(long milliSec) {
        long hours = milliSec / 1000 / 60 / 60;
        long minutes = milliSec / 1000 / 60 - hours * 60;
        long seconds = milliSec / 1000 - hours * 60 * 60 - minutes * 60;

        return new long[]{hours, minutes, seconds};
    }

    /**
     * Returns time in milliseconds composed by hours, minutes and seconds.
     *
     * @param hours   hours.
     * @param minutes minutes.
     * @param seconds seconds.
     * @return time in milliseconds.
     */
    public static long getTimeMilliSec(long hours, long minutes, long seconds) {
        return 1000 * (seconds + 60 * (minutes + 60 * hours));
    }
}
