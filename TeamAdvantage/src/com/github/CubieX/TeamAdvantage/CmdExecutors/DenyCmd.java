package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
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
                  if(teamByName.uninvitePlayer(player.getName())
                        && teamByName.deleteJoinTeamRequest(player.getName())) // delete invitation and request for/from player
                  {
                     player.sendMessage("§a" + "Einladung in das Team " + "§f" + teamByName.getName() + "§a" + " abgelehnt.");
                  }
                  else
                  {
                     player.sendMessage("§4" + "Datenbank-Fehler beim Ablehnen der Einladung in dieses Team!\n" +
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Du hast keine Einladung fuer dieses Team!");                           
               }

               return;
            }

            teamByLeader = plugin.getTeamByLeader(player.getName());

            if(null != teamByLeader)  // issuing player is leader of a team
            {
               OfflinePlayer targetedPlayer = Bukkit.getServer().getOfflinePlayer(args[1]); // to get correct case of name if player has played before

               if(teamByLeader.getRequests().contains(targetedPlayer.getName())) // a join request of a player for this team is pending and the leader is denying by using the player name
               {
                  if(teamByLeader.deleteJoinTeamRequest(targetedPlayer.getName())
                        && teamByLeader.uninvitePlayer(player.getName())) // delete request and invitation from/for player
                  {
                     player.sendMessage("§a" + "Aufnahme-Anfrage von Spieler " + "§f" + targetedPlayer.getName() + "§a" + " abgelehnt.");

                     if((null != targetPlayer && (targetPlayer.isOnline())))
                     {
                        targetPlayer.sendMessage("§a" + "Deine Aufnahme-Anfrage an Team " + "§f" + teamByLeader.getName() + "§a" + " wurde abgelehnt.");
                     }
                  }
                  else
                  {
                     player.sendMessage("§4" + "Datenbank-Fehler beim Ablehnen der Aufnahme-Anfrage an dieses Team!\n" +
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Dieser Spieler hat keine Aufnahme-Anfrage an dein Team gestellt!");
               }

               return;
            }

            // if this is reached, something went wrong
            if(null != targetPlayer)
            {
               player.sendMessage("§6" + "Du bist kein Teamleiter!");                        
            }
            else
            {
               player.sendMessage("§6" + "Kein Spieler mit diesem Namen gefunden!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}