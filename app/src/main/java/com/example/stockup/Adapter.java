package com.example.stockup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {
    MainActivity mainAct;
    List<Stock> stocks;
    List<Stock> searchAllStocks;

    Adapter(MainActivity ma, List<Stock> stocks) {
        this.mainAct = ma;
        this.stocks = stocks;
        this.searchAllStocks = new ArrayList<>(stocks);
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_listview, parent, false);
        view.setOnLongClickListener(mainAct); //to enable long click to delete in MainActivity
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        String symbol = stocks.get(i).getSymbol();
        String name = stocks.get(i).getName();
        double latestPrice = stocks.get(i).getLatestPrice();
        double change = stocks.get(i).getChange();
        double changePercent = stocks.get(i).getChangePercent();
        String up = "▲";
        String down = "▼";

        //set symbol and name
        holder.symbol.setText(symbol);
        holder.name.setText(name);


        //to call custom textView we made (rounded textView)
        GradientDrawable drawableRounded = (GradientDrawable) holder.price.getBackground();


        //set color schemes of price, change and changePercentage accordingly
        if(change > 0) { //if positive change
            holder.change.setText(up + " $" + String.format("%.2f", change) + " (" + String.format("%.2f", changePercent) + "%)");
            holder.price.setText("$" + String.format("%.2f", latestPrice));
            drawableRounded.setColor(Color.parseColor("#329136"));
        }
        else { //if negative change
            holder.change.setText(down + " $" + String.format("%.2f", change) + " (" + String.format("%.2f", changePercent) + "%)");
            holder.price.setText("$" + String.format("%.2f", latestPrice));
            drawableRounded.setColor(Color.parseColor("#DC1313"));
        }
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }


    //filtering for search bar
    @Override
    public Filter getFilter() {
        return stockFilter;
    }
    private Filter stockFilter = new Filter() {
        //runs on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //charSequence constraint is whatever is typed into search
            List<Stock> filteredList = new ArrayList<Stock>(); //list to store only filtered items

            if(constraint == null || constraint.length() == 0) { //if nothing in input field
                filteredList.addAll(searchAllStocks); //show all stocks on filteredList
                for(int i = 0; i < filteredList.size(); i++) {
                    Log.d("FilterBarEmpty: ", filteredList.get(i).getSymbol().toString());
                }

            }
            else { //else get user input and convert into string to filter
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(Stock item: searchAllStocks) { //for each stock item in
                    if(item.getName().toLowerCase().contains(filterPattern)
                            || item.getSymbol().toLowerCase().contains(filterPattern)) { //add items that match to filteredList
                        filteredList.add(item);
                            for(int i = 0; i < filteredList.size(); i++) {
                                Log.d("FilteredBarFiltered: ", filteredList.get(i).getSymbol().toString());
                            }
                    }
                }
            }

            //returns FilterResults and gets passed to publishResults
            FilterResults results = new FilterResults();
            results.values = filteredList;


            return results;
        }
        //runs on UI thread
        //clears current stock list and adds results of user's search to list
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            stocks.clear();
            stocks.addAll((List)results.values);
            for(int i = 0; i < stocks.size(); i++) {
                Log.d("Stocks: ", stocks.get(i).getSymbol().toString());
            }

            for(int i = 0; i < searchAllStocks.size(); i++) {
                Log.d("SearchAllStocks: ", searchAllStocks.get(i).getSymbol().toString());
            }

            notifyDataSetChanged();
        }
    };



    //ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbol;
        TextView name;
        TextView change;
        TextView price;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            symbol = (TextView)itemView.findViewById(R.id.symbol);
            name = (TextView)itemView.findViewById(R.id.name);
            change = (TextView)itemView.findViewById(R.id.change);
            price = (TextView)itemView.findViewById(R.id.price);


            //opens MarketWatch website on a click / can also be put in MainActivity class
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String symbol = ((TextView)v.findViewById(R.id.symbol)).getText().toString(); //get stock symbol to add to url end

                    if(symbol.trim().isEmpty()) { //if symbol empty don't do anything
                        return;
                    }

                    String url = "https://www.marketwatch.com/investing/stock/" + symbol; //form url with specific stock


                    Intent i = new Intent(Intent.ACTION_VIEW); //ACTION_VIEW opens a browser, whichever browser you choose
                    i.setData(Uri.parse(url)); //intent parses url with uri library's help
                    v.getContext().startActivity(i); //starts activity
                }

            });
        }
    }
}
