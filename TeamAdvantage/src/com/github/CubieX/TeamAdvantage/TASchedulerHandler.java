package com.github.CubieX.TeamAdvantage;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.github.CubieX.TeamAdvantage.TATeam.Status;

public class TASchedulerHandler
{
   private TeamAdvantage plugin = null;

   public TASchedulerHandler(TeamAdvantage plugin)
   {
      this.plugin = plugin;
   }

   /**
    * <b>Notifies players of pending invitations and team leaders of pending join requests</b><br>
    * 
    * */
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
                     String requestNotice = "§a" + TeamAdvantage.logPrefix + "§f" + "Folgende Spieler haben um Aufnahme in dein Team gebeten:\n";
                     // check for pending requests
                     for(String requestee : teamOfLeader.getRequests())
                     {
                        requestNotice += requestee + " "; 
                     }

                     player.sendMessage(requestNotice);
                  }

                  if(!teamOfLeader.getReceivedDiplomacyRequests().isEmpty())
                  {
                     String requestNotice = "§a" + TeamAdvantage.logPrefix + "§fFolgende Teams haben einen Wechsel\n" +
                           "des Diplomatie-Status angefragt:\n" +
                           "§aGruen = Friedens-Anfrage (Allianz) §f\n§cRot = Kriegsanfrage (PvP)\n";
                     
                     // check for pending diplomacy requests
                     HashMap<String, Status> diplReqs = teamOfLeader.getReceivedDiplomacyRequests();

                     for(String requestingTeam : diplReqs.keySet())
                     {
                        if(diplReqs.get(requestingTeam) == Status.ALLIED)
                        {
                           requestNotice += "§a" + requestingTeam + " ";  
                        }
                        else
                        {
                           requestNotice += "§c" + requestingTeam + " ";
                        }
                     }

                     player.sendMessage(requestNotice);
                  }
               }
               else
               {
                  String invitationNotice = "§a" + TeamAdvantage.logPrefix + "§f" + "Du hast Einladungen von folgenden Teams:\n";
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

   /**
    * <b>Sends a chat message via main server thread</b><br>
    * Used for send messages to players from async tasks
    * */
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

   /**
    * <b>Manages team chat, so only team members can see the message</b><br>
    * 
    * */
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
                  if(teamOfSender.getMembersAndLeader().contains(p.getName()))
                  {
                     p.sendMessage(message);
                     receiverCount++;
                  }
               }

               if(receiverCount <= 1) // if only the sender is on the list
               {
                  sender.sendMessage("§6" + "Es sind momentan keine Teammitglieder online!");
               }
            }
         }
      });
   }

   /**
    * <b>Handler for cyclic team fee payment</b><br>
    * 
    * */
   public void startTeamFeeManagerScheduler_SyncRep()
   {
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            // Check teamNextFeeDueDate in DB for each team and charge fee if due

            if(TeamAdvantage.teamFeeCycle == 0)
            {
               return;
            }

            for(TATeam team : TeamAdvantage.teams)
            {
               if(System.currentTimeMillis() > team.getNextTeamFeeDueDateTimestamp())
               {
                  // fee is due for this team
                  int fee = TeamAdvantage.costsPerMemberPerTeamFeeCycle * (1 + team.getMembers().size()); // amount of members including leader * fee per member 
                  OfflinePlayer offLeader = Bukkit.getServer().getOfflinePlayer(team.getLeader());
                  Player leader = null;

                  if(offLeader.isOnline())
                  {
                     leader = (Player)offLeader;
                  }

                  // charge team with fee
                  if(team.setMoney(team.getMoney() - fee))
                  {
                     if(team.getTeamBonusEffectsStatus() == 0)
                     {
                        // re-enable team bonus effects because overdue fee was now payed
                        if(team.setTeamBonusEffectsStatus(1))
                        {
                           if(null != leader)
                           {
                              leader.sendMessage("§a" + "Die ueberfaellige Teamsteuer wurde vom Teamkonto abgebucht.\n" +
                                    "Team-Boni sind wieder verfuegbar!");
                           }
                        }
                        else
                        {
                           team.setMoney(team.getMoney() + fee); // refund team fee

                           if(null != leader)
                           {
                              leader.sendMessage("§4" + "Datenbank-Fehler beim Setzen des Team-Boni Status!\n" +
                                    "Bitte melde das einem Admin!\n" +
                                    "Team-Steuer wurde deinem Team zuerueckerstattet.");
                           }
                        }
                     }

                     if(null != leader)
                     {
                        leader.sendMessage("§a" + "Die Teamsteuer von " + "§f" + fee + " " + TeamAdvantage.currencyPlural +
                              "§a" + " wurde soeben vom Teamkonto abgebucht.");
                     }
                  }
                  else
                  {
                     // deactivate all active bonus effects immediately, because fee has not been paid
                     // TODO deactivate effects

                     // suspend further activations of bonus effects
                     if(team.setTeamBonusEffectsStatus(0))
                     {
                        if(null != leader)
                        {                        
                           leader.sendMessage("§6" + "ACHTUNG: Die Teamsteuer von " + "§f" + fee + " " +
                                 TeamAdvantage.currencyPlural + "§6" + "\n" +
                                 " konnte nicht vom Teamkonto abgebucht werden.\n" +
                                 "Alle Team-Boni werden gesperrt, bis zur naechsten erfolgreichen Abbuchung!\n" +
                                 "Du kannst den Beitrag jederzeit mit " + "§f" + "/team fee" + "§6" + " oder " + "§f" +
                                 "/team steuer" +
                                 "§6" + " nachzahlen.");
                        }
                     }
                     else
                     {
                        TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "ERROR while setting team bonus effects status in DB!");
                     }

                  }

                  // schedule next due date
                  if(!team.scheduleNextTeamFeeDueDate())
                  {
                     TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "ERROR while scheduling next team fee due date in DB!");
                  }
               }
            }            
         }
      }, (20*5L), (20*3600)); // 5 sec delay, 1 hour period
   }
}
