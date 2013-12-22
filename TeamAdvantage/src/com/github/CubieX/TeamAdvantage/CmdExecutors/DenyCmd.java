package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class DenyCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         TATeam teamByName = null;
         TATeam teamByLeader = null;
         OfflinePlayer offPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
         Player targetPlayer = null;

         if(offPlayer.hasPlayedBefore())
         {
            targetPlayer = (Player)offPlayer; // given player is known to the server
         }

         if(player != null)
         {
            teamByName = plugin.getTeamByName(args[1]);

            if(null != teamByName) // Player is trying to deny a team invitation using the team name
            {
               if(teamByName.getInvitations().contains(player.getName())) // a team invitation for this player from given team is pending
               {
                  if(teamByName.uninvitePlayer(player.getName()))
                  {
                     player.sendMessage(ChatColor.GREEN + "Du hast die Einladung in das Team " + ChatColor.WHITE + teamByName.getName() + ChatColor.GREEN + " abgelehnt.");
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Ablehnen der Einladung in dieses Team!");
                     player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                  }                          

                  teamByName.uninvitePlayer(player.getName());
                  teamByName.deleteJoinTeamRequest(player.getName());                           
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Du hast keine Einladung fuer dieses Team!");                           
               }

               return;
            }

            teamByLeader = plugin.getTeamByLeader(player.getName());

            if(null != teamByLeader)  // issuing player is leader of a team
            {
               OfflinePlayer targetedPlayer = Bukkit.getServer().getOfflinePlayer(args[1]); // to get correct case of name if player has played before

               if(teamByLeader.getRequests().contains(targetedPlayer.getName())) // a join request of a player for this team is pending and the leader is denying by using the player name
               {
                  if(teamByLeader.deleteJoinTeamRequest(targetedPlayer.getName()))
                  {
                     player.sendMessage(ChatColor.GREEN + "Die Aufnahme-Anfrage von Spieler " + ChatColor.WHITE + targetedPlayer.getName() + ChatColor.GREEN + " wurde abgelehnt.");

                     if((null != targetPlayer && (targetPlayer.isOnline())))
                     {
                        targetPlayer.sendMessage(ChatColor.GREEN + "Deine Aufnahme-Anfrage an Team " + ChatColor.WHITE + teamByLeader.getName() + ChatColor.GREEN + " wurde abgelehnt.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Ablehnen der Einladung in dieses Team!");
                     player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                  }

                  teamByLeader.uninvitePlayer(args[1]);
                  teamByLeader.deleteJoinTeamRequest(args[1]);
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Dieser Spieler hat keine Aufnahme-Anfrage an dein Team gestellt!");
               }

               return;
            }

            // if this is reached, something went wrong
            if(null != targetPlayer)
            {
               player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");                        
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Kein Spieler mit diesem Namen gefunden!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}