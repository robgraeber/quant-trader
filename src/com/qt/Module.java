package com.qt;

import java.util.List;
//DataTrader uses a very flexible module system, 
//that automatically searches for modules in the modules namespace
//Basically the main system just routes and feeds parameters to the modules
//How the module itself is structured is unchecked
public abstract class Module {
	public String name = "Module"; 
	public String triggerWord = "aaa123";
	public String helpText = "module <cmd1> <cmd2> <cmd3>";
	public abstract void activate(List<String> parameters);
	
}
