package com.example.stockup;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.nio.Buffer;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class AsyncNameDownloader extends AsyncTask<String, Integer, String> {
    private MainActivity ma;
    private String userSearch;
    private final String dataURL = "https://api.iextrading.com/1.0/ref-data/symbols";


    public AsyncNameDownloader(MainActivity ma, String userSearch) {
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
    protected String doInBackground(String... strings) {
        Uri dataUri = Uri.parse(dataURL);
        String urlToUse = dataUri.toString();

        Log.d(TAG, "URL: " + urlToUse); //check to see if url is accessed

        StringBuilder sb = new StringBuilder();

        //try to talk to API to get stock symbol and name
        try{
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            InputStream input = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String line;
            while((line = reader.readLine()) != null) { //while there is a JSON line to read
                sb.append(line).append("\n"); //append to stringbuilder
            }

            Log.d(TAG, "Stringbuilder: " + sb.toString()); //stringbuilder obj contains all JSON info passed from API

        } catch (Exception e) { //catch if exception thrown
            Log.e(TAG, "doInBackground ERROR: ", e);
            e.printStackTrace();
        }

        //convert stringbuilder to string to return
        return sb.toString();
    }


    //called after doInBackground() is done
    @Override
    protected void onPostExecute(String s) {
        ArrayList<TempStock> stockList = parseJSON(s); //parse string passed down from doInBackground() to return Stock obj w/ symbol and name
                                                        //this is used to display multiple stocks w/ same name in it

        //if stockList w/ stringbuilder elements from doInBackground() is NOT empty,
        if(stockList.size() != 0) {
            Toast.makeText(ma, stockList.size() + " stock(s) found", Toast.LENGTH_SHORT).show();
        }

        //return stockList to mainActivity's searchResult to evaluate list and do sth w/ it
        ma.searchResult(stockList);
    }


    //parses string of JSON strings & finds stock user wants
    public ArrayList<TempStock> parseJSON(String s) {
        ArrayList<TempStock> stockList = new ArrayList<>();
        ArrayList<TempStock> foundStock = new ArrayList<>();

        try {
            JSONArray jArray = new JSONArray(s); //JSONArray called bc JSON gotten from API is in array format

            //extracts stock symbol and name from JSON string to make tempStock obj
            for(int i = 0; i < jArray.length(); i++) {
                JSONObject jStock = (JSONObject)jArray.get(i);

                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");


                //make a list of tempStocks w/ symbol and name of every stock existing from API
                stockList.add(new TempStock(symbol, name));
            }

            //searches for speceific stock user wants by using stockList of tempStocks made previously
            for(int i = 0; i < stockList.size(); i++) {
                if(userSearch.equals(stockList.get(i).getSymbol()) //if symbol matches w/ userSearch
                        || stockList.get(i).getName().toLowerCase().contains(userSearch.toLowerCase())) { //or if stock name has the userSearch
                    foundStock.add(stockList.get(i));
                }
            }
            return foundStock; //only return stocks that match w/ user's search criteria
        } catch (JSONException e) { //catch exceptions
            Log.d(TAG, "parseJSON ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
