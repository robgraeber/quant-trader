//
//	StockDatabase.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.qt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import com.qt.classes.Stock;


//The big repository of stock data, loads from database or cache as necessary
//Either get stocks by symbol name, stock exchange (NYSE,NASDAQ,AMEX), or custom user lists (SP500, industry lists, etc)
public class StockDatabase {
	public enum ExchangeType {NYSE, NASDAQ, ETF, ALL};
	public enum ListType {SP500}; //Enum for convenience, it just gets converted to a string
	//randomizes the stock arrays so algorithms don't get caught up on the same stock
	public static boolean isRandomizedArrays = false;
	//dictionary that links the "ExchangeType" to an array of already loaded stocks
	private static EnumMap<ExchangeType, List<Stock>> stockArrayMap = new EnumMap<ExchangeType, List<Stock>>(ExchangeType.class);
	//dictionary that links the "ExchangeType" to a cached array of stock symbols
	private static EnumMap<ExchangeType, List<String>> symbolArrayMap = new EnumMap<ExchangeType, List<String>>(ExchangeType.class);
	//dictionary that links the "ExchangeType" to the database name ie nyse_eod.db
	private static EnumMap<ExchangeType, String> databaseMap = new EnumMap<ExchangeType, String>(ExchangeType.class); 
	
	//dictionary that links the "ListType" to a cached array of stocks
	private static HashMap<String, List<Stock>> listStockArrayMap = new HashMap<String, List<Stock>>();
	//dictionary that links the "ListType" to a cached array of stock symbols
	private static HashMap<String, List<String>> listSymbolArrayMap = new HashMap<String, List<String>>();

	//maps the database names to ExchangeTypes
	static {
		databaseMap.put(ExchangeType.NYSE, "databases/nyse_eod.db");
		databaseMap.put(ExchangeType.NASDAQ, "databases/nasdaq_eod.db");
		databaseMap.put(ExchangeType.ETF, "databases/amex_eod.db");
		
		//listTableMap.put(ListType.SP500, "SP500");
    }
	//gets the quantity of symbols available in the databases, doesn't matter if they are loaded yet
	public static int totalSymbolCount(){
		int intSize = 0;
		for(ExchangeType e : databaseMap.keySet()){
			intSize += StockDatabase.getSymbolArray(e).size();
		}
		return intSize;
	}
	//gets the quantity of stocks preloaded into memory
	public static int activeSymbolCount(){
		int intSize = 0;
		for(ExchangeType e : ExchangeType.values()){
			intSize += StockDatabase.getStockArray(e, false).size();
		}
		return intSize;
	}
	//Gets array of symbols in the Exchange, checks cache first, if not loads from databases based on table names 
	//ExchangeType.ALL == ExchangeType.NYSE + ExchangeType.NASDAQ
	private static List<String> getSymbolArray(ExchangeType e){
		if(!symbolArrayMap.containsKey(e) && e != ExchangeType.ALL){
			symbolArrayMap.put(e, fetchSymbolArrayFromDB(databaseMap.get(e)));
		}else if(!symbolArrayMap.containsKey(e) && e == ExchangeType.ALL){
			List<String> symbolList = new ArrayList<String>();
			symbolList.addAll(getSymbolArray(ExchangeType.NYSE));
			symbolList.addAll(getSymbolArray(ExchangeType.NASDAQ));
			symbolArrayMap.put(e, symbolList);
		}
		return symbolArrayMap.get(e);
	}
	//Gets array of symbols in the user list, checks cache first, if not loads from databases based on table names 
	private static List<String> getListSymbolArray(String e){
		if(!listSymbolArrayMap.containsKey(e)){
			listSymbolArrayMap.put(e, fetchSymbolArrayFromListDB(e));
		}
		return listSymbolArrayMap.get(e);
	}
	//Deletes the cached Stock objects and symbols
	//Useful for conserving memory when your changing/hot-reloading algorithms and not using all stocks
	public static void cleanCache(){
		System.out.println("-- Purging Cache --");
		stockArrayMap = new EnumMap<ExchangeType, List<Stock>>(ExchangeType.class);
		symbolArrayMap = new EnumMap<ExchangeType, List<String>>(ExchangeType.class);
		
		listStockArrayMap = new HashMap<String, List<Stock>>();
		listSymbolArrayMap = new HashMap<String, List<String>>();
		System.gc();
		System.out.println("Done!");
	}
	//Loads all parsed Stock objects + symbols into memory
	public static void preloadMe(){
		
		List<String> symbolList = new ArrayList<String>();
		for(ExchangeType e : databaseMap.keySet()){
			symbolList.addAll(getSymbolArray(e));
		}
		fetchStockArray(symbolList);
	}
	public static List<Stock> getStockArray(ListType e) {
		return getStockArray(e.toString(), true);
	}
	public static List<Stock> getStockArray(String e) {
		return getStockArray(e, true);
	}
	private static List<Stock> getStockArray(String e, boolean preload) {
		if(!listStockArrayMap.containsKey(e)){
			listStockArrayMap.put(e, new ArrayList<Stock>());
		}
		if(preload && listStockArrayMap.get(e).size() == 0){
			listStockArrayMap.put(e, fetchStockArray(getListSymbolArray(e)));
		}
		if(isRandomizedArrays){
			List<Stock> stockList = new ArrayList<Stock>(listStockArrayMap.get(e));
			Collections.shuffle(stockList);
	        return stockList;
		}else{
			return listStockArrayMap.get(e);
		}
    }
	//Returns (and fetches if necessary) the parsed stocks for an exchange/list
	//The preload parameter is to prevent infinite loops internally during initialization
	public static List<Stock> getStockArray(ExchangeType e) {
		return getStockArray(e, true);
	}
	private static List<Stock> getStockArray(ExchangeType e, boolean preload) {
		if(!stockArrayMap.containsKey(e)){
			stockArrayMap.put(e, new ArrayList<Stock>());
		}
		if(preload && stockArrayMap.get(e).size() < getSymbolArray(e).size()){
			stockArrayMap.put(e, fetchStockArray(getSymbolArray(e)));
		}
		if(isRandomizedArrays){
			List<Stock> stockList = new ArrayList<Stock>(stockArrayMap.get(e));
			Collections.shuffle(stockList);
	        return stockList;
		}
        return stockArrayMap.get(e);
    }
	//loads symbol lists from List DB and returns it
	public static List<String> getListNames(){
		Connection conn = null;
		Statement stmt = null;
		List<String> listNameArray = new ArrayList<String>();
		try{
			
			String databaseName = "databases/user_lists.db";
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table'");
			while(rs.next()){
				listNameArray.add(rs.getString("name"));
			}
			rs.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null){
					conn.close();
				}
			}catch(SQLException se){
				
			}try{
				if(conn!=null){
					conn.close();
				}
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		return listNameArray;
	}
	//parses all symbols from database into Stock objects and prints status
	private static List<Stock> fetchStockArray(List<String> symbolList){
		
		System.out.println("- Loading Stocks From Database -");
		double percentVar = 0.1;
		List<Stock> stockList = new ArrayList<Stock>(symbolList.size());
		for (int i = 0; i < symbolList.size(); i++){
			String symbol = symbolList.get(i);
			Stock stock = getStock(symbol);
			if(stock.getArraySize()>0){
				stockList.add(stock);
			}
			double percentComplete = (double)Math.round((double)(i+1)/(double)symbolList.size()*100.0*100)/100;
			if(percentComplete >= percentVar){
				System.out.println("Symbols Loaded - "+(i+1)+"/"+symbolList.size() +" - "+ percentComplete+"%");
				percentVar += 0.1;
			}
		}
		System.out.println("Done!");
		return stockList;
	}
	//loads symbol lists from List DB
	private static List<String> fetchSymbolArrayFromListDB(String tableName){
		Connection conn = null;
		Statement stmt = null;
		List<String> symbolList = new ArrayList<String>();
		try{
			String formattedListName = tableName.toUpperCase();
			formattedListName = formattedListName.replace("-", "_");
			formattedListName = formattedListName.replace(".", "_");
			String databaseName = "databases/user_lists.db";
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM '"+formattedListName+"'");
			while(rs.next()){
				symbolList.add(rs.getString("symbol"));
			}
			rs.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null){
					conn.close();
				}
			}catch(SQLException se){
				
			}try{
				if(conn!=null){
					conn.close();
				}
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		return symbolList;
	}
	//loads symbol lists from Exchange DB
	private static List<String> fetchSymbolArrayFromDB(String databaseName){
		Connection conn = null;
		Statement stmt = null;
		List<String> symbolList = new ArrayList<String>();
		try{
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
			stmt = conn.createStatement();
			System.out.println("Loading symbols from "+databaseName);
			ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
			while(rs.next()){
				symbolList.add(rs.getString("name"));
			}
			rs.close();
		}catch(SQLException se){
			se.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(stmt!=null){
					conn.close();
				}
			}catch(SQLException se){
				
			}try{
				if(conn!=null){
					conn.close();
				}
			}catch(SQLException se){
				se.printStackTrace();
			}
		}
		return symbolList;
	}
	@SuppressWarnings("unused")
	//searches a list of symbols/strings for a specfic symbol
	private static boolean containsString(List<String> list, String targetString){
		for (String string : list){
			if(string.equalsIgnoreCase(targetString)){
				return true;
			}
		}
		return false;
	}
	//just a wrapper to help algorithm read-ability
	public static Stock getETF(String symbol){
		return getStock(symbol);
	}
	//Loads data from database and parses into a Stock object.
	//First checks each exchange's stock cache, if not found then checks
	//which database has the symbol, then loads symbol from that database.
	//If not found, returns empty stock object.
	public static Stock getStock(String symbol){
		Connection conn = null;
		Statement stmt = null;
		for(ExchangeType key : stockArrayMap.keySet()){
			for(int i = 0; i < stockArrayMap.get(key).size(); i++){
				Stock stock = stockArrayMap.get(key).get(i);
				if (stock.symbol.equalsIgnoreCase(symbol)){
					return stock;
				}
			}
		}
		Stock stock = new Stock(symbol);
		String databaseName = "placeholder";
		for(ExchangeType key : databaseMap.keySet()){
			for(int i = 0; i < getSymbolArray(key).size(); i++){
				String theSymbol = getSymbolArray(key).get(i);
				if (theSymbol.equalsIgnoreCase(symbol)){
					getStockArray(key, false).add(stock);
					databaseName = databaseMap.get(key);
				}
			}
		}
		if(databaseName.equals("placeholder")){
			System.out.println("Error -- Unable to find "+symbol.toUpperCase()+" in DB!");
			return stock;
		}
		
		try{
			//STEP 2: Register JDBC driver
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
			//System.out.println("Connected database successfully...");

			//STEP 4: Execute a query
			//System.out.println("Creating statement...");
			stmt = conn.createStatement();

			// Extract records in ascending order by first name.
			//System.out.println("Fetching records in ascending order...");
			ResultSet rs = stmt.executeQuery("SELECT * FROM '"+symbol+"'");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			while(rs.next()){
				java.util.Date date = dateFormat.parse(rs.getString("day"));
				stock.addData(rs.getDouble("open"), rs.getDouble("high"), rs.getDouble("low"), rs.getDouble("close"), rs.getDouble("volume"), date);
			}
			rs.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null){
					conn.close();
				}
			}catch(SQLException se){
				// do nothing
			}try{
				if(conn!=null){
					conn.close();
				}
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		//System.out.println("Goodbye!");
		return stock;
	}
}