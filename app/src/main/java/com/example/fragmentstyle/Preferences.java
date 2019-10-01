package com.example.fragmentstyle;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.example.fragmentstyle.Constants.IMAGE_KEY;

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

    public static void saveHashMap(Context c, String key, Map myHash){
        sp = c.getSharedPreferences(CUSTOM_COMMANDS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Type hashType = new TypeToken<Map<Integer, Point>>() {}.getType();
        Gson gson = new Gson();
        String json = gson.toJson(myHash, hashType);
        editor.putString(key, json);
        editor.apply();
    }
    public static Map getHashMap(Context c,String key){
        sp = c.getSharedPreferences(CUSTOM_COMMANDS, Context.MODE_PRIVATE);
        Type hashType = new TypeToken<Map<Integer, Point>>() {}.getType();
        Gson gson = new Gson();
        String json = sp.getString(key,null);
        Map myMap = gson.fromJson(json, hashType);
        return myMap;
    }

    public static void removeHashMap (Context c){
        sp = c.getSharedPreferences(CUSTOM_COMMANDS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(IMAGE_KEY);
        editor.apply();
    }
}