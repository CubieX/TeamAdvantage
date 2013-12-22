package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class DeleteCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam applicableTeam = null;

            for(TATeam team : TeamAdvantage.teams)
            {
               if(team.getName().equalsIgnoreCase(args[1]))
               {
                  applicableTeam = team;
                  break;
               }
            }

            if(null != applicableTeam)
            {
               if(player.isOp()
                     || player.hasPermission("teamadvantage.admin")
                     || player.getName().equals(applicableTeam.getLeader()))
               {
                  if(plugin.getSQLman().sqlDeleteTeam(applicableTeam))
                  {
                     player.sendMessage(ChatColor.GREEN + "Team: " + ChatColor.WHITE + applicableTeam.getName() + ChatColor.GREEN + " wurde geloescht!");                             
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Loeschen des Teams!");
                     player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung dieses Team zu loeschen!");
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
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can create teams!");
         }
      }
   }
}