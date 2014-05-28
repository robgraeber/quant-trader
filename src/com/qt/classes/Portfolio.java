package com.qt.classes;

import java.util.ArrayList;
//
//	Portfolio.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
import java.util.List;

import com.qt.StockDatabase;

//Holds all the stock/assets and tracks algorithm results
public class Portfolio {
	private static ArrayList<Portfolio> portfolioArray = new ArrayList<Portfolio>();
	public double avaliableFunds = 100000;
	public double initialFunds = 100000;
	private List<Asset> assetArray = new ArrayList<Asset>();

	public static Portfolio getNewPortfolio(double initialFunds){
		
		Portfolio portfolio = new Portfolio();
		portfolio.avaliableFunds = initialFunds;
		portfolio.initialFunds = initialFunds;
		portfolioArray.add(portfolio);
		return portfolio;
	}
	public void resetMe(){
		this.avaliableFunds = this.initialFunds;
		assetArray = new ArrayList<Asset>();
	}
	public void resetMe(double initialFunds){
		this.initialFunds = initialFunds;
		resetMe();
	}
	
	public List<Asset> getAssetArray(){
    	return assetArray;
    }
	public Asset getAsset(String symbol){
		for(Asset asset : assetArray){
			if (asset.symbol.equalsIgnoreCase(symbol)){
				return asset;
			}
		}
		return new Asset("ASDF", 0);
	}
	public double getTotalValue(){
		double totalValue = avaliableFunds;
		for(Asset asset:assetArray){
			Stock stock = StockDatabase.getStock(asset.symbol);
			totalValue += stock.getClose() * asset.quantity;
		}
		return totalValue;
	}
	public void addAsset(String symbol, int quantity, double cost){
		if(findSymbol(symbol) != null){
			Asset asset = findSymbol(symbol);
			double currentCost = asset.quantity * asset.averageCost;
			currentCost += cost * quantity;
			asset.quantity += quantity;
			currentCost /= asset.quantity;
			asset.averageCost = currentCost;
		}else{
			assetArray.add(new Asset(symbol, quantity));
			findSymbol(symbol).averageCost = cost;
		}
	}
	public void removeAsset(String symbol, int quantity){
		if(findSymbol(symbol) != null){
			findSymbol(symbol).quantity -= quantity;
			if(findSymbol(symbol).quantity == 0){
				assetArray.remove(findSymbol(symbol));
			}
		}else{
			System.out.println("Unable to remove asset -- Asset not found!");
		}
	}
	public boolean containsSymbol(String symbol){
		return getSymbolQuantity(symbol) != 0;
    }
	public int getSymbolQuantity(String symbol){
		int quantity = 0;
    	for(Asset asset:getAssetArray()){
    		if (asset.symbol.equalsIgnoreCase(symbol)){
    			quantity = asset.quantity;
    		}
    	}
    	return quantity;
    }
    public Asset findSymbol(String symbol){
    	for(Asset asset:getAssetArray()){
    		if (asset.symbol.equalsIgnoreCase(symbol)){
    			return asset;
    		}
    	}
    	return null;
    }
    
 }
