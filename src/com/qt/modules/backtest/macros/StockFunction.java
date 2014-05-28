//
//	Algo1.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.qt.modules.backtest.macros;
import com.qt.OrderManager;
import com.qt.StockDatabase;
import com.qt.classes.Stock;
import com.qt.classes.Order.OrderType;
import com.qt.modules.backtest.Macro;

//Simple algorithm that tracks a certain stock, ie a benchmark
//Command format: "backtest stock:APPL script1 script2"  
public class StockFunction extends Macro{
	public StockFunction(){
		this.triggerWord = "stock";
	}
	public void handleEnterTick(){
		Stock stock = StockDatabase.getStock(param.get(0));
		if(getPortfolio().getSymbolQuantity(stock.symbol) <= 0){
			int quantity = (int) Math.floor(getPortfolio().avaliableFunds/stock.getOpen());
			OrderManager.submitOrder(OrderType.BUY_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0);
		}
	}
}
