Stock Backtester and Analysis application for EOD (End-of-day) data. Easy creation of java-based scripts, macros, user lists without touching internal code. Uses hot-reloading to rapidly edit scripts without restarting. Automatically downloads data from Yahoo finance.

Easy to setup - Steps:

1. Download source and open in favorite IDE (I like Eclipse)
2. Add JVM options to run configuration: -d64 -Xms512m -Xmx4g
3. Add all the jars in resource folder to the build path
4. Run starting from the main QuantTrader class
5. Enter 'database download' to download initial set of Yahoo finance EOD data (~10 minutes).
6. Then enter 'backtest test' to verify everything works, or 'help' to see all commands.


Add custom backtesting scripts to: com.qt.modules.backtest.scripts  
Add custom backtesting macros to: com.qt.modules.backtest.macros  
Add custom stock lists (SP500, NASDAQ100, etc) to: /UserLists (then do 'database rebuild') 


Example Script:  
![](https://dl.dropboxusercontent.com/u/6061717/Screenshot%202014-02-19%2014.47.49%20copy.png)

Screenshots:

![](https://dl.dropboxusercontent.com/u/6061717/Screenshot%202014-02-19%2015.53.59.png)

![](https://dl.dropboxusercontent.com/u/6061717/Screenshot%202014-02-19%2015.57.56.png)

![](https://dl.dropboxusercontent.com/u/6061717/Screenshot%202014-02-19%2015.39.29.png)

Please fork and improve :)
