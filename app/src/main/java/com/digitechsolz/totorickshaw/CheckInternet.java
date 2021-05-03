package com.digitechsolz.totorickshaw;

import android.content.Context;
import android.net.ConnectivityManager;

public class CheckInternet {
    Context context;

    public CheckInternet(Context context) {
        this.context = context;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
