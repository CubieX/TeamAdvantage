package com.github.CubieX.TeamAdvantage;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TASchedulerHandler
{
   private TeamAdvantage plugin = null;

   public TASchedulerHandler(TeamAdvantage plugin)
   {
      this.plugin = plugin;
   }

   public void startProjectileSpecialAttributeCleanerScheduler_SynchDelayed(final Integer entityID)
   {
      plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
      {
         public void run()
         {
            plugin.getExplodingList().remove(entityID);
         }
      }, 1L); // 1 tick delay
   }

   public void startNotifierScheduler_SynchRepeating()
   {
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         public void run()
         {
            for(Player player : Bukkit.getServer().getOnlinePlayers())
            {
               TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());

               if(null != teamOfLeader) // player is a team leader
               {
                  if(!teamOfLeader.getRequests().isEmpty())
                  {
                     String requestNotice = ChatColor.GREEN + TeamAdvantage.logPrefix + ChatColor.WHITE + "Folgende Spieler haben um Aufnahme in dein Team gebeten:\n";
                     // check for pending requests
                     for(String requestee : teamOfLeader.getRequests())
                     {
                        requestNotice += requestee + " "; 
                     }

                     player.sendMessage(requestNotice);
                  }
               }
               else
               {
                  String invitationNotice = ChatColor.GREEN + TeamAdvantage.logPrefix + ChatColor.WHITE + "Du hast Einladungen von folgenden Teams:\n";
                  boolean invitationsPending = false;

                  for(TATeam team : TeamAdvantage.teams)
                  {                     
                     if(team.getInvitations().contains(player.getName()))
                     {
                        invitationNotice += team.getName() + " ";
                        invitationsPending = true;
                     }
                  }

                  if(invitationsPending)
                  {
                     player.sendMessage(invitationNotice);
                  }
               }
            }
         }
      }, 10*20L, TeamAdvantage.notificationDelay*60*20L); // 10 seconds initial delay, 10 minutes cycle
   }

   public void sendSyncChatMessageToPlayer(final Player player, final String message)
   {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         public void run()
         {
            if(null != player)
            {
               player.sendMessage(message);
            }
         }
      });
   }

   public void handleTeamChat(final Player sender, final TATeam teamOfSender, final String message)
   {
      plugin.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         public void run()
         {
            if((null != sender) && (null != teamOfSender))
            {
               int receiverCount = 0;
               
               for(Player p : Bukkit.getServer().getOnlinePlayers())
               {
                  if((teamOfSender.getMembers().contains(p.getName()))
                        || (teamOfSender.getLeader().equals(p.getName())))
                  {
                     p.sendMessage(message);
                     receiverCount++;
                  }
               }
               
               if(receiverCount <= 1) // if only the sender is on the list
               {
                  sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teammitglieder online!");
               }
            }
         }
      });
   }
}
