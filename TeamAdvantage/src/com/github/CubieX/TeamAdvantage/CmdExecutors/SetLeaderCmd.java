package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class SetLeaderCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            OfflinePlayer member = Bukkit.getServer().getOfflinePlayer(args[1]);

            if((null != member)
                  && (member.hasPlayedBefore())) // only known players are allowed
            {
               TATeam teamOfPlayer = plugin.getTeamByLeader(player.getName());

               if(null != teamOfPlayer)
               {
                  if(teamOfPlayer.getMembers().contains(member.getName()))
                  {
                     if(teamOfPlayer.removeMember(member.getName())) // remove member from team (due to DB constraints)
                     {
                        if(teamOfPlayer.setLeader(member.getName())) // re-add player as new team leader
                        {
                           player.sendMessage("§f" + member.getName() + "§a" + " ist jetzt der neue Teamleiter!");

                           if(member.isOnline())
                           {
                              Player newLeader = (Player)member;                                   
                              newLeader.sendMessage("§a" + "Du bist neuer TEAMLEITER des Teams: " + "§f" + teamOfPlayer.getName()  + "§a" + " !");
                           }

                           // set old leader to member status
                           if(teamOfPlayer.addMember(player.getName()))
                           {
                              player.sendMessage("§a" + " Du wurdest zu einem Teammitglied heruntergestuft!");
                           }
                           else
                           {
                              player.sendMessage("§4" + "Datenbank-Fehler beim Eintragen von dir als Mitglied!\n" +
                                    "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage("§4" + "Datenbank-Fehler beim Eintragen dieses Spielers als neuer Teamleiter!\n" +
                                 "Bitte melde das einem Admin.");                                    
                        }
                     }
                     else
                     {
                        player.sendMessage("§4" + "Datenbank-Fehler beim Entfernen des Spielers aus dem Mitglied-Status!\n" +
                              "Bitte melde das einem Admin.");
                     }
                  }
                  else
                  {
                     player.sendMessage("§f" + member.getName() + "§6" + " ist kein Mitglied deines Teams!!");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Du bist kein Teamleiter!");
               }
            }
            else
            {
               player.sendMessage("§6" + "Spieler " + "§f" + member.getName() + "§6" + " war nie auf diesem Server!");
            }                               
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}