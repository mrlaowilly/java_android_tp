package com.example.background_tp.restClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.background_tp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class AsyncRestClient extends AsyncTask<Pair<String, String>, Void, JSONObject> {

    protected String error = new String();
    protected ProgressBar progressBar;
    protected Context context;

    // -------------- start Listener ---------------

    private  OnReceiveDataListener onReceiveDataListener;

    public interface OnReceiveDataListener {
        public void onReceiveData(JSONObject jsonObject);
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener){
        this.onReceiveDataListener = onReceiveDataListener;
    }

    // -------------- End Listener -----------------

    public AsyncRestClient(Context context, ProgressBar progressBar){
        this.progressBar = progressBar;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressBar.animate().alpha(1.0f).setDuration(500);

    }

    @Override
    protected JSONObject doInBackground(Pair<String, String>... pairs) {

        JSONObject result = new JSONObject();
        String query_body = new String();
        String query_url = null;
        String query_method = null;

        for (int i=0; i<pairs.length; i++){
            if (pairs[i].first.equals("HTTP_URL")){
                query_url = pairs[i].second;
                continue;
            }
            if (pairs[i].first.equals("HTTP_METHOD")){
                query_method = pairs[i].second.toUpperCase();
                continue;
            }

            if (!query_body.isEmpty()) {
                query_body +="&";
            }

            try {
                query_body += URLEncoder.encode(pairs[i].first, "UTF-8") + "=" + URLEncoder.encode(pairs[i].second, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                error = context.getString(R.string.error_encode);
            }

            // Log.e(">>>>>> arc", query_url);
            // Log.e(">>>>>> arc", query_method);
            // Log.e(">>>>>> arc", query_body);
        }

        try {
            URL url = new URL(query_url);
            if (query_method.equals("GET") || query_method.equals("DELETE")){
                url = new URL(query_url + "?" + query_body);
            }

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestMethod(query_method);
            httpsURLConnection.setInstanceFollowRedirects(false);
            httpsURLConnection.setConnectTimeout(5000);
            httpsURLConnection.setReadTimeout(5000);
            httpsURLConnection.setUseCaches(false);

            if (!query_method.equals("GET") && !query_method.equals("DELETE")) {
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setDoInput(true);
                OutputStream outputStream = httpsURLConnection.getOutputStream();
                outputStream.write(query_body.getBytes("UTF-8"));
                outputStream.close();
            }

            InputStream inputStream = httpsURLConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
            String response = scanner.hasNext() ? scanner.next() : "{}";
            scanner.close();
            inputStream.close();

            Log.e(">>>>>> arc", response);
            result = new JSONObject(response);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            error = context.getString(R.string.error_url);
        } catch (IOException e) {
            e.printStackTrace();
            error = context.getString(R.string.error_network);
        } catch (JSONException e) {
            e.printStackTrace();
            error = context.getString(R.string.error_response);
        }

        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (!error.isEmpty()){
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }

        if (onReceiveDataListener!=null){
            onReceiveDataListener.onReceiveData(jsonObject);
        }
        progressBar.animate().alpha(0.0f).setDuration(500);
    }
}
