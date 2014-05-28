//
//	Algo1.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.qt.modules.backtest.macros;
import java.util.List;

import com.qt.OrderManager;
import com.qt.StockDatabase;
import com.qt.classes.Stock;
import com.qt.classes.Order.OrderType;
import com.qt.modules.backtest.Macro;

//Simple algorithm that tracks a user-created list, ie a benchmark
//Command format: "backtest list:SP500 script1 script2"  
public class ListFunction extends Macro{
	public ListFunction(){
		this.triggerWord = "list";
	}
	public void handleEnterTick(){
		List<Stock> selectedStocks = StockDatabase.getStockArray(param.get(0));
		if(getPortfolio().getSymbolQuantity(selectedStocks.get(0).symbol) <= 0){
			for(Stock stock : selectedStocks){
				int quantity = (int) Math.floor(getPortfolio().avaliableFunds/stock.getOpen()/selectedStocks.size());
				if(quantity > 0){
					OrderManager.submitOrder(OrderType.BUY_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0);
				}
			}
		}
	}
}
