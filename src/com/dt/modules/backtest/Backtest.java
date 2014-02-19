package com.dt.modules.backtest;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.dt.Module;
import com.dt.OrderManager;
import com.dt.StockDatabase;
import com.dt.classes.Asset;
import com.dt.classes.Stock;
import com.effing.toolkit.Util2;

//The core backtest module
//Add scripts and macros to their respective folders for dynamic loading.
//Macros are reusable scripts that take input parameters.
//See help for usage details.
public class Backtest extends Module {
	private static List<Macro> macroArray = new ArrayList<Macro>(); //holds all initialized macros
	private static List<Script> scriptArray = new ArrayList<Script>(); //holds all initialized scripts
	private static List<Script> backtestArray = new ArrayList<Script>(); //holds scripts being actively tested
	private static TimeSeriesCollection resultDataset = new TimeSeriesCollection();
	private static Date backtestStartDate = new Date();
	private static Date backtestEndDate = new Date();
	
	public Backtest(){
		this.name = "Backtest";
		this.triggerWord = "backtest";
		this.helpText = "backtest <scriptname> <stock:APPL> <list:SP500> <pair:KO:PEP> <2005-2012> <2/3/2006-3/14/2006>";
	}
	@Override
	public void activate(List<String> parameters) {
		if(parameters.size() == 1){
			if(parameters.get(0).equalsIgnoreCase("portfolios")){
				portfolioMe();
				return;
			}else if(parameters.get(0).equalsIgnoreCase("assets")){
				printAssets();
				return;
			}
		}
		try {
			searchForMacros();
			searchForScripts();
			backtestArray = new ArrayList<Script>();
			SimpleDateFormat e = new SimpleDateFormat("MM/dd/yyyy");
			backtestStartDate = e.parse("01/01/1990");
			
			backtestEndDate = new Date();
			mainloop:
			for(String parameter:parameters){
				//Date command check
				if(parameter.matches("([0-9].)(.*)")){
					System.out.println("MATCH: "+parameter);
					SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
					Date startDate = new Date();;
					Date endDate = new Date();
					if(parameter.matches("([0-9]*)")){
						//System.out.println("Plain year");
						dateFormat = new SimpleDateFormat("yyyy");
						startDate = dateFormat.parse(parameter);
					}
					if(parameter.matches("([0-9]*)-([0-9]*)")){
						//System.out.println("year range");
						dateFormat = new SimpleDateFormat("yyyy");
						String[] dateInputs =  parameter.split("-");
						startDate = dateFormat.parse(dateInputs[0]);
						endDate = dateFormat.parse(dateInputs[1]);
					}
					if(parameter.matches("([0-9]*)/([0-9]*)/([0-9]*)")){
						//System.out.println("month day year");
						dateFormat = new SimpleDateFormat("MM/dd/yyyy");
						startDate = dateFormat.parse(parameter);
					}
					if(parameter.matches("([0-9]*)/([0-9]*)/([0-9]*)-([0-9]*)/([0-9]*)/([0-9]*)")){
						//System.out.println("month day year range");
						dateFormat = new SimpleDateFormat("MM/dd/yyyy");
						String[] dateInputs =  parameter.split("-");
						startDate = dateFormat.parse(dateInputs[0]);
						endDate = dateFormat.parse(dateInputs[1]);
					}
					backtestStartDate = startDate;
					backtestEndDate = endDate;
					System.out.println("Start Date: "+startDate);
					System.out.println("End Date: "+endDate);
					continue;
				}
				//Macro command check
				for(Macro macro:macroArray){
					if(parameter.matches(macro.triggerWord+":(.*?)")){
						String[] args =  parameter.split(":");
						Macro m2 = macro.getClass().newInstance();
						//skips the first parameter which is the trigger word
						for(int n = 1; n<args.length; n++){
							m2.param.add(args[n]);	
						}
						backtestArray.add(m2);
						continue mainloop;
					}
				}
				//Script command check
				for(Script script:scriptArray){
					//System.out.println(script.getClass().getSimpleName() +" : "+scriptName);
					if(script.getClass().getSimpleName().equalsIgnoreCase(parameter)){
						backtestArray.add(script);
					}
				}
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		backtestMe();
		createChart();
		System.out.println("For more information, type 'backtest portfolios' or 'backtest assets'");

	}
	private void searchForMacros(){
		macroArray = new ArrayList<Macro>();
		for(Class<?> macroClass:Util2.getClassesForPackage("com.dt.modules.backtest.macros")){
			//System.out.println("Class:"+scriptClass);
			try {
				if(Macro.class.isAssignableFrom(macroClass)){
					macroArray.add((Macro)macroClass.newInstance());
				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void searchForScripts(){
		scriptArray = new ArrayList<Script>();
		for(Class<?> scriptClass:Util2.getClassesForPackage("com.dt.modules.backtest.scripts")){
			//System.out.println("Class:"+scriptClass);
			try {
				if(Script.class.isAssignableFrom(scriptClass)){
					scriptArray.add((Script)scriptClass.newInstance());
				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private String getScriptName(Script script){
		String scriptName = "Default";
		if(script instanceof Macro){
			scriptName = ((Macro)script).getDisplayName();
		}else{
			scriptName = script.getClass().getSimpleName();
		}
		return scriptName;
	}
	//gets the nearest stock row id that is before or equal to target date
	public int getIdForDate(List<Date> dates, Date targetDate) {
		long selectedDiff = Long.MAX_VALUE;
		int selectedId = 0;
		for (int i = 0; i<dates.size(); i++) {
			Date date = dates.get(i);			
			long diff = targetDate.getTime() - date.getTime();
			if (diff < selectedDiff && diff >= 0) {
				selectedId = i;
			 	selectedDiff = diff;
			}
		} 
		return selectedId;
	}
	private void resetPortfolios(){
		for(Script script : backtestArray){
			script.getPortfolio().resetMe();
		}
	}
	//initiates the backtest loop
	private void backtestMe(){
		resetPortfolios();
		//currentId is the day/tick all the stocks are on
		//Corresponds to rows in a database
		//Stock.currentId = 0;
		Stock.currentId = getIdForDate(StockDatabase.getStock("IBM").dateArray, backtestStartDate);
		int targetId = getIdForDate(StockDatabase.getStock("IBM").dateArray, backtestEndDate)+1;
		//uses IBM as the baseline stock in terms of data length
		//If a stock has less data length, then assumes the stock got listed later
		Stock.lengthTarget = StockDatabase.getStock("IBM").getArraySize();
		System.out.println("Number of days: "+(Stock.lengthTarget-Stock.currentId));
		resultDataset = new TimeSeriesCollection();
		for(int i = 0; i < backtestArray.size(); i++){
			Script script = backtestArray.get(i);
			resultDataset.addSeries(new TimeSeries(getScriptName(script)));
		}
		
		while(Stock.currentId < targetId){
			for(Script script : backtestArray){
				script.handleEnterTick(); //lets each algo initiate their trades for the day
			}
			OrderManager.handleEnterTick(); //then order manager fills the trades
			System.out.println("");
			System.out.println("Current Date: "+StockDatabase.getStock("AA").getDate());
			DecimalFormat commaFormat = new DecimalFormat("###,###,###,###,###.##");
			DecimalFormat percentFormat = new DecimalFormat("###.##%");
			for(int i = 0; i < backtestArray.size(); i++){
				Script script = backtestArray.get(i);
				double percentChange = (script.getPortfolio().getTotalValue()/script.getPortfolio().initialFunds)-1;
				resultDataset.getSeries(i).add(new Day(StockDatabase.getStock("AA").getDate()), percentChange);;
				String portfolioString = commaFormat.format(script.getPortfolio().getTotalValue());
				String percentString = percentFormat.format((script.getPortfolio().getTotalValue()/script.getPortfolio().initialFunds-1.0));
			
				if(script.getPortfolio().getTotalValue()>script.getPortfolio().initialFunds){
					System.out.println(getScriptName(script)+" Portfolio Value: $"+ portfolioString +" (+"+percentString+")");
				}else{
					System.out.println(getScriptName(script)+" Portfolio Value: $"+ portfolioString +" ("+percentString+")");
				}
			}
			Stock.currentId++;
		}
		Stock.currentId--;
	}
	//gets stats on all the algorithm portfolios
	private void portfolioMe(){
		System.out.println("Current Date: "+StockDatabase.getStock("AA").getDate());
		for(Script script : backtestArray){
			System.out.println("");
			DecimalFormat commaFormat = new DecimalFormat("###,###,###,###,###.##");
			DecimalFormat percentFormat = new DecimalFormat("###.##%");
			String portfolioString = commaFormat.format(script.getPortfolio().getTotalValue());
			String percentString = percentFormat.format((script.getPortfolio().getTotalValue()/script.getPortfolio().initialFunds-1.0));
			if(script.getPortfolio().getTotalValue()>script.getPortfolio().initialFunds){
				System.out.println(getScriptName(script)+" Portfolio Value: $"+ portfolioString +" (+"+percentString+")");
			}else{
				System.out.println(getScriptName(script)+" Portfolio Value: $"+ portfolioString +" ("+percentString+"%)");
			}
			System.out.println(getScriptName(script)+" Number of Assets Currently Held:"+script.getPortfolio().getAssetArray().size());
		}
		System.out.println("");
		System.out.println("Enter 'backtest assets' for more info");
	}
	private void printAssets(){
		for(Script script : backtestArray){
			System.out.println("");
			System.out.println(getScriptName(script)+" Assets Currently Held:");
			DecimalFormat commaFormat = new DecimalFormat("###,###,###,###,###.##");
			for(Asset asset : script.getPortfolio().getAssetArray()){
				
				System.out.println("Symbol: "+asset.symbol.toUpperCase()+" Market Value: $"+commaFormat.format(StockDatabase.getStock(asset.symbol).getClose()*asset.quantity));
			}

			
			
		}
	}
	private static void createChart(){    
        JFreeChart chart = ChartFactory.createTimeSeriesChart
                ("Cumulative Returns",    			// Title
                 "",                     	// X-Axis label
                 "Cumulative Returns",      // Y-Axis label
                 resultDataset,               	// Dataset
                 true,                      	// Show legend
                 true,              			//tooltips
                 false              			//url
                );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        @SuppressWarnings("serial")
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(){
			Stroke solid = new BasicStroke(1.3f);
          
            public Stroke getItemStroke(int row, int column) {
                   return solid;
            } 
        };

        renderer.setBaseShapesVisible(false);
        renderer.setBaseShapesFilled(false);
        renderer.setBaseStroke(new BasicStroke(1));
        plot.setRenderer(renderer);
        
        ChartFrame frame = new ChartFrame("Cumulative Returns", chart);
		frame.pack();
		frame.setVisible(true);
	}
}
