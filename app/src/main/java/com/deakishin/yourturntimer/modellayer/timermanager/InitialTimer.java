package com.deakishin.yourturntimer.modellayer.timermanager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dmitry Akishin on 21.01.2017.
 * <p>
 * An initial timer with its own name and starting time.
 * Values of the InitialTimer object are used to initiate and start timers.
 */

public class InitialTimer {

    /**
     * Maximum value for hours.
     */
    public static final int MAX_HOURS = 99;

    /* Keys for constructing JSON object. */
    private static final String JSON_NAME = "name";
    private static final String JSON_TIME = "time";

    /* Name. */
    private String mName;
    /* Initial time in milliseconds. */
    private long mTime;

    /**
     * Constructs object by its values.
     *
     * @param name timer's name.
     * @param time timer's time in milliseconds.
     */
    public InitialTimer(String name, long time) {
        setName(name);
        setTime(time);
    }

    /**
     * Constructs object by JSON object.
     *
     * @param json JSON object.
     * @throws JSONException if unable to construct object due to
     *                       invalid JSON object.
     */
    public InitialTimer(JSONObject json) throws JSONException {
        if (json == null) {
            throw new JSONException("Null JSONObject.");
        }
        setName(json.getString(JSON_NAME));
        setTime(json.getLong(JSON_TIME));
    }

    /**
     * Creates and returns copy of current object.
     *
     * @return copy of current object.
     */
    public InitialTimer copy() {
        return new InitialTimer(mName, mTime);
    }

    /**
     * Sets data from another timer.
     *
     * @param timer timer to get data from.
     */
    public void setData(InitialTimer timer) {
        if (timer == null) {
            return;
        }
        setTime(timer.getTime());
        setName(timer.getName());
    }

    /**
     * Converts object to JSON presentation.
     *
     * @return JSON object.
     * @throws JSONException if unable to convert to JSON.
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_NAME, mName);
        json.put(JSON_TIME, mTime);
        return json;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }
}
