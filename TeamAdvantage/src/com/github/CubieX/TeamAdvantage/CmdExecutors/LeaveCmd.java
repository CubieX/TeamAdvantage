package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class LeaveCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(null != player)
         {
            TATeam team = plugin.getTeamOfPlayer(player.getName());

            if(null != team)
            {
               if(team.removeMember(player.getName()))
               {
                  player.sendMessage(ChatColor.GREEN + "Du bist aus dem Team " + ChatColor.WHITE + team.getName() +ChatColor.GREEN + " ausgetreten.");
               }
               else
               {
                  player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Verlassen des Teams!\n" +                            
                        "Bitte melde das einem Admin.");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Du bist kein Mitglied eines Teams!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}