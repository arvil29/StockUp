package com.example.stockup;

import java.util.ArrayList;
import java.util.List;

//uses quickSort algorithm for O(nlog(n)) runtime

public class QuickSortSymbol {

    List<Stock> stocks = new ArrayList<>();

    public QuickSortSymbol(List<Stock> stocks) {
        this.stocks = stocks;
    }

    public void sort(List<Stock> stocks, int low, int high) {
        if(low < high) {
            int pIndex = partition(stocks, low, high); //get partition index

            //recursively call sort() w/ new start & end values based on partition index
            sort(stocks, low, pIndex - 1);
            sort(stocks, pIndex + 1, high);
        }
    }

    public int partition(List<Stock> stocks, int low, int high) {
        Stock pivot = stocks.get(high); //we set pivot point to last element in list
                                        //maybe use another partitioning method for better performance at worst case --future improvements

        int pIndex = (low - 1); //set partition index -- will be -1 at first

        for(int j = low; j < high; j++) {

            //if current stock is smaller than pivot --> swap
            if(stocks.get(j).getSymbol().compareToIgnoreCase(pivot.getSymbol()) <= 0) {
                pIndex++;

                //Collections.swap(stocks, pIndex, j);
                Stock temp = stocks.get(pIndex);
                stocks.set(pIndex, stocks.get(j));
                stocks.set(j, temp);

            }
        }

        //Collections.swap(stocks, pIndex + 1, high);
        Stock temp = stocks.get(pIndex + 1);
        stocks.set(pIndex + 1, stocks.get(high));
        stocks.set(high, temp);


        //return the partition index
        return pIndex + 1;
    }

}
