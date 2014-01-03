package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class UnrequestCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam team = plugin.getTeamByName(args[1]);

            if(null != team)
            {
               // check if player is not the leader of the requested team
               if(!team.getLeader().equals(player.getName()))
               {
                  if(team.getRequests().contains(player.getName()))
                  {
                     if(team.deleteJoinTeamRequest(player.getName()))
                     {
                        player.sendMessage(ChatColor.GREEN + "Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " wurde geloescht.");
                     }
                     else
                     {
                        player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Beantragen einer Aufnahme in dieses Team!\n" +
                              "Bitte melde das einem Admin.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Du hast noch keine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.YELLOW + " geschickt.");
                  }
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}