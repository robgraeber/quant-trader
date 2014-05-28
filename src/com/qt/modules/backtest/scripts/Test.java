package com.qt.modules.backtest.scripts;

import com.qt.OrderManager;
import com.qt.StockDatabase;
import com.qt.classes.Stock;
import com.qt.classes.Order.OrderType;
import com.qt.modules.backtest.Script;

//For testing purposes
public class Test extends Script {

	@Override
	public void handleEnterTick() {
		Stock stock = StockDatabase.getStock("IBM");
		System.out.println("Close: "+stock.getClose());
		System.out.println("Price change: "+(stock.getClose()-stock.getClose(1)));
		System.out.println("MA 30: "+stock.getMA(30));
		System.out.println("SD 5: "+stock.getMovingSD(5));
		System.out.println("SD 30: "+stock.getMovingSD(30));
		System.out.println("SD 90: "+stock.getMovingSD(90));
		if(getPortfolio().getSymbolQuantity(stock.symbol) <= 0){
			int quantity = (int) Math.floor(getPortfolio().avaliableFunds/stock.getOpen());
			OrderManager.submitOrder(OrderType.BUY_AT_OPEN, stock.symbol, quantity, getPortfolio(), 0);
		}

	}

}
