package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class SetNameCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());

            if(null != teamOfLeader)
            {
               if(plugin.checkTeamName(args[1]))
               {
                  if(teamOfLeader.setName(args[1]))
                  {
                     player.sendMessage(ChatColor.GREEN + "Dein Team heisst jetzt: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + ".");
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Setzen des Teamnamens!\n" +
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Der Teamname muss zwischen 4 und 20 Zeichen lang sein\n" +
                        "und darf nur folgende Zeichen enthalten:\n" + ChatColor.WHITE + "a-z, A-Z, 0-9, _\n" +
                        "(keine Leerzeichen und kein Spielername)");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}