package com.github.CubieX.TeamAdvantage.CmdExecutors;

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
                     player.sendMessage("§a" + "Dein Team heisst jetzt: " + "§f" + args[1] + "§a" + ".");
                  }
                  else
                  {
                     player.sendMessage("§4" + "Datenbank-Fehler beim Setzen des Teamnamens!\n" +
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Der Teamname muss zwischen 4 und 20 Zeichen lang sein\n" +
                        "und darf nur folgende Zeichen enthalten:\n" + "§f" + "a-z, A-Z, 0-9, _\n" +
                        "(keine Leerzeichen und kein Spielername)");
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