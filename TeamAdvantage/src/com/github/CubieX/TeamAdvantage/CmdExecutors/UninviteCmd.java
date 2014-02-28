package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
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
                     player.sendMessage("§a" + "Einladung an " + "§f" + targetedPlayer.getName() + "§a" + " wurde geloescht.");
                  }
                  else
                  {
                     player.sendMessage("§4" + "Datenbank-Fehler beim Loeschen der Einladung dieses Spielers!\n" +
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage("§f" + targetedPlayer.getName() + "§6" + " hat noch keine Einladung erhalten.");
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