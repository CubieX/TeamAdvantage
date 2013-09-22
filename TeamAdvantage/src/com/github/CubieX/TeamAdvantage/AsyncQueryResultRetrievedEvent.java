package com.github.CubieX.TeamAdvantage;

import java.sql.ResultSet;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncQueryResultRetrievedEvent extends Event
{
   private static final HandlerList handlers = new HandlerList();
   private ResultSet resSet = null;
   private CommandSender sender = null;

   //Constructor
   public AsyncQueryResultRetrievedEvent(CommandSender sender, ResultSet resSet)
   {
      this.sender = sender;
      this.resSet = resSet;
   }

   public CommandSender getSender()
   {
      return (this.sender);
   }
   
   public ResultSet getResultSet()
   {
      return (this.resSet);
   }

   public HandlerList getHandlers()
   {
      return handlers;
   }

   public static HandlerList getHandlerList()
   {
      return handlers;
   }
}
