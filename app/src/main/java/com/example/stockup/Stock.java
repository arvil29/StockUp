package com.example.stockup;

import java.io.Serializable;

public class Stock implements Serializable {
    private long ID;
    private String symbol;
    private String name;
    private double latestPrice;
    private double change;
    private double changePercent;

    Stock() {

    }

    Stock(String symbol, String name, double latestPrice, double change, double changePercentage) {
        this.symbol = symbol;
        this.name = name;
        this.latestPrice = latestPrice;
        this.change = change;
        this.changePercent = changePercentage;
    }


    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }
}
