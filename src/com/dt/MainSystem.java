//
//	MainSystem.java
//	DataTrader
//
//  Created by Rob Graeber on Nov 3, 2012.
//  Copyright 2012 Rob Graeber. All rights reserved.
//
package com.dt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.effing.toolkit.Util2;

//The central class that handles the console + loops through all the algorithms
public class MainSystem {
	private static Boolean initialized = false;
	private static List<Module> moduleArray = new ArrayList<Module>();
	//Initializer that starts the console loop and loads the modules
	public static void init(){
		if(!initialized){
			initialized = true;
		}else{
			return;
		}
		System.out.println("Activating DataTrader v1.02 -- Backtest Mode");
		System.out.println("Warming up.. This may take a while");
		System.out.println("Searching for Modules..");
		for(String moduleName:Util2.getAllSubDirectories("src/com/dt/modules/")){
			for(Class<?> moduleClass:Util2.getClassesForPackage("com.dt.modules."+moduleName)){	
				try {
					if(Module.class.isAssignableFrom(moduleClass)){
						Module module = (Module)moduleClass.newInstance();
						moduleArray.add(module);
						System.out.println("Loading "+Util2.uppercaseFirstLetter(module.name)+" Module.. Success!");
					}
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
		}		
		inputPrompt();
	}

	//the main console loop
	//most commands for the backtest and database module are case insensitive 
	private static void inputPrompt(){
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			//System.out.println("");
			System.out.println("What do you want to do now?");
			String line = bufferedReader.readLine();
			boolean matchFound = false;
			for(Module module:moduleArray){
				if(line.matches(module.triggerWord+" (.*?)")){
					String[] textInputs =  line.split(" ");
					List<String> parameters = new ArrayList<String>(Arrays.asList(textInputs));
					for(String str:parameters){
						str.trim();
					}
					parameters.remove(0); //removes trigger word
					module.activate(parameters);
					matchFound = true;
					break;
				}else if(line.matches("^"+module.triggerWord+"$")){
					System.out.println(usageTextForModule(module)); //help text
					matchFound = true;
					break;
				}
			}
			if(line.equalsIgnoreCase("quit")||line.equalsIgnoreCase("exit")){
				System.exit(0);
			}else if(!matchFound || line.equalsIgnoreCase("help")){
				for(Module module:moduleArray){
					System.out.println(usageTextForModule(module)); //help text
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		inputPrompt();
	}
	//help text
	private static String usageTextForModule(Module module){
		return Util2.uppercaseFirstLetter(module.name)+" Usage: "+module.helpText;
	} 
	
	
	
}
