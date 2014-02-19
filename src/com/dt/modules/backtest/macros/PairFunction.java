//
//	Algo1.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt.modules.backtest.macros;
import java.util.ArrayList;
import java.util.List;

import com.dt.OrderManager;
import com.dt.StockDatabase;
import com.dt.classes.Stock;
import com.dt.classes.Order.OrderType;
import com.dt.modules.backtest.Macro;
import com.effing.toolkit.Util2;

//Simple algorithm that tracks a pair's relative change, buys stock1, shorts stock2
//Also accepts lists and defaults to list if found, hopefully there's no collisions
//Command format: "backtest pair:KO:PEP script1 script2"  
public class PairFunction extends Macro{
	public PairFunction(){
		this.triggerWord = "pair";
	}
	public void handleEnterTick(){
		List<Stock> selectedStocksBuy = new ArrayList<Stock>();
		List<Stock> selectedStocksShort = new ArrayList<Stock>();
		if(Util2.arrayContainsString(StockDatabase.getListNames(), param.get(0))){
			selectedStocksBuy = StockDatabase.getStockArray(param.get(0));
		}else{
			selectedStocksBuy.add(StockDatabase.getStock(param.get(0)));
		}
		if(Util2.arrayContainsString(StockDatabase.getListNames(), param.get(1))){
			selectedStocksShort = StockDatabase.getStockArray(param.get(1));
		}else{
			selectedStocksShort.add(StockDatabase.getStock(param.get(1)));
		}
		
		for(Stock stock : selectedStocksBuy){
			int quantity = (int) Math.floor(getPortfolio().initialFunds/stock.getOpen()/selectedStocksBuy.size());
			if(quantity > 0 && getPortfolio().getSymbolQuantity(stock.symbol) <= 0){
				OrderManager.submitOrder(OrderType.BUY_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0);
			}
		}
		for(Stock stock : selectedStocksShort){
			int quantity = (int) Math.floor(getPortfolio().initialFunds/stock.getOpen()/selectedStocksShort.size());
			if(quantity > 0  && getPortfolio().getSymbolQuantity(stock.symbol) >= 0){
				OrderManager.submitOrder(OrderType.SHORT_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0);
			}
		}
	}
	public String getDisplayName(){
		return param.get(0).toUpperCase() +":"+param.get(1).toUpperCase();
	}
}
