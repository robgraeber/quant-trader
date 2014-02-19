Stock Backtester and Analysis platform for EOD data. Easy creation of java-based scripts, macros, user lists without touching internal code. Uses hot-reloading to rapidly edit scripts without restarting. Automatic data download from Yahoo finance.

Initial Setup Steps:

1. Add JVM options: -d64 -Xms512m -Xmx4g
2. Add all the jars in resource folder to the build path
3. Run starting from the main DataTrader class
4. Enter 'database download' to download initial set of Yahoo finance EOD data (~10 minutes).
5. Then enter 'backtest buyongap' to verify everything works, or 'help' to see all commands.


Add custom backtesting scripts to: com.dt.modules.backtest.scripts
Add custom backtesting macros to: com.dt.modules.backtest.macros
Add custom stock lists (SP500, NASDAQ100, etc) to: /UserLists


Example Script:
![](https://www.dropbox.com/s/ahomkqd4nejq4ax/Screenshot%202014-02-19%2014.47.49.png)

Screenshots:
