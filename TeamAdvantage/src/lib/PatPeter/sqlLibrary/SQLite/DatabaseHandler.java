package lib.PatPeter.sqlLibrary.SQLite;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

import com.github.CubieX.TeamAdvantage.AsyncQueryResultRetrievedEvent;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class DatabaseHandler {
   /*
    * @author: alta189
    * 
    */

   private sqlCore core;
   private Connection connection;
   private File SQLFile;
   private TeamAdvantage plugin;
   private Logger log;

   public DatabaseHandler(sqlCore core, File SQLFile, TeamAdvantage plugin, Logger log)
   {
      this.core = core;
      this.SQLFile = SQLFile;
      this.plugin = plugin;
      this.log = log;
   }

   public Connection getConnection() {
      if (connection == null) {
         initialize();
      }
      return connection;
   }

   public void closeConnection() {
      if (this.connection != null)
         try {
            this.connection.close();
         } catch (SQLException ex) {
            this.core.writeError("Error on Connection close: " + ex, true);
         }
   }

   public Boolean initialize() {
      try {
         Class.forName("org.sqlite.JDBC");
         connection = DriverManager.getConnection("jdbc:sqlite:" + SQLFile.getAbsolutePath());
         return true;
      } catch (SQLException ex) {
         core.writeError("SQLite exception on initialize " + ex, true);
      } catch (ClassNotFoundException ex) {
         core.writeError("You need the SQLite library " + ex, true);
      }
      return false;
   }

   public Boolean createTable(String query) {
      try {
         if (query == null) { core.writeError("SQL Create Table query empty.", true); return false; }

         Statement statement = connection.createStatement();
         statement.execute(query);
         return true;
      } catch (SQLException ex){
         core.writeError(ex.getMessage(), true);
         return false;
      }
   }

   /**
    * Executes a SELECT query synchronously.
    * This may block the main thread if the connection to the DB is slow
    * or the query takes long to process.
    * For avoiding lag, use the sqlQueryCallback instead.
    * 
    * @param query the raw SELECT query string to be executed
    * 
    * @return resSet the result set with the queried information
    * */
   public ResultSet sqlQuery(String query)
   {
      try
      {
         Connection connection = getConnection();
         Statement statement = connection.createStatement();

         ResultSet result = statement.executeQuery(query);

         return result;
      }
      catch (SQLException ex)
      {
         if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
            return retryResult(query);
         }else{
            core.writeError("Error at SQL Query: " + ex.getMessage(), false);
         }			
      }
      return null;
   }

   /**
    * Executes a SELECT query via an async callback method and delivers
    * the ResultSet through a Future object by firing a custom Event.
    * 
    * @param query the raw SELECT query string to be executed
    * 
    * @return resSet the result set with the queried information
    * */
   public ResultSet sqlQueryCallback(final String query)
   {
      Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            FutureTask<ResultSet> future = new FutureTask<ResultSet>(new Callable<ResultSet>()
                  {
               @Override
               public ResultSet call()
               {
                  if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "doAsyncSelectQuery running its callable and waiting for result...");}

                  try
                  {
                     Connection connection = getConnection();
                     Statement statement = connection.createStatement();

                     ResultSet result = statement.executeQuery(query);

                     return result;
                  }
                  catch (SQLException ex)
                  {
                     if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked"))
                     {
                        return retryResult(query);
                     }
                     else
                     {
                        core.writeError("Error at SQL Query: " + ex.getMessage(), false);
                     }        
                  }
                  // if this is taking longer than future.get(MAX_RETRIEVAL_TIME) specifies, the return value will come too late and
                  // no one will get it

                  if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "doAsyncSelectQuerys callable has retrieved the result. Returning the result now...");}

                  return null;
               }});

            executor.execute(future); // start the callable task to retrieve the result

            if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Async tasks main thread working while waiting for DB ResultSet...work...work...");}

            try
            {
               ResultSet resSet = null;

               try
               {
                  resSet = future.get(TeamAdvantage.MAX_RETRIEVAL_TIME, TimeUnit.MILLISECONDS); // will wait until result is ready, but will return if MAX_RETRIEVAL_TIME has expired                  
               }
               catch (TimeoutException e)
               {
                  resSet = null;
                  //e.printStackTrace();
               }
               finally
               {
                  executor.shutdown(); // shutdown executor service after Callable has finished and returned its value (will block current task until all threads in the pool have finished!)
                  // Using this order will make sure, the task will return after given MAX_RETRIEVAL_TIME and not block forever, if the DB connection is dead or slow
               }

               // future.get() will block this thread until the result is ready
               if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "ResultSet aquired. Now firing QueryResultRetrievedEvent...");}

               // fire custom query event ================================================                                
               AsyncQueryResultRetrievedEvent qrreEvent = new AsyncQueryResultRetrievedEvent(null, resSet); // Create the event to deliver resultSet
               plugin.getServer().getPluginManager().callEvent(qrreEvent); // fire Event
               //==========================================================================                 
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
            catch (ExecutionException e)
            {        
               TeamAdvantage.log.severe(TeamAdvantage.logPrefix + e.getMessage());
            }

            if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "doAsyncSelectQuery task finished.");}
         }
      });

      return null;
   }

   public void insertQuery(final String query)
   { // execute query asynchronous, so main thread is not blocked by SQL database operations. (Insert, Update, Delete)
      Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            long startTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();

            try
            {
               Connection connection = getConnection();
               Statement statement = connection.createStatement();

               statement.executeQuery(query);
            }
            catch (SQLException ex)
            {

               if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                  retry(query);
               }else{
                  if (!ex.toString().contains("not return ResultSet")) core.writeError("Error at SQL INSERT Query: " + ex, false);
               }
            }

            if(TeamAdvantage.debug)
            {
               log.info(TeamAdvantage.logPrefix + "INSERT-Query executed in: " + (((Calendar)Calendar.getInstance()).getTimeInMillis() - startTime) + " milliseconds.");
            }
         }
      });
   }

   public void updateQuery(final String query)
   {
      // execute query asynchronous, so main thread is not blocked by SQL database operations. (Insert, Update, Delete)
      Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            long startTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();

            try {
               Connection connection = getConnection();
               Statement statement = connection.createStatement();

               statement.executeQuery(query);                    

            } catch (SQLException ex) {
               if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                  retry(query);
               }else{
                  if (!ex.toString().contains("not return ResultSet")) core.writeError("Error at SQL UPDATE Query: " + ex, false);
               }
            }

            if(TeamAdvantage.debug)
            {
               log.info(TeamAdvantage.logPrefix + "UPDATE-Query executed in: " + (((Calendar)Calendar.getInstance()).getTimeInMillis() - startTime) + " milliseconds.");
            }
         }
      });		
   }

   public void deleteQuery(final String query)
   {   // execute query asynchronous, so main thread is not blocked by SQL database operations. (Insert, Update, Delete)
      Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            long startTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();

            try
            {
               Connection connection = getConnection();
               Statement statement = connection.createStatement();

               statement.executeQuery(query);	                    
            }
            catch (SQLException ex) {
               if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                  retry(query);
               }else{
                  if (!ex.toString().contains("not return ResultSet")) core.writeError("Error at SQL DELETE Query: " + ex, false);
               }
            }

            if(TeamAdvantage.debug)
            {
               log.info(TeamAdvantage.logPrefix + "DELETE-Query executed in: " + (((Calendar)Calendar.getInstance()).getTimeInMillis() - startTime) + " milliseconds.");
            }
         }
      });
   }

   public Boolean wipeTable(String table) {
      try {
         if (!core.checkTable(table)) {
            core.writeError("Error at Wipe Table: table, " + table + ", does not exist", true);
            return false;
         }
         Connection connection = getConnection();
         Statement statement = connection.createStatement();
         String query = "DELETE FROM " + table + ";";
         statement.executeQuery(query);

         return true;
      } catch (SQLException ex) {
         if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
            //retryWipe(query);
         }else{
            if (!ex.toString().contains("not return ResultSet")) core.writeError("Error at SQL WIPE TABLE Query: " + ex, false);
         }
         return false;
      }
   }


   public Boolean checkTable(String table) {
      DatabaseMetaData dbm;
      try {
         dbm = this.getConnection().getMetaData();
         ResultSet tables = dbm.getTables(null, null, table, null);
         if (tables.next()) {
            return true;
         }
         else {
            return false;
         }
      } catch (SQLException e) {			
         core.writeError("Failed to check if table \"" + table + "\" exists: " + e.getMessage(), true);
         return false;
      }

   }

   private ResultSet retryResult(String query) {
      Boolean passed = false;

      while (!passed) {
         try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(query);

            passed = true;

            return result;
         } catch (SQLException ex) {

            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
               passed = false;
            }else{
               core.writeError("Error at SQL Query: " + ex.getMessage(), false);
            }
         }
      }

      return null;
   }

   private void retry(String query) {
      Boolean passed = false;

      while (!passed) {
         try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            statement.executeQuery(query);

            passed = true;

            return;
         } catch (SQLException ex) {

            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked") ) {
               passed = false;
            }else{
               core.writeError("Error at SQL Query: " + ex.getMessage(), false);
            }
         }
      }

      return;
   }
}
