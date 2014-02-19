//
//	BuyOnGapAlgo.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 20, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt.modules.backtest.scripts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dt.OrderManager;
import com.dt.StockDatabase;
import com.dt.classes.Order.OrderType;
import com.dt.classes.Stock;
import com.dt.modules.backtest.Script;
import com.effing.toolkit.Util2;


//Buy at the open 100 stocks within the S&P500 which have the lowest returns
//from their previous day's lows to the current day's open, provided that 
//the gap down is greater than one standard deviation.
//Exit such long positions at the day's close.
//Special thanks to Ernie Chan for this strategy
//Source: http://epchan.blogspot.com/2012/04/life-and-death-of-strategy.html
public class BuyOnGap extends Script {

	public BuyOnGap() {
		
	}

	@Override
	public void handleEnterTick() {
		List<Stock> stockArray = new ArrayList<Stock>(StockDatabase.getStockArray("sp500_2010"));
		
		for(int i = stockArray.size()-1; i >= 0; i--){
			Stock stock = stockArray.get(i);
			double returns = stock.getOpen(0) - stock.getLow(1);
			boolean criteria = Stock.currentId > 90 && returns < -1*stock.getMovingSD(90, 1);
			if(!criteria){
				stockArray.remove(i);
			}
		}
		Collections.sort(stockArray, stockChangeComparator);
		Util2.trimArray(stockArray, 100);
		System.out.println("BuyOnGap Buys: "+stockArray.size());
		double fundsPerOrder = getPortfolio().initialFunds / stockArray.size();
		for(Stock stock : stockArray){
			int quantity = (int) Math.floor(fundsPerOrder/stock.getOpen());
			if(quantity > 0){
				OrderManager.submitOrder(OrderType.BUY_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0, true);
				OrderManager.submitOrder(OrderType.SELL_AT_CLOSE, stock.symbol, quantity, getPortfolio(), 0, true);
			}
		}
	}
	
	//sorts a stock array to have the biggest % losers first
	public static Comparator<Stock> stockChangeComparator = new Comparator<Stock>() {
		
		public int compare(Stock stock1, Stock stock2) {
			double s1Gap = stock1.getOpen(0) - stock1.getLow(1);
			double s2Gap = stock2.getOpen(0) - stock2.getLow(1);
			double stock1Value =  s1Gap/stock1.getClose(1);
			double stock2Value =  s2Gap/stock2.getClose(1);
			
			if(stock1Value > stock2Value){
				return 1;
			}else if (stock1Value < stock2Value){
				return -1;
			}else{
				return 0;
			}
		}
		
		};

}
