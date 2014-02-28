package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.CubieX.TeamAdvantage.TAChatManager;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class ChatCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         TATeam teamOfPlayer = null;

         if(player != null)
         {
            teamOfPlayer = plugin.getTeamOfPlayer(player.getName()); // player is team member

            if(null != teamOfPlayer)
            {
               if(!TAChatManager.teamChat.contains(player.getName())) // player has team chat enabled
               {
                  TAChatManager.teamChat.add(player.getName());
                  player.sendMessage("§a" + "Team-Chat aktiviert!");
               }
               else
               {
                  TAChatManager.teamChat.remove(player.getName());                  
                  player.sendMessage("§6" + "Team-Chat deaktiviert!");
               }               
            }
            else
            {
               player.sendMessage("§6" + "Du bist kein Mitglied eines Teams!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}