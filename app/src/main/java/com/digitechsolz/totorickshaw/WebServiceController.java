package com.digitechsolz.totorickshaw;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

public class WebServiceController {
    Context context;
    Dialog pDialog;
    WebInterface myInterface;

    public WebServiceController(Context context, Object obj) {
        this.context = context;
        this.myInterface = (WebInterface) obj;

    }

    public void sendRequest(String url, RequestParams params, final int flag) {
        Log.e("URL_string", url);
        showProgress();
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(60000);
        client.post(url, params, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pDialog.dismiss();
                String response = "";

                try {
                    response = new String(responseBody, "UTF-8");
                    Log.e("responseBody",response);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                myInterface.getResponse(response,flag);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                error.printStackTrace();
                pDialog.dismiss();
                myInterface.onFailure(flag);

            }

        });
    }

    public void getRequest(String url,final int flag) {
        Log.e("URL_string", url);
        showProgress();
        AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
        client.setTimeout(60000);
        client.get(url, new AsyncHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                pDialog.dismiss();
                String response = "";

                try {
                    response = new String(responseBody, "UTF-8");
                    Log.e("responseBody",response);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                myInterface.getResponse(response,flag);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                pDialog.dismiss();

                if (statusCode == 404) {
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG)
                            .show();
                } else {
                    Toast.makeText(
                            context,
                            "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]",
                            Toast.LENGTH_LONG).show();
                }

            }

        });
    }


    private void showProgress() {
        // TODO Auto-generated method stub
        pDialog = new Dialog(context, android.R.style.Theme_Translucent);
        pDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pDialog.setContentView(R.layout.yudala_progress);
        pDialog.setCancelable(false);
        pDialog.show();

    }
}
