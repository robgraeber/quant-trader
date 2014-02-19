//
//	Algorithm.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt.modules.backtest;

import com.dt.classes.Portfolio;



//The main backtesting algorithms
//Overwrite handleEnterFrame and issue your buy/sell orders to the OrderManager
public abstract class Script {
	private Portfolio portfolio = Portfolio.getNewPortfolio(100000000);
	public Script() {
	}

	//called every tick/row of the stock database, 1 tick = 1 day when using EOD data
	public abstract void handleEnterTick();

	public Portfolio getPortfolio() {
		return portfolio;
	}
}
