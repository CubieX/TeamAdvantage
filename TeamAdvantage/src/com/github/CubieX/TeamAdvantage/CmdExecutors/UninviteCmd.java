package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class UninviteCmd implements ISubCmdExecutor
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
               OfflinePlayer targetedPlayer = Bukkit.getServer().getOfflinePlayer(args[1]); // to get correct case of name if player has played before

               if(teamOfLeader.getInvitations().contains(targetedPlayer.getName()))
               {
                  if(teamOfLeader.uninvitePlayer(targetedPlayer.getName()))
                  {
                     player.sendMessage(ChatColor.GREEN + "Einladung an " + ChatColor.WHITE + targetedPlayer.getName() + ChatColor.GREEN + " wurde geloescht.");
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Loeschen der Einladung dieses Spielers!");
                     player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.WHITE + targetedPlayer.getName() + ChatColor.YELLOW + " hat noch keine Einladung erhalten.");
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