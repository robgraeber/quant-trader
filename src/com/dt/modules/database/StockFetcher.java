package com.dt.modules.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
public class StockFetcher {  
	
	//from a txt list of Stocks, fetches all the csv's via multi-threading and saves to disk, then parses it into database
		public static void downloadStocks(String fileName, String databaseName){
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(fileName));
				String line;
				ExecutorService pool = Executors.newFixedThreadPool(10);
				while ((line = br.readLine()) != null) {
					pool.submit(new StockFetchTask(line.trim(), databaseName));
				}
				pool.shutdown();
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				br.close();
				StockFetcher.parseStockIntoDatabase("csv/"+databaseName+"/",databaseName);
				FileUtils.deleteDirectory(new File("csv/"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	//downloads single stock csv from yahoo finance and saves to disk
	public static void getStockCSV(String symbol, String databaseName){
		symbol = symbol.toUpperCase();
		URL url;
		try {
			url = new URL("http://ichart.finance.yahoo.com/table.csv?s="+symbol+"&g=d&a=1&b=1&c=2000&ignore=.csv");
			File file = new File("csv/"+databaseName+"/"+symbol+".csv");
			FileUtils.copyURLToFile(url, file, 3000, 3000);
			System.out.println("Downloaded "+symbol+".csv for "+databaseName);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
		
	}
	//Searches for csv folder, then parses all csv's and inserts into database in one big transaction
	//Afterwards deletes all the csv's
	public static void parseStockIntoDatabase(String dirName, String databaseName) {
		(new File(databaseName)).delete(); //deletes database then rebuilds from files
		File fileDir = new File(dirName);
		Collection<File> fileList = FileUtils.listFiles(fileDir, FileFileFilter.FILE, 
				  DirectoryFileFilter.DIRECTORY);
		// load the sqlite-JDBC driver using the current class loader
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = null;
		    connection = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      statement.executeUpdate("BEGIN");
	      for (File file: fileList){	
				Pattern MY_PATTERN = Pattern.compile(dirName+"(.*?).csv");
				Matcher m = MY_PATTERN.matcher(file.toString());
				String symbol = "";
				while (m.find()) {
					symbol = m.group(1);
				    // s now contains "BAR"
				}
				symbol = symbol.toUpperCase();
				String formattedSymbol = symbol;
				formattedSymbol = formattedSymbol.replace("-", "_");
				formattedSymbol = formattedSymbol.replace(".", "_");
				// Retrieve CSV File

				BufferedReader br = new BufferedReader(new FileReader(file));  

				List<String> csvLines = new ArrayList<String>();
				String line;
				while ((line = br.readLine()) != null){
					csvLines.add(line);
				};
				br.close();
				csvLines.remove(0); //removes column titles
				Collections.reverse(csvLines);
				  statement.executeUpdate("drop table if exists '"+formattedSymbol+"'");
				  statement.executeUpdate("create table '"+formattedSymbol+"' (id INTEGER PRIMARY KEY, day TEXT, open DECIMAL(8,3), high DECIMAL(8,3), low DECIMAL(8,3), close DECIMAL(8,3), volume BIGINT, adj_close DECIMAL(8,3))");
				  //statement.executeUpdate("ALTER TABLE "+formattedSymbol+" DROP COLUMN adj_close;");
				  for(int i=0;i<csvLines.size();i++) {
					  String[] csv = csvLines.get(i).split(",");
					  if(csv.length != 7){
						  System.out.println("Invalid CSV - "+symbol+".csv"); //probably a damaged file
						  continue;
					  }
//					  SimpleDateFormat csvFormat = new SimpleDateFormat("yyyy-MM-dd");
//					  SimpleDateFormat databaseFormat = new SimpleDateFormat("yyyy-MM-dd");
//					  String date = "1970-10-21 10:00:00.000";
//					  try {
//					      date = databaseFormat.format(csvFormat.parse(csv[0]));
//					  } catch (ParseException e) {
//					      e.printStackTrace();
//					  }
					  
					  statement.executeUpdate("insert into '"+formattedSymbol+"' values(NULL, '"+csv[0]+"', "+csv[1]+", "+csv[2]+", "+csv[3]+", "+csv[4]+", "+csv[5]+", "+csv[6]+")");
				  }
				  statement.executeUpdate("UPDATE '"+formattedSymbol+"' SET open = open * adj_close/close WHERE open IS NOT NULL;");
				  statement.executeUpdate("UPDATE '"+formattedSymbol+"' SET high = high * adj_close/close WHERE high IS NOT NULL;");
				  statement.executeUpdate("UPDATE '"+formattedSymbol+"' SET low = low * adj_close/close WHERE low IS NOT NULL;");
				  statement.executeUpdate("UPDATE '"+formattedSymbol+"' SET close = adj_close WHERE close IS NOT NULL;");
				  
				  System.out.println("Loading "+formattedSymbol+" into "+databaseName);
	      }
	      statement.executeUpdate("COMMIT");
	      System.out.println("Commited "+fileList.size()+" stocks/tables into "+databaseName);
	      if(connection != null) connection.close();
	      FileUtils.deleteDirectory(fileDir);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	//Searches for all txt lists of stocks, ie SP500 or other arbitary lists
	//Then inserts it into stock_lists.db in it's own table of the same name (all caps)
	//i.e. sp500.txt -> stock_lists.db/SP500
	public static void parseListsIntoDatabase(String dirName, String databaseName) {
		(new File(databaseName)).delete(); //deletes database then rebuilds from files
		File fileDir = new File(dirName);
		Collection<File> fileList = FileUtils.listFiles(fileDir, FileFileFilter.FILE, 
				  DirectoryFileFilter.DIRECTORY);
		// load the sqlite-JDBC driver using the current class loader
		try {
			Class.forName("org.sqlite.JDBC");
			Connection connection = null;
		    connection = DriverManager.getConnection("jdbc:sqlite:"+databaseName);
		      Statement statement = connection.createStatement();
		      statement.setQueryTimeout(30);  // set timeout to 30 sec.
		      statement.executeUpdate("BEGIN");
	      for (File file: fileList){	
				Pattern MY_PATTERN = Pattern.compile(dirName+"(.*?).txt");
				Matcher m = MY_PATTERN.matcher(file.toString());
				String listName = "";
				while (m.find()) {
					listName = m.group(1);
				    // s now contains "BAR"
				}
				//symbol = symbol.toUpperCase();
				String formattedListName = listName.toUpperCase();
				formattedListName = formattedListName.replace("-", "_");
				formattedListName = formattedListName.replace(".", "_");
				// Retrieve CSV File

				BufferedReader br = new BufferedReader(new FileReader(file));  

				List<String> symbols = new ArrayList<String>();
				String line;
				while ((line = br.readLine()) != null){
					symbols.add(line.trim());
				};
				br.close();
				  statement.executeUpdate("drop table if exists '"+formattedListName+"'");
				  statement.executeUpdate("create table '"+formattedListName+"' (id INTEGER PRIMARY KEY, symbol TEXT)");
				  //statement.executeUpdate("ALTER TABLE "+formattedSymbol+" DROP COLUMN adj_close;");
				  for(int i=0;i<symbols.size();i++) {
					  
					  statement.executeUpdate("insert into '"+formattedListName+"' values(NULL, '"+symbols.get(i)+"')");
				  }
				  System.out.println("Loading "+formattedListName+" ("+symbols.size()+" symbols) into "+databaseName);
	      }
	      statement.executeUpdate("COMMIT");
	      System.out.println("Commited "+fileList.size()+" lists/tables into "+databaseName);
	      if(connection != null) connection.close();
	      //FileUtils.deleteDirectory(fileDir);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}