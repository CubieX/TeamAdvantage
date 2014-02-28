package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class RemoveCmd implements ISubCmdExecutor
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
               OfflinePlayer targetedPlayer = Bukkit.getServer().getOfflinePlayer(args[1]); // to get correct case if player has played before

               if(team.getMembers().contains(targetedPlayer.getName()))
               {
                  if(team.removeMember(targetedPlayer.getName()))
                  {
                     player.sendMessage("§f" + targetedPlayer.getName() + "§a" + " wurde aus deinem Team entfernt.");
                  }
                  else
                  {
                     player.sendMessage("§4" + "Datenbank-Fehler beim Entfernen des Spielers aus dem Team!\n" + 
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Kein Spieler " + "§f" + targetedPlayer.getName() + "§6" + " in diesem Team gefunden!");
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