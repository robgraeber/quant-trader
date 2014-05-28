package com.qt.modules.database;

import java.util.List;

import com.qt.Module;
import com.qt.StockDatabase;

//The core database module
//Handles the user lists, downloading data from yahoo, and some wrapper functions for the StockDatabase class
public class Database extends Module {
	public Database(){
		this.name = "Database";
		this.triggerWord = "database";
		this.helpText = "database <download> <rebuild> <preload> <purge> <stats> <lists>";
	}
	@Override
	public void activate(List<String> parameters) {
		// TODO Auto-generated method stub
		for (String parameter:parameters){
			if(parameter.equalsIgnoreCase("download")){
				rebuildMe();
				downloadMe();
			}else if(parameter.equalsIgnoreCase("rebuild")){
				rebuildMe(); //rebuilds list database
			}else if(parameter.equalsIgnoreCase("preload")){
				StockDatabase.preloadMe(); //preloads symbols in memory
			}else if(parameter.equalsIgnoreCase("purge")){
				StockDatabase.cleanCache(); //purges symbols in memory
			}else if(parameter.equalsIgnoreCase("stats")){
				System.out.println("Total Symbols in Database: "+StockDatabase.totalSymbolCount());
				System.out.println("Total Symbols in Memory: "+StockDatabase.activeSymbolCount());
			}else if(parameter.equalsIgnoreCase("lists")){
				System.out.println("User Lists:");
				for(String listName : StockDatabase.getListNames()){
					System.out.println(listName);
				}
				System.out.println("");
			}
		}
	}
	//Downloads all AMEX,NYSE,NASDAQ eod stock data from Yahoo and inserts into database
	//Rebuilds whole database each time, not the best solution but it's not that bad
	//Takes around 10-20 minutes, mainly to download the 7000+ stock csv's from yahoo
	private void downloadMe(){
		StockFetcher.downloadStocks("databases/StockLists/NYSE.txt", "databases/nyse_eod.db");
		StockFetcher.downloadStocks("databases/StockLists/NASDAQ.txt", "databases/nasdaq_eod.db");
		StockFetcher.downloadStocks("databases/StockLists/AMEX.txt", "databases/amex_eod.db");
	}
	//rebuilds the stock_lists.db from txt lists in resources/custom-lists/ folder
	private void rebuildMe(){
		StockFetcher.parseListsIntoDatabase("UserLists/","databases/user_lists.db");
	}
}
