package com.example.stockup;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class AsyncStockDownloader extends AsyncTask<String, Void, String> {
    private static final String FINANCIAL_DATA = "https://cloud.iexapis.com/stable/stock/";
    private static final String myAPIKey = "pk_e356d021c8d24ebfb6f5f3961e1138d8";
    private String userSearch;
    private MainActivity ma;


    public AsyncStockDownloader(MainActivity ma, String userSearch) {
        this.ma = ma;
        this.userSearch = userSearch;
    }


    //async starts here
    @Override
    protected void onPreExecute() {
        //make toast to show for 1/4 of a second
        final Toast toast = Toast.makeText(ma, "Searching...", Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 250);
    }


    //async moves from onPreExecute to here
    @Override
    protected String doInBackground(String... params) {
        Uri stockUri = Uri.parse(FINANCIAL_DATA);
        String urlToUse = stockUri.toString() + userSearch + "/quote?token=" + myAPIKey; //normalize url we're about to connect

        StringBuilder sb = new StringBuilder(); //to add results below


        //try to talk to stock API to get real time updates
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            InputStream input = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line;
            while((line = reader.readLine()) != null) { //while there is JSON line to read
                sb.append(line).append("\n"); //append to stringbuilder
            }

            Log.d(TAG, "Stringbuilder: " + sb.toString()); //stringbuilder obj contains all JSON info passed from API

        } catch (Exception e) { //catch exception
            Log.e(TAG, "doInBackground ERROR: ", e);
            e.printStackTrace();
        }

        //convert stringbuilder to string to return
        return sb.toString();
    }


    //called after doInBackground() is done
    @Override
    protected void onPostExecute(String s) {
        Stock stock = parseJSON(s); //parse string passed down from doInBackground() to return Stock obj w/ needed stock info
        ma.updateData(stock); //finally send to updateData() method in MainActivity to update adapter and database w/ current data parsed
    }


    //parses string of JSON objects & gets stock info
    public Stock parseJSON(String s)  {
        String symbol = "";
        String name = "";
        double price = 0;
        double change = 0;
        double changePercentage = 0;

        try {
            JSONObject jStock = new JSONObject(s); //JSONObject called bc JSON gotten from API is in object format

            symbol = jStock.getString("symbol");
            name = jStock.getString("companyName");

            price = Double.parseDouble(jStock.getString("latestPrice"));
            String p = String.format("%.2f", price);
            price = Double.parseDouble(p);

            //use try catch incase price change is null value
            //to stop program from crashing
            try {
                change = Double.parseDouble(jStock.getString("change"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String c = String.format("%.2f", change);
            change = Double.parseDouble(c);


            //use try catch incase price changePercentage is null value
            //to stop program from crashing
            try {
                changePercentage = Double.parseDouble(jStock.getString("changePercent"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String cp = String.format("%.2f", changePercentage);
            changePercentage = Double.parseDouble(cp);

            return new Stock(symbol, name, price, change, changePercentage); //return stock user wanted
        } catch (JSONException e) { //catch exception
            Log.d(TAG, "parseJSON ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
