package com.example.stockup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;


import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {
    Toolbar toolbar;
    RecyclerView recyclerView;
    private List<Stock> stocks;
    private Adapter adapter;
    SwipeRefreshLayout refreshLayout;
    public String userSearch;
    StockDatabase db;
    SearchView searchView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up toolbar
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("StockUP");


        //set up data storage and retrieval
        stocks = new ArrayList<Stock>();
        db = new StockDatabase(this);
        List<Stock> list = db.getStocks();
        stocks.addAll(list);


        //setup recyclerview and adapter to display stocks on list
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        adapter = new Adapter(this, stocks);
        //Log.d("item ", "count " + adapter.getItemCount());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        //refresh stock info every time user swipes down
        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

    }

    //creates and inflates options menu with add icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);
        return true;
    }

    //when an item on options menu is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //if add button is pressed
        if (item.getItemId() == R.id.addStock) {
            if (!amConnected()) { //if phone is not connected to internet

                //create alertbox to display alert message
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please connect to internet");
                builder.setTitle("No Network Connection");

                AlertDialog dialog = builder.create();
                dialog.show();

            } else { //if phone is connected to internet
                //create alertbox for user to enter stock symbole
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Stock Search");

                builder.setMessage("Not sure which stock? Press OK");


                final EditText stockInput = new EditText(this);


                //whatever user types in becomes capitalized and centered
                stockInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                stockInput.setGravity(Gravity.CENTER_HORIZONTAL);
                stockInput.setFilters(new InputFilter[]{new InputFilter.AllCaps()});


                builder.setView(stockInput);

                //make ok button on alertbox
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userSearch = stockInput.getText().toString(); //store user's input in userSearch
                        new AsyncNameDownloader(MainActivity.this, userSearch).execute(); //all asynctask to use internet and evaluate search
                    }
                });
                //make cancel button on alertbox
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancels
                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
            }
            return super.onOptionsItemSelected(item);
        }

        //if sort button is pressed
        if(item.getItemId() == R.id.sort) {
            final String[] sortBy = new String[3];
            sortBy[0] = "Name";
            sortBy[1] = "Symbol";
            sortBy[2] = "Price";

            //create alertbox for user to enter which type of sorting it wants to do
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setItems(sortBy, new DialogInterface.OnClickListener() { //sets list of options
                @Override
                public void onClick(DialogInterface dialog, int which) { //when clicked on a stock
                    String myChoice = sortBy[which]; //get sort option

                    //use QuickSort to sort list according to user's criteria
                    if(myChoice.equals("Name")) {
                        QuickSortName QS = new QuickSortName(stocks);
                        QS.sort(stocks, 0, stocks.size() - 1);
                        adapter.notifyDataSetChanged();
                    }

                    if(myChoice.equals("Symbol")) {
                        QuickSortSymbol QS = new QuickSortSymbol(stocks);
                        QS.sort(stocks, 0, stocks.size() - 1);
                        adapter.notifyDataSetChanged();
                    }

                    if(myChoice.equals("Price")) {
                        QuickSortPrice QS = new QuickSortPrice(stocks);
                        QS.sort(stocks, 0, stocks.size() - 1);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            builder.setTitle("Sort By");

            AlertDialog dialog = builder.create();
            dialog.show();
        }


        //if search button is pressed
        if(item.getItemId() == R.id.search) {
            //set up new toolbar for searching
            searchView = (SearchView)item.getActionView();
            searchView.onActionViewExpanded(); //expands search bar
            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE); //sets keyboard icon to done after user types
            searchView.setQueryHint("Search...");


            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                //does stuff after text submitted
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                //does stuff in realtime
                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return true;
                }
            });

        }


        //this clears search bar when user presses back button
        item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
            //turns search bar filter blank when back button pressed
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                adapter.getFilter().filter("");
                return true;
            }
        });
        return true;
    }



    public void doRefresh() {
        if(!amConnected()) { //if no internet --> cannot connect
            //create alertbox
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please connect to internet");
            builder.setTitle("No Network Connection");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else { //else refresh and change content accordingly
            List<Stock> temp = new ArrayList<>(stocks); //create temp list of current stocks

            //go thru list of current stocks and delete all
            for(int i = stocks.size() - 1; i >= 0; i--) {
                Stock s = stocks.get(i);
                db.deleteStock(s.getSymbol());
                stocks.remove(i);
                adapter.searchAllStocks.remove(i);
            }

            stocks.clear();

            //go thru temp stocks list from before and add them back with Async req
            for(int i = temp.size() - 1; i >= 0; i--) {
                Stock s = temp.get(i);
                userSearch = s.getSymbol();
                AsyncStockDownloader asd = new AsyncStockDownloader(this, userSearch);
                asd.execute();
            }

            //notify adapter to redraw items
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false); //stop refreshing
        }
    }

    //check if you're connected to internet
    public boolean amConnected() {
        ConnectivityManager conn = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conn.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }


    //responds to whatever is passed on from AsyncTasks
    public void searchResult(ArrayList<TempStock> stocks) {

        //if nothing inside stocks list then output search failed
        if(stocks.size() == 0) {
            //create alert box
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stock " + userSearch + " does not exist!");
            builder.setTitle("Search Failed");

            AlertDialog dialog = builder.create();
            dialog.show();
        }


        //if one stock inside stocks list then use AsyncStockDownloader to get all info on it
        else if(stocks.size() == 1) {
            new AsyncStockDownloader(MainActivity.this, stocks.get(0).getSymbol()).execute();
        }


        //if stocks list had multiple stocks in it
        else {
            final String[] stockArray = new String[stocks.size()];

            for(int i = 0; i < stockArray.length; i++) { //store stocks in string array to display on alertbox
                stockArray[i] = stocks.get(i).getSymbol() + " | " + stocks.get(i).getName();
            }

                //create alertbox to display multiple stock options to choose from
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose your stock");

                builder.setItems(stockArray, new DialogInterface.OnClickListener() { //sets list of stocks
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //when clicked on a stock
                        String myChoice = stockArray[which]; //get stock we chose from list
                        int pos = myChoice.indexOf("|"); //get index of | on list
                        userSearch = myChoice.substring(0, pos - 1); //get the symbol name to the left of |

                        new AsyncStockDownloader(MainActivity.this, userSearch).execute(); //get all info for that stock from API via Async class
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { //sets cancel button
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancelled
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);
        }
    }

    //update Stock list on screen w/ info passed from Async class AsyncStockDownloader
    public void updateData(Stock stock) {
        boolean flag = false;

        //check for duplicate stocks on list
        for(int i = 0; i < stocks.size(); i++) {
            if(stocks.get(i).getSymbol().equals(stock.getSymbol())) {
                adapter.searchAllStocks.clear();
                //create alertbox
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(stock.getSymbol() + " is already displayed!");
                builder.setTitle("Duplicate Stock");

                AlertDialog dialog = builder.create();
                dialog.show();
                flag = true;
            }
        }

        //if no duplicate then add to list, database, and notify adapter that we changed
        if(!flag) {
            stocks.add(stock);
            adapter.searchAllStocks.add(new Stock(stock.getSymbol(), stock.getName(), stock.getLatestPrice(), stock.getChange(), stock.getChangePercent()));
            db.addStock(stock);
            adapter.notifyDataSetChanged();
        }
    }


    //call when user long presses on stock to delete
    @Override
    public boolean onLongClick(View v) {
        final int pos = recyclerView.getChildLayoutPosition(v);
        Stock s = stocks.get(pos);

        //create alertbox
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete " + s.getSymbol() + "?");
        builder.setTitle("Delete Stock");

        //sets delete button
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Stock stockDel = stocks.get(pos);
                db.deleteStock(stockDel.getSymbol());
                stocks.remove(pos);
                adapter.searchAllStocks.remove(pos);
                adapter.notifyDataSetChanged();
            }
        });

        //sets cancel button
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancels box
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }
}
