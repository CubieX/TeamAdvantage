package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                           player.sendMessage(ChatColor.WHITE + member.getName() + ChatColor.GREEN + " ist jetzt der neue Teamleiter!");

                           if(member.isOnline())
                           {
                              Player newLeader = (Player)member;                                   
                              newLeader.sendMessage(ChatColor.GREEN + "Du bist neuer TEAMLEITER des Teams: " + ChatColor.WHITE + teamOfPlayer.getName()  + ChatColor.GREEN + " !");
                           }

                           // set old leader to member status
                           if(teamOfPlayer.addMember(player.getName()))
                           {
                              player.sendMessage(ChatColor.GREEN + " Du wurdest zu einem Teammitglied heruntergestuft!");
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Eintragen von dir als Mitglied!");
                              player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Eintragen dieses Spielers als neuer Teamleiter!");
                           player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");                                    
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Entfernen des Spielers aus dem Mitglied-Status!");
                        player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.WHITE + member.getName() + ChatColor.YELLOW + " ist kein Mitglied deines Teams!!");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + member.getName() + ChatColor.YELLOW + " war nie auf diesem Server!");
            }                               
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}