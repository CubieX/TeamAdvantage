package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
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
                           player.sendMessage("§a" + "Aufnahmeanfrage an Team " + "§f" + team.getName() + "§a" + " versendet!");

                           Player teamLeader = Bukkit.getServer().getPlayer(team.getLeader());

                           if((null != teamLeader) && (teamLeader.isOnline()))
                           {
                              player.sendMessage("§a" + "Du hast eine Aufnahmeanfrage von " + "§f" + player.getName() + "§a" + " erhalten.");
                           }
                        }
                        else
                        {
                           player.sendMessage("§4" + "Datenbank-Fehler beim Beantragen einer Aufnahme in dieses Team!\n" +
                                 "Bitte melde das einem Admin.");
                        }
                     }
                     else
                     {
                        player.sendMessage("§6" + "Du hast bereits eine Aufnahmeanfrage an Team " + "§f" + team.getName() + "§a" + " geschickt.");
                     }
                  }
                  else
                  {
                     player.sendMessage("§6" + "Du bist bereits in diesem Team.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Du bist der Teamleiter.");
               }
            }
            else
            {
               player.sendMessage("§6" + "Kein Team " + "§f" + args[1] + "§6" + " gefunden!");
               player.sendMessage("§6" + "Verwende " + "§f" + "/ta list"  + "§6" + " um eine Liste der Teams zu erhalten.");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}