package com.dt.modules.backtest;

import java.util.ArrayList;
import java.util.List;



//Basically these are scripts that can take parameters, unique to the backtest module
//i.e. "backtest stock:AAPL script1 script2" 
//unlimited parameters are possible, i.e. "backtest macro1:AAPL:GOOG:IBM:5:3"
public abstract class Macro extends Script {
	public List<String> param = new ArrayList<String>();
	public String triggerWord = "aaa123"; //the command
	
	public abstract void handleEnterTick();
	
	//overwrite this to change it
	public String getDisplayName() {
		if(param.size()>0){
			return param.get(0).toUpperCase();
		}else{
			return "NULL";
		}
	}

}
