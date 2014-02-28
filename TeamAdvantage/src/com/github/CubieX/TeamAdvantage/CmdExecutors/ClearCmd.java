package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class ClearCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam team = plugin.getTeamByLeader(player.getName());

            if(null != team)
            {
               if(team.clearMembers())
               {
                  player.sendMessage("§a" + "ALLE Mitglieder wurden aus deinem Team " + "§f" + team.getName() +
                        "§a" + " entfernt.");
               }
               else
               {
                  player.sendMessage("§4" + "Datenbank-Fehler beim Leeren des Teams!\n" +                           
                        "Bitte melde das einem Admin.");
               }
            }
            else
            {
               player.sendMessage("§6" + "Du bist kein Teamleiter!");
            }                     
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}