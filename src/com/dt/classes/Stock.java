//
//	Stock.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt.classes;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import com.effing.toolkit.Math2;

//The objects that holds massive array loaded from the database.
//Has a bunch of price, volume, MA, etc, getters. 
public class Stock {
	//The day/tick all the stocks are on
	//Corresponds to rows in a database
	public static int currentId = 0;
	//Adjusts if stock is listed later than start date
	//Assumes that no stocks in data get delisted (yahoo finance doesn't include delisted stocks) 
	public static int lengthTarget = 0;
	public String symbol;
	private List<Double> closeArray = new ArrayList<Double>();
	private List<Double> openArray = new ArrayList<Double>();
	private List<Double> highArray = new ArrayList<Double>();
	private List<Double> lowArray = new ArrayList<Double>();
	private List<Double> volumeArray = new ArrayList<Double>();
	public List<Date> dateArray = new ArrayList<Date>();
	public Stock(String theSymbol){
		symbol = theSymbol;
	}
	public int getArraySize(){
		return closeArray.size();
	}
	public void addData(double open, double high, double low, double close, double volume, Date date){
		openArray.add(new Double(open));
		highArray.add(new Double(high));
		lowArray.add(new Double(low));
		closeArray.add(new Double(close));
		volumeArray.add(new Double(volume));
		dateArray.add(date);
	}
	public double getMinimumVolume(int overHowManyDays){
		return getMinimumVolume(overHowManyDays, 0);
	}
	public double getMinimumVolume(int overHowManyDays, int daysAgo){
		double minimumVolume = 10000000000.0;
		for (int i = 0; i<overHowManyDays; i++){
			double volume = getVolume(i+daysAgo);
			if(minimumVolume > volume){
				minimumVolume = volume; 
			}
		}
		return minimumVolume;
	}
	public double getAverageVolume(int overHowManyDays){
		return getAverageVolume(overHowManyDays, 0);
	}
	public double getAverageVolume(int overHowManyDays, int daysAgo){
		List<Double> volumeList = new ArrayList<Double>();
		for (int i = 0; i<overHowManyDays; i++){
			double volume = getVolume(i+daysAgo);
			if(volume >= 0.0){
				volumeList.add(volume);
			}else{
				return -1.0;//before data start
			}
		}
		return Math2.getMean(volumeList);
	}
	//Moving standard deviation of close to close change, not the total change of the time period
	public double getMovingSD(int overHowManyDays){
		return getMovingSD(overHowManyDays, 0);
	}
	public double getMovingSD(int overHowManyDays, int daysAgo){
		List<Double> changeList = new ArrayList<Double>();
		for (int i = 0; i<overHowManyDays; i++){
			double change = getClose(i+daysAgo) - getClose(i+daysAgo+1);
			changeList.add(change);
		}
		return Math2.getStandardDeviation(changeList);
	}
	//simple moving average
	public double getMA(int overHowManyDays){
		return getMA(overHowManyDays, 0);
	}
	public double getMA(int overHowManyDays, int daysAgo){
		List<Double> closeArray = new ArrayList<Double>();
		for (int i = 0; i<overHowManyDays; i++){
			double close = getClose(i+daysAgo);
			if(close >= 0.0){
				closeArray.add(close);
			}
		}
		return Math2.getMean(closeArray);
	}
	public double getPrice(){
		return getPrice(0);
	}
	public double getVolume(){
		return getVolume(0);
	}
	public double getHigh(){
		return getHigh(0);
	}
	public double getLow(){
		return getLow(0);
	}
	public double getOpen(){
		return getOpen(0);
	}
	public double getClose(){
		return getClose(0);
	}
	public Date getDate(){
		return getDate(0);
	}
	public Date getDate(int daysAgo){
		int targetId = Stock.currentId-daysAgo - (lengthTarget - closeArray.size());
		if(targetId < 0){
			return dateArray.get(0);
		}
		if (targetId > closeArray.size()-1){
			return dateArray.get(dateArray.size()-1);
		}
		return dateArray.get(targetId);
	}
	public double getPrice(int daysAgo){
		return getClose(daysAgo);
	}
	public double getVolume(int daysAgo){
		int targetId = Stock.currentId-daysAgo - (lengthTarget - closeArray.size());
		if(targetId < 0){
			return -1.0;
		}
		if (targetId > closeArray.size()-1){
			return -1.0;
		}
		return volumeArray.get(targetId).doubleValue();
	}
	public double getOpen(int daysAgo){
		int targetId = Stock.currentId-daysAgo - (lengthTarget - closeArray.size());
		if(targetId < 0){
			return -1.0;
		}
		if (targetId > closeArray.size()-1){
			return -1.0;
		}
		return openArray.get(targetId).doubleValue();
	}
	public double getClose(int daysAgo){
		int targetId = Stock.currentId-daysAgo - (lengthTarget - closeArray.size());
		if(targetId < 0){
			return -1.0;
		}
		if (targetId > closeArray.size()-1){
			return -1.0;
		}
		return closeArray.get(targetId).doubleValue();
	}
	public double getHigh(int daysAgo){
		int targetId = Stock.currentId-daysAgo - (lengthTarget - closeArray.size());
		if(targetId < 0){
			return -1.0;
		}
		if (targetId > closeArray.size()-1){
			return -1.0;
		}
		return highArray.get(targetId).doubleValue();
	}
	public double getLow(int daysAgo){
		int targetId = Stock.currentId-daysAgo - (lengthTarget - closeArray.size());
		if(targetId < 0){
			return -1.0;
		}
		if (targetId > closeArray.size()-1){
			return -1.0;
		}
		return lowArray.get(targetId).doubleValue();
	}
}
