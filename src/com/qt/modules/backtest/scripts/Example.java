//
//	Algo1.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.qt.modules.backtest.scripts;
import java.util.ArrayList;
import java.util.List;

import com.qt.OrderManager;
import com.qt.StockDatabase;
import com.qt.StockDatabase.ListType;
import com.qt.classes.Stock;
import com.qt.classes.Order.OrderType;
import com.qt.modules.backtest.Script;

//To add new algorithm for testing, see MainSystem.java->init()
//Here's a sample algorithm to shows off the basic ordering structure:

//handleEnterTick() is called every new tick, in this case 1 tick = 1 day
//Access stocks from the StockDatabase, and Stock objects have price and volume info 
//Each algo has an attached portfolio accessed by getPortfolio()
//Submit orders to the OrderManager, calculate on your own the quantity of shares to buy
//Note: Keep in mind survivorship bias, commissions, and not using future info when crafting strategies

public class Example extends Script{
	public Example(){
		
	}
	public void handleEnterTick(){
		List<Stock> selectedStocks = new ArrayList<Stock>();
		for (Stock stock : StockDatabase.getStockArray(ListType.SP500)){
			if(stock.getClose(1)>1.0 && stock.getVolume(1)>100000){
				selectedStocks.add(stock);
			}
		}
		double totalFundsPerDay = getPortfolio().initialFunds;
		double fundsPerOrder = totalFundsPerDay / selectedStocks.size();
		for(Stock stock : selectedStocks){
			int quantity = (int) Math.floor(fundsPerOrder/stock.getOpen());
			if(quantity > 0){
				OrderManager.submitOrder(OrderType.BUY_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0);
				OrderManager.submitOrder(OrderType.SELL_AT_CLOSE, stock.symbol, quantity, getPortfolio(), 0);
			}
		}
	}
}
