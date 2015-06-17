package edu.duke.pratt.hal.triangletraffic.utility;

import android.content.SharedPreferences;

import java.util.ArrayList;

public class TTPref {

    private static ArrayList<SharedPreferences> sharedPreferencesArrayList = new ArrayList<>();

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        if (sharedPreferencesArrayList.size() == 0) {
            sharedPreferencesArrayList.add(sharedPreferences);
        }
    }

    public static double getRadiusMeters() {
        SharedPreferences sharedPref = getSharedPref();
        return 1609.34 * Double.parseDouble(sharedPref.getString("radius_list", "0"));
    }

    public static long getTimeMillis() {
        SharedPreferences sharedPref = getSharedPref();
        long time = (long) Integer.parseInt(sharedPref.getString("timing_list", "60"));
        return time * 60 * 1000;

    }

    public static boolean textMode() {
        return getSharedPref().getBoolean("text_mode", true);
    }

    public static boolean audioMode() {
        return getSharedPref().getBoolean("audio_mode", true);
    }

    public static boolean vibrateMode() {
        return getSharedPref().getBoolean("vibrate_mode", true);
    }

    public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPref().registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getSharedPref().unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static SharedPreferences getSharedPref() {
        return sharedPreferencesArrayList.get(0);
    }

}
