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
![](https://cloud.githubusercontent.com/assets/2387719/5312322/81460100-7c25-11e4-928b-b868ae26e7bb.png)

Screenshots:

![](https://cloud.githubusercontent.com/assets/2387719/5312324/8a1dfcce-7c25-11e4-974f-8cd5393e8bbd.png)

![](https://cloud.githubusercontent.com/assets/2387719/5312325/8e59b954-7c25-11e4-9ad6-d45a4423bb08.png)

![](https://cloud.githubusercontent.com/assets/2387719/5312328/93cd961c-7c25-11e4-9fe9-93e3aaeb8a5d.png)

Please fork and improve :)
