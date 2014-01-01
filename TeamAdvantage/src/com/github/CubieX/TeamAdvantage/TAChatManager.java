package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TAChatManager
{
   private TASchedulerHandler schedHandler = null;  
   public static ArrayList<String> teamChat = new ArrayList<String>();

   public TAChatManager(TASchedulerHandler schedHandler)
   {
      this.schedHandler = schedHandler;
   }

   /**
    * <b>Set recipients for currently handled player chat message</b><br>   
    * Do NOT call this directly! May only be called by AsyncPlayerChatEvent
    * 
    * @param recipients Set of recipients
    * */
   public void handleTeamChat(Player sender, TATeam teamOfSender, String message)
   {
      String playerGroup = TeamAdvantage.perm.getPrimaryGroup(sender);
      String chatPrefix = TeamAdvantage.chat.getGroupPrefix(sender.getWorld(), playerGroup);
      String chatPrefixFormatted = "";

      if(null != chatPrefix)
      {
         chatPrefixFormatted = ChatColor.translateAlternateColorCodes('&', chatPrefix); // converts vanilla "&7" color codes to "§7" for bukkit
      }

      String msgFormatted = message;

      if(null != chatPrefix)
      {
         msgFormatted = ChatColor.LIGHT_PURPLE + "[" + ChatColor.GRAY + teamOfSender.getTag() + ChatColor.LIGHT_PURPLE + "]" +
               chatPrefixFormatted + sender.getName() + ":" + ChatColor.GRAY + message;
      }

      schedHandler.handleTeamChat(sender, teamOfSender, msgFormatted);
   }
}
