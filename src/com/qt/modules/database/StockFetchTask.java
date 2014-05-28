package com.qt.modules.database;

//Used to download multiple stock csv's in an asynchronous way
public class StockFetchTask implements Runnable {

	private String symbol;
	private String databaseName;
	public StockFetchTask(String symbol, String databaseName) {
		this.symbol = symbol;
		this.databaseName = databaseName;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		StockFetcher.getStockCSV(symbol, databaseName);
		//StockFetcher.getStock(symbol, databaseName);
	}

}
