package com.example.fragmentstyle;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yongj on 2/8/2018.
 */

public class Preferences {

    //  Logging
    private final static String TAG = "PREFS";

    //  Preference keys
    private final static String CUSTOM_COMMANDS = "CUSTOM_COMMANDS";

    //  Member variables
    private static SharedPreferences sp;

    /**
     * Read preference of a given key
     * @param c Context where preference is being read
     * @param res Resource ID of preference key
     * @return String value of read preference
     */
    public static String readPreference(Context c, int res) {
        sp = c.getSharedPreferences(CUSTOM_COMMANDS, Context.MODE_PRIVATE);
        return sp.getString(c.getString(res), "");
    }

    /**
     * Same as above, except if no value is found for preference,
     *  a default value will be returned
     * @param c Context where preference is being read
     * @param res Resource ID of preference key
     * @param defaultRes Resource ID of default preference value
     * @return String value of read preference if available, otherwise the default value
     */
    public static String readPreference(Context c, int res, int defaultRes) {
        sp = c.getSharedPreferences(CUSTOM_COMMANDS, Context.MODE_PRIVATE);
        String defaultValue = c.getResources().getString(defaultRes);
        return sp.getString(c.getString(res), defaultValue);
    }

    /**
     * Saves preference of a given key
     * @param c Context where preference is being referenced
     * @param res Resource ID of preference key
     * @param value Value to be written to preference
     */
    public static void savePreference(Context c, int res, String value) {
        sp = c.getSharedPreferences(CUSTOM_COMMANDS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(c.getString(res), value);
        editor.apply();
    }
}
