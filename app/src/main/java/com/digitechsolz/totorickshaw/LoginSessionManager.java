package com.digitechsolz.totorickshaw;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class LoginSessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "totorick";

    // All Shared Preferences Keys
    private static final String IS_MPLOG = "IsLogin";

    // User id (make variable public to access from outside)
    public static final String KEY_USERID_SES = "userId";

    // Constructor
    public LoginSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(HashMap<String, String> loginMap) {
        // Storing login value as TRUE
        editor.putBoolean(IS_MPLOG, true);
        // Storing name in pref
        editor.putString(KEY_USERID_SES, loginMap.get("user_id"));
        // commit changes
        editor.commit();
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getLoginSessionDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user id
        user.put(KEY_USERID_SES, pref.getString(KEY_USERID_SES, null));
        // return user
        return user;
    }

    public void clearLoginSession(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoginSession(){
        return pref.getBoolean(IS_MPLOG, false);
    }
}
