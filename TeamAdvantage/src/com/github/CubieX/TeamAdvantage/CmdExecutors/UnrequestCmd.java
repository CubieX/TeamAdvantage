package com.github.CubieX.TeamAdvantage.CmdExecutors;

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
                        player.sendMessage("§a" + "Aufnahmeanfrage an Team " + "§f" + team.getName() + "§a" + " wurde geloescht.");
                     }
                     else
                     {
                        player.sendMessage("§4" + "Datenbank-Fehler beim Beantragen einer Aufnahme in dieses Team!\n" +
                              "Bitte melde das einem Admin.");
                     }
                  }
                  else
                  {
                     player.sendMessage("§6" + "Du hast noch keine Aufnahmeanfrage an Team " + "§f" + team.getName() + "§6" + " geschickt.");
                  }
               }
            }
            else
            {
               player.sendMessage("§6" + "Kein Team " + "§f" + args[1] + "§6" + " gefunden!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}