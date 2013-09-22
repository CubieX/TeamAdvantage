package com.github.CubieX.TeamAdvantage;

import lib.PatPeter.sqlLibrary.SQLite.sqlCore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TAEntityListener implements Listener
{
   private TeamAdvantage plugin = null;
   private sqlCore sqlMan = null;

   public TAEntityListener(TeamAdvantage plugin, sqlCore sqlMan)
   {        
      this.plugin = plugin;
      this.sqlMan = sqlMan;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   //================================================================================================    
   /*@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // event has NORMAL priority and will be skipped if it has been cancelled before
   public void onAsyncQueryResultRetrievedEvent(final AsyncQueryResultRetrievedEvent e)
   {
      // Beware! This event is asynchronously called!
      // so make sure to use a sync task if you are accessing any Bukkit API methods
      Bukkit.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         @Override
         public void run()
         {            
            if(null != e.getResultSet())
            {
               // do stuff with resultSet
            }
            else
            {
               // BEWARE: sender may be null!
               // e.getSender().sendMessage(TeamAdvantage.logPrefix + "Request timed out! (" + TeamAdvantage.MAX_RETRIEVAL_TIME + " ms)");
            }
         }

      });
   }*/
}
