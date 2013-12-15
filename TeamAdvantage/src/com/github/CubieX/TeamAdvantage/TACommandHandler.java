package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TACommandHandler implements CommandExecutor
{
   private TeamAdvantage plugin = null;
   private TAConfigHandler cHandler = null;
   private TASQLManager sqlMan = null;
   private final int contentLinesPerPage = 10;

   public TACommandHandler(TeamAdvantage plugin, TAConfigHandler cHandler, TASQLManager sqlMan) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
      this.sqlMan = sqlMan;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player player = null;
      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      // Befehls-Ideen siehe: http://www.northkingdom.eu/board/index.php?/topic/1083-ideensammlung-teamplugin/
      if (cmd.getName().equalsIgnoreCase("ta"))
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }
         else if (args.length == 1)
         {
            if (args[0].equalsIgnoreCase("version"))
            {            
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " version " + plugin.getDescription().getVersion());

               return true;
            }

            if (args[0].equalsIgnoreCase("list"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(!TeamAdvantage.teams.isEmpty())
                  {
                     int countAll = 0;
                     ArrayList <String> lineList = new ArrayList<String>();                  
                     HashMap<String, String> teamList = sqlMan.sqlGetTeamList();

                     for (String teamName : teamList.keySet())
                     {
                        countAll++;
                        lineList.add(ChatColor.WHITE + "Team: " + ChatColor.YELLOW + teamName + ChatColor.WHITE +
                              " - Leader: " + ChatColor.YELLOW + teamList.get(teamName));                      
                     }

                     // send list paginated
                     paginateList(sender, lineList, 1, countAll);
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
                  }
               }            

               return true;
            }

            if (args[0].equalsIgnoreCase("reload"))
            {
               if(sender.hasPermission("teamadvantage.admin"))
               {
                  cHandler.reloadConfig(sender);                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
               }

               return true;
            }
         }
         else if (args.length == 2)
         {
            // CREATE new team =======================
            if (args[0].equalsIgnoreCase("create"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(player != null)
                  {                    
                     TATeam teamOfLeader = null;
                     boolean teamExists = false;

                     // check if player is leader of a team or if team with this name already exists
                     for(TATeam team : TeamAdvantage.teams)
                     {
                        if(team.getLeader().equals(player.getName()))
                        {
                           teamOfLeader = team;
                        }

                        if(team.getName().equals(args[1]))
                        {
                           teamExists = true;                           
                        }

                        if((null != teamOfLeader) || teamExists)
                        {
                           break;
                        }
                     }

                     if(null == teamOfLeader) // this player is no leader of a team
                     {
                        if(!teamExists) // no team with this name exists
                        {
                           if(sqlMan.sqlAddTeam(args[1], player.getName()))
                           {
                              sender.sendMessage(ChatColor.GREEN + "Dein Team: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde erstellt!");
                           }
                           else
                           {
                              sender.sendMessage(ChatColor.RED + "Fehler beim Erstellen des Teams in der Datenbank!");
                              sender.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           sender.sendMessage(ChatColor.YELLOW + "Das Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " existiert bereits!");
                        }
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.YELLOW + "Du bist bereits Teamleiter von " + ChatColor.WHITE + teamOfLeader.getName());
                     }
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can create teams!");
                  }
               }

               return true;
            }

            // DELETE existing team =======================
            if (args[0].equalsIgnoreCase("delete"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(player != null)
                  {
                     TATeam applicableTeam = null;

                     for(TATeam team : TeamAdvantage.teams)
                     {
                        if(team.getName().equalsIgnoreCase(args[1]))
                        {
                           applicableTeam = team;
                           break;
                        }
                     }

                     if(null != applicableTeam)
                     {
                        if(player.isOp()
                              || player.hasPermission("teamadvantage.admin")
                              || player.getName().equals(applicableTeam.getLeader()))
                        {
                           if(sqlMan.sqlDeleteTeam(applicableTeam))
                           {
                              sender.sendMessage(ChatColor.GREEN + "Team: " + ChatColor.WHITE + applicableTeam.getName() + ChatColor.GREEN + " wurde geloescht!");                             
                           }
                           else
                           {
                              sender.sendMessage(ChatColor.RED + "Fehler beim Loeschen des Teams in der Datenbank!");
                              sender.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung dieses Team zu loeschen!");
                        }                                             
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.YELLOW + "Das Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " wurde nicht gefunden!");
                        sender.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
                     }
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can create teams!");
                  }
               }

               return true;
            }

            // INVITE player into team =======================
            if (args[0].equalsIgnoreCase("invite"))
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
                           if(!teamOfPlayer.getInvitations().contains(member.getName()))
                           {
                              teamOfPlayer.getInvitations().add(member.getName());
                              player.sendMessage(ChatColor.WHITE + member.getName() + ChatColor.GREEN + " hat eine Einladung in dein Team " + ChatColor.WHITE + teamOfPlayer.getName() + ChatColor.GREEN + " erhalten.");
                           }
                           else
                           {
                              player.sendMessage(ChatColor.WHITE + member.getName() + ChatColor.YELLOW + " hat bereits eine Einladung fuer " + teamOfPlayer.getName() + ChatColor.WHITE + " erhalten.");
                           }                          
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " war nie auf diesem Server!");
                     }                               
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can invite players into a team!");
                  }
               }

               return true;
            }

            // REQUEST membership in a team =======================
            if (args[0].equalsIgnoreCase("request"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(player != null)
                  {
                     TATeam team = plugin.getTeamByName(args[1]);

                     if(null != team)
                     {
                        // check if player is not the leader of the requested team
                        if(!team.getLeader().equals(player.getName()))
                        {
                           if(!team.getRequests().contains(player.getName()))
                           {
                              team.getRequests().add(player.getName());
                              player.sendMessage(ChatColor.GREEN + "Du hast eine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " geschickt.");
                           }
                           else
                           {
                              player.sendMessage(ChatColor.YELLOW + "Du hast bereits eine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " geschickt.");
                           }
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                     }
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can request a membership in a team!");
                  }
               }

               return true;
            }

            // ACCEPT membership invitation of a team / ACCEPT membership request of a player =======================
            if (args[0].equalsIgnoreCase("accept"))
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

                     if(null != teamByName) // Player is trying to accept a team invitation using the team name
                     {
                        if(teamByName.getInvitations().contains(player.getName())) // a team invitation for this player from given team is pending
                        {
                           if(!teamByName.getMembers().contains(player.getName())) // requesting player must not be a member of this team already
                           {
                              if(teamByName.addMember(player.getName()))
                              {
                                 player.sendMessage(ChatColor.GREEN + "Du bist jetzt Mitglied im Team " + ChatColor.WHITE + teamByName.getName() + ChatColor.GREEN + "!");
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.YELLOW + "Du bist schon in einem Team!");
                              }
                           }

                           teamByName.getInvitations().remove(player.getName());
                           teamByName.getRequests().remove(player.getName());                           
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Du hast keine Einladung fuer dieses Team!");                           
                        }

                        return true;
                     }

                     teamByLeader = plugin.getTeamByLeader(player.getName());

                     if(null != teamByLeader)  // issuing player is leader of a team
                     {
                        if(teamByLeader.getRequests().contains(args[1])) // a join request of a player for this team is pending and the leader is accepting by using the player name
                        {
                           if(teamByLeader.addMember(args[1]))
                           {
                              player.sendMessage(ChatColor.GREEN + "Spieler " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde aufgenommen!");
                           }
                           else
                           {
                              player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " ist schon im Team!");
                           }

                           teamByLeader.getInvitations().remove(args[1]);
                           teamByLeader.getRequests().remove(args[1]);                           
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Dieser Spieler hat keine Aufnahme-Anfrage an dein Team gestellt!");
                        }

                        return true;
                     }

                     // if this is reached, something went wrong
                     if(null != targetPlayer)
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du bist nicht der Teamleiter!");                        
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

               return true;
            }

            // REMOVE player from team =======================
            if (args[0].equalsIgnoreCase("remove"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(player != null)
                  {
                     TATeam team = plugin.getTeamByLeader(player.getName());

                     if(null != team)
                     {                        
                        if(sqlMan.sqlRemoveMemberFromTeam(team.getName(), args[1]))
                        {
                           player.sendMessage(ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde aus deinem Team entfernt.");
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Kein Spieler " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " in diesem Team gefunden!");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du bist nicht der Teamleiter!");
                     }                     
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can remove players from a team!");
                  }
               }

               return true;
            }
         }
         else
         {
            sender.sendMessage(ChatColor.YELLOW + "Invalid argument count.");
         }
      }         
      return false;
   }

   // ###################################################################################################

   /**
    * Paginates a string list to display it in chat page-by-page
    * 
    * @param Pass the first parameter as the sender.
    * @param The second parameter as the list with all entries.
    * @param The third as the page number to display.
    * @param The fifth as the count of all teams
    */
   public void paginateList(CommandSender sender, ArrayList<String> list, int page, int countAll)
   {

      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         totalPageCount = list.size() / contentLinesPerPage;
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
      sender.sendMessage(ChatColor.GREEN + "Liste der Teams - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");
      sender.sendMessage(ChatColor.WHITE + "----------------------------------------");

      if(list.isEmpty())
      {
         sender.sendMessage(ChatColor.WHITE + "Keine Eintraege.");
      }
      else
      {
         int i = 0, k = 0;
         page--;

         for (String entry : list)
         {
            k++;
            if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1)))
            {
               i++;
               sender.sendMessage(entry);
            }
         }
      }

      sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
      sender.sendMessage(ChatColor.WHITE + "Teams Gesamt: " + ChatColor.YELLOW + countAll);
      sender.sendMessage(ChatColor.WHITE + "----------------------------------------");      
   }
}
