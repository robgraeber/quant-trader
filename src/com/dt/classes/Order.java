//
//	StockOrder.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 16, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt.classes;


//An order initiated by an algorithm and carried out by the order manager
//Adjust commission and slippage values in order manager
public class Order {
	public enum OrderType {BUY_AT_CLOSE, BUY_AT_OPEN, SELL_AT_CLOSE, SELL_AT_OPEN, SHORT_AT_OPEN, SHORT_AT_CLOSE};

	public OrderType orderType;
	public String symbol;
	public int quantity;
	public int timeDelay;
	public Portfolio portfolio;
	public boolean isCommissionAndSlippageEnabled;
	
	public Order(OrderType orderType, String symbol, int quantity, Portfolio portfolio, int timeDelay, boolean isCommissionAndSlippageEnabled){
		this.orderType = orderType;
		this.symbol = symbol;
		this.quantity = quantity;
		this.portfolio = portfolio;
		this.timeDelay = timeDelay;
		this.isCommissionAndSlippageEnabled = isCommissionAndSlippageEnabled;
	}
}
