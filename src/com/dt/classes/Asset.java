//
//	StockAsset.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 16, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt.classes;

import com.dt.StockDatabase;


//Asset represents the asset holding of a portfolio, basically a stock, quantity, and average cost for tracking purposes
public class Asset {
	public String symbol;
	public int quantity;
	public double averageCost;
	public Stock stock;
	public Asset(String symbol, int quantity){
		this.symbol = symbol;
		this.quantity = quantity;
		this.averageCost = 0;
		this.stock = StockDatabase.getStock(symbol);
	}
}
