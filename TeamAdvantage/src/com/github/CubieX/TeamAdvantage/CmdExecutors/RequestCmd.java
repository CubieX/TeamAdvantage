package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class RequestCmd implements ISubCmdExecutor
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
                  if(!team.getMembers().contains(player.getName()))
                  {
                     if(!team.getRequests().contains(player.getName()))
                     {
                        if(team.addJoinTeamRequest(player.getName()))
                        {
                           player.sendMessage(ChatColor.GREEN + "Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " versendet!");

                           Player teamLeader = Bukkit.getServer().getPlayer(team.getLeader());

                           if((null != teamLeader) && (teamLeader.isOnline()))
                           {
                              player.sendMessage(ChatColor.GREEN + "Du hast eine Aufnahmeanfrage von " + ChatColor.WHITE + player.getName() + ChatColor.GREEN + " erhalten.");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Beantragen einer Aufnahme in dieses Team!\n" +
                                 "Bitte melde das einem Admin.");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du hast bereits eine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " geschickt.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Du bist bereits in diesem Team.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Du bist der Teamleiter.");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
               player.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}