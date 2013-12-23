package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                     player.sendMessage(ChatColor.WHITE + targetedPlayer.getName() + ChatColor.GREEN + " wurde aus deinem Team entfernt.");
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim entfernen des Spielers aus dem Team!");
                     player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Kein Spieler " + ChatColor.WHITE + targetedPlayer.getName() + ChatColor.YELLOW + " in diesem Team gefunden!");
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