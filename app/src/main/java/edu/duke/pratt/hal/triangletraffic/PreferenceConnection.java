package edu.duke.pratt.hal.triangletraffic;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import edu.duke.pratt.hal.triangletraffic.utility.TTPref;

public class PreferenceConnection {

    public PreferenceConnection(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        TTPref.setSharedPreferences(sharedPref);
    }
}
