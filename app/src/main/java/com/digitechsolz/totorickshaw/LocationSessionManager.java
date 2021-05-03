package com.digitechsolz.totorickshaw;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class LocationSessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "totorickloc";

    // All Shared Preferences Keys
    private static final String IS_MPLOC = "IsLocation";

    // User id (make variable public to access from outside)
    public static final String KEY_LUSERID_SES = "userId";

    // Constructor
    public LocationSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLocationSession(HashMap<String, String> loginMap) {
        // Storing login value as TRUE
        editor.putBoolean(IS_MPLOC, true);
        // Storing name in pref
        editor.putString(KEY_LUSERID_SES, loginMap.get("user_id"));
        // commit changes
        editor.commit();
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getLocationSessionDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user id
        user.put(KEY_LUSERID_SES, pref.getString(KEY_LUSERID_SES, null));
        // return user
        return user;
    }

    public void clearLocationSession(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLocationSession(){
        return pref.getBoolean(IS_MPLOC, false);
    }
}
