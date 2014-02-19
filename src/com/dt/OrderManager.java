package com.dt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
//
//	OrderHandler.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//






















import com.dt.classes.Order;
import com.dt.classes.Portfolio;
import com.dt.classes.Stock;
import com.dt.classes.Order.OrderType;

//Basically just takes orders from the backtest scripts and queues it so they execute at the right time.
//There's options for slippage/commission and infinite margin buying
public class OrderManager {
	private static final double slippage = 0.0005; //percent loss per trade, i.e. 0.001 = .1% loss each way
	private static final double commission = 0.005; //per share cost, i.e. 0.01 = $0.01 per share each way
	private static final boolean allowNegativePortfolio = false;
	private static List<Order> orderArray = new ArrayList<Order>();
	
    public static void submitOrder(OrderType orderType, String symbol, int quantity, Portfolio portfolio){
    	submitOrder(orderType, symbol, quantity, portfolio, 0, false);
    }
    public static void submitOrder(OrderType orderType, String symbol, int quantity, Portfolio portfolio, int timeDelay){
    	submitOrder(orderType, symbol, quantity, portfolio, timeDelay, false);
    }
    public static void submitOrder(OrderType orderType, String symbol, int quantity, Portfolio portfolio, int timeDelay, boolean isCommissionAndSlippageEnabled){
    	if(StockDatabase.getStock(symbol).getClose() <= 0.0){
    		System.out.println("Invalid Order -- "+symbol.toUpperCase()+" -- Data unavaliable");
    		return;
    	}
    	if(quantity <= 0){
    		System.out.println("Invalid Order -- "+symbol.toUpperCase()+" -- Quantity cannot be "+quantity);
    		return;
    	}
    	
    	orderArray.add(new Order(orderType, symbol, quantity, portfolio, timeDelay, isCommissionAndSlippageEnabled));
    }
    private static void executeOrder(Order order){
    	if(order.orderType == OrderType.BUY_AT_OPEN || order.orderType == OrderType.BUY_AT_CLOSE){
    		Stock stock = StockDatabase.getStock(order.symbol);
    		double stockPrice = 0.0;
    		if(order.orderType == OrderType.BUY_AT_OPEN){
    			stockPrice = stock.getOpen();
    		}
    		if(order.orderType == OrderType.BUY_AT_CLOSE){
    			stockPrice = stock.getClose();
    		}
    		order.portfolio.avaliableFunds -= stockPrice * (double)order.quantity;
    		if(order.isCommissionAndSlippageEnabled){
    			double commissionTotal = order.quantity * commission;
    			if(commissionTotal < 1.0){
    				commissionTotal = 1.0;
    			}
    			order.portfolio.avaliableFunds -= commissionTotal;
    			order.portfolio.avaliableFunds -= stockPrice * (double)order.quantity * slippage;
    		}
    		order.portfolio.addAsset(order.symbol, order.quantity, stockPrice);
    		//System.out.println("Bought "+order.quantity+" shares of "+order.symbol.toUpperCase()+ " at "+stockPrice);
    	}
    	if(order.orderType == OrderType.SELL_AT_OPEN || order.orderType == OrderType.SELL_AT_CLOSE || order.orderType == OrderType.SHORT_AT_OPEN || order.orderType == OrderType.SHORT_AT_CLOSE){
    		Stock stock = StockDatabase.getStock(order.symbol);
    		double stockPrice = 0.0;
    		if(order.orderType == OrderType.SELL_AT_OPEN || order.orderType == OrderType.SHORT_AT_OPEN){
    			stockPrice = stock.getOpen();
    		}
    		if(order.orderType == OrderType.SELL_AT_CLOSE || order.orderType == OrderType.SHORT_AT_CLOSE){
    			stockPrice = stock.getClose();
    		}
    		order.portfolio.avaliableFunds += stockPrice * (double)order.quantity;
    		if(order.isCommissionAndSlippageEnabled){
    			double commission = order.quantity * 0.01;
    			if(commission < 1.0){
    				commission = 1.0;
    			}
    			order.portfolio.avaliableFunds -= commission;
    			order.portfolio.avaliableFunds -= stockPrice * (double)order.quantity * slippage;
    		}
    		if(order.orderType == OrderType.SELL_AT_OPEN || order.orderType == OrderType.SELL_AT_CLOSE){
	    		DecimalFormat percentFormat = new DecimalFormat("###.##%");
	    		double profitPercentage = (stockPrice/order.portfolio.findSymbol(order.symbol).averageCost)-1;
	    		if(profitPercentage > 0.30){
	    			System.out.println(StockDatabase.getStock(order.symbol).getDate()+" - Sold "+order.quantity+" "+order.symbol.toUpperCase()+" at "+stockPrice+" -- Profit: "+percentFormat.format(profitPercentage));
	    		}
	    		order.portfolio.removeAsset(order.symbol, order.quantity);
    		}else{
    			order.portfolio.addAsset(order.symbol, -order.quantity, stockPrice);
    		}
    		
    		//System.out.println("Sold "+order.quantity+" shares of "+order.symbol.toUpperCase()+ " at "+stockPrice);
    	}
    }
    
    public static void handleEnterTick(){
    	for(int i = 0; i < orderArray.size(); i++){
    		Order order = orderArray.get(i);
    		if(order.timeDelay == 0){
	    		Stock stock = StockDatabase.getStock(order.symbol);
	    		if(order.orderType == OrderType.BUY_AT_OPEN && (order.portfolio.avaliableFunds >= stock.getOpen() * order.quantity || allowNegativePortfolio)){
	    			executeOrder(order);
	    			orderArray.remove(i);
	    			i--;
	    		}else if(order.orderType == OrderType.BUY_AT_OPEN && order.portfolio.avaliableFunds < stock.getOpen() * order.quantity){
	    			System.out.println("Unable to buy -- "+ order.symbol +" -- Not enough avaliable funds!");
	    			orderArray.remove(i);
	    			i--;
	    		}else if(order.orderType == OrderType.SHORT_AT_OPEN || (order.orderType == OrderType.SELL_AT_OPEN && order.portfolio.getSymbolQuantity(order.symbol) >= order.quantity)){
	    			executeOrder(order);
	    			orderArray.remove(i);
	    			i--;
	    		}else if(order.orderType == OrderType.SELL_AT_OPEN && order.portfolio.getSymbolQuantity(order.symbol) < order.quantity){
	    			//System.out.println("Unable to sell -- "+ order.symbol +" -- Cannot find shares in portfolio!");
	    			orderArray.remove(i);
	    			i--;
	    		}
    		}
    	}
    	for(int i = 0; i < orderArray.size(); i++){
    		Order order = orderArray.get(i);
    		if(order.timeDelay == 0){
    			Stock stock = StockDatabase.getStock(order.symbol);
    			if(order.orderType == OrderType.BUY_AT_CLOSE && (order.portfolio.avaliableFunds >= stock.getClose() * order.quantity || allowNegativePortfolio)){
    				executeOrder(order);
    				orderArray.remove(i);
    				i--;
    			}else if(order.orderType == OrderType.BUY_AT_CLOSE && order.portfolio.avaliableFunds < stock.getClose() * order.quantity){
    				System.out.println("Unable to buy -- "+ order.symbol +" --  Not enough avaliable funds!");
    				orderArray.remove(i);
    				i--;
    			}else if(order.orderType == OrderType.SHORT_AT_CLOSE || (order.orderType == OrderType.SELL_AT_CLOSE && order.portfolio.getSymbolQuantity(order.symbol) >= order.quantity)){
    				executeOrder(order);
    				orderArray.remove(i);
    				i--;
    			}else if (order.orderType == OrderType.SELL_AT_CLOSE && order.portfolio.getSymbolQuantity(order.symbol) < order.quantity){
    				//System.out.println("Unable to sell -- "+ order.symbol +" --  Cannot find shares in portfolio!");
    				orderArray.remove(i);
    				i--;
    			}
    		}
    	}
    	for(int i = 0; i < orderArray.size(); i++){
    		Order order = orderArray.get(i);
    		if(order.timeDelay != 0){
    			order.timeDelay -= 1;
    		}
    	}
    	//if
    }
}
