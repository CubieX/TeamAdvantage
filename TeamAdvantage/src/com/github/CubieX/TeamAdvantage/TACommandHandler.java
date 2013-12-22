package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;
import net.milkbowl.vault.economy.Economy;
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
   private Economy econ = null;
   private ArrayList<String> helpList = new ArrayList<String>();
   private final int contentLinesPerPage = 10;

   public TACommandHandler(TeamAdvantage plugin, TAConfigHandler cHandler, TASQLManager sqlMan, Economy econ) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
      this.sqlMan = sqlMan;
      this.econ = econ;

      loadHelpList();
   }

   private void loadHelpList()
   {
      // add all available commands here      
      helpList.add("" + ChatColor.GREEN + "Farben: "+ ChatColor.WHITE + "Jeder" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Teamleiter" + ChatColor.WHITE + " - " + ChatColor.RED + "Admins");
      helpList.add("" + ChatColor.GREEN + "=============== Befehle ===============");
      helpList.add("" + ChatColor.WHITE + "help - Dieses Hilfemenue");
      helpList.add("" + ChatColor.WHITE + "version - Version des Plugins anzeigen");
      helpList.add("" + ChatColor.WHITE + "list [seite] - Liste aller Teams und Teamleiter");
      helpList.add("" + ChatColor.WHITE + "info [teamName][seite] - Infos ueber das Team");
      helpList.add("" + ChatColor.WHITE + "create <Teamname> - Team erstellen");
      helpList.add("" + ChatColor.WHITE + "request <Teamname> - Aufnahme in ein Team beantragen");
      helpList.add("" + ChatColor.WHITE + "unrequest <Teamname> - Aufnahmeantrag zurueckziehen");
      helpList.add("" + ChatColor.WHITE + "accept <Teamname/"+ ChatColor.YELLOW + "Spielername" + ChatColor.WHITE + "> - Einladung/Antrag annehmen");
      helpList.add("" + ChatColor.WHITE + "deny <Teamname/" + ChatColor.YELLOW + "Spielername" + ChatColor.WHITE + "> - Einladung/Antrag ablehnen");
      helpList.add("" + ChatColor.WHITE + "leave <Teamname> - Team verlassen");
      helpList.add("" + ChatColor.YELLOW + "delete <Teamname> - Team loeschen");
      helpList.add("" + ChatColor.YELLOW + "setname <Teamname> - Team umbenennen");
      helpList.add("" + ChatColor.YELLOW + "setleader <Mitgliedsname> - Mitglied zum neuen Leiter machen");
      helpList.add("" + ChatColor.YELLOW + "invite <Spielername> - Spieler ins Team enladen");
      helpList.add("" + ChatColor.YELLOW + "uninvite <Spielername> - Einladung zurueckziehen");
      helpList.add("" + ChatColor.YELLOW + "remove <Spielername> - Mitglied aus Team entfernen");
      helpList.add("" + ChatColor.YELLOW + "clear - Alle Mitglieder des eigenes Teams entfernen");
      helpList.add("" + ChatColor.RED + "reload - Plugin und DB-Daten neu laden");
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player player = null;
      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      // Befehls-Ideen siehe: http://www.northkingdom.eu/board/index.php?/topic/1125-server-30-team-plugin
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
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " version " +
                     plugin.getDescription().getVersion());

               return true;
            }

            // LIST all existing teams, their leaders and money (Page 1) ==================
            if (args[0].equalsIgnoreCase("list"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(!TeamAdvantage.teams.isEmpty())
                  {
                     int countAll = 0;
                     ArrayList <String> lineList = new ArrayList<String>();                  

                     for (TATeam team : TeamAdvantage.teams)
                     {
                        countAll++;
                        lineList.add("" + ChatColor.WHITE + countAll + ". " + team.getName() + ChatColor.YELLOW +
                              " - Leader: " + ChatColor.WHITE + team.getLeader() + ChatColor.YELLOW + " - Geld: " + ChatColor.WHITE +
                              team.getMoney() + " " + TeamAdvantage.currencyPlural);                      
                     }

                     // send list paginated
                     paginateTeamAndMemberList(sender, lineList, 1, countAll, "Teams");
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
                  }
               }

               return true;
            }

            // CLEAR all members of own team except from the leader himself =======================
            if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("flush") || args[0].equalsIgnoreCase("empty"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(player != null)
                  {
                     TATeam team = plugin.getTeamByLeader(player.getName());

                     if(null != team)
                     {
                        if(team.clearMembers())
                        {
                           player.sendMessage(ChatColor.GREEN + "ALLE Mitglieder wurden aus deinem Team " + ChatColor.WHITE + team.getName() +
                                 ChatColor.GREEN + " entfernt.");
                        }
                        else
                        {
                           player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Leeren des Teams!");                           
                           player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
                     }                     
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can remove players from a team!");
                  }
               }

               return true;
            }

            // LEAVE a team =================================
            if (args[0].equalsIgnoreCase("leave"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  TATeam team = plugin.getTeamOfPlayer(player.getName());

                  if(null != team)
                  {
                     if(team.removeMember(player.getName()))
                     {
                        player.sendMessage(ChatColor.GREEN + "Du bist aus dem Team " + ChatColor.WHITE + team.getName() +ChatColor.GREEN + " ausgetreten.");
                     }
                     else
                     {
                        player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Verlassen des Teams!");                           
                        player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Du bist kein Mitglied eines Teams!");
                  }
               }

               return true;
            }

            // HELP will be displayed (Page 1) =================================
            if (args[0].equalsIgnoreCase("help"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  // send list paginated
                  paginateHelpList(sender, helpList, 1, helpList.size());
               }

               return true;
            }

            // RELOAD the plugin ================================
            if (args[0].equalsIgnoreCase("reload"))
            {
               if(sender.hasPermission("teamadvantage.admin"))
               {
                  cHandler.reloadConfig(sender);
                  sqlMan.loadTeamsFromDB(player);
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
            // LIST all existing teams, their leaders and money (Page 2 and following) ==================
            if (args[0].equalsIgnoreCase("list"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(!TeamAdvantage.teams.isEmpty())
                  {
                     if(plugin.isValidInteger(args[1]))
                     {
                        int currPage = Integer.parseInt(args[1]);
                        int countAll = 0;
                        ArrayList <String> lineList = new ArrayList<String>();                  

                        for (TATeam team : sqlMan.sqlGetTeamList())
                        {
                           countAll++;
                           lineList.add("" + ChatColor.WHITE + countAll + ". " + team.getName() + ChatColor.YELLOW +
                                 " - Leader: " + ChatColor.WHITE + team.getLeader() + ChatColor.YELLOW + " - Geld: " +
                                 ChatColor.WHITE + team.getMoney() + " " + TeamAdvantage.currencyPlural);
                        }

                        // send list paginated
                        paginateTeamAndMemberList(sender, lineList, currPage, countAll, "Teams");
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine positive Zahl sein!");
                     }
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
                  }
               }

               return true;
            }

            // INFO about all members of a team and the leader (Page 1) =================
            if (args[0].equalsIgnoreCase("info"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(!TeamAdvantage.teams.isEmpty())
                  {
                     TATeam team = plugin.getTeamByName(args[1]);

                     if(null != team)
                     {
                        int countAll = 1;
                        ArrayList <String> lineList = new ArrayList<String>();
                        lineList.add("" + ChatColor.YELLOW + countAll + ". " + team.getLeader());

                        for (String member : team.getMembers())
                        {
                           countAll++;
                           lineList.add("" + ChatColor.WHITE + countAll + ". " + member);                      
                        }

                        // send list paginated
                        paginateTeamAndMemberList(sender, lineList, 1, countAll, "Mitglieder");
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                        sender.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
                     }
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
                  }
               }            

               return true;
            }

            // HELP will be displayed (Page 2 and following) =================================
            if (args[0].equalsIgnoreCase("help"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(plugin.isValidInteger(args[1]))
                  {
                     int currPage = Integer.parseInt(args[1]);

                     // send list paginated
                     paginateHelpList(sender, helpList, currPage, helpList.size());
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine positive Zahl sein!");
                  }
               }

               return true;
            }

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
                        if(team.getLeader().equalsIgnoreCase(player.getName()))
                        {
                           teamOfLeader = team;
                        }

                        if(team.getName().equalsIgnoreCase(args[1]))
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
                              player.sendMessage(ChatColor.GREEN + "Dein Team: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde erstellt!");
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Erstellen des Teams!");
                              player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Das Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " existiert bereits!");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du bist bereits Teamleiter von " + ChatColor.WHITE + teamOfLeader.getName());
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
                              player.sendMessage(ChatColor.GREEN + "Team: " + ChatColor.WHITE + applicableTeam.getName() + ChatColor.GREEN + " wurde geloescht!");                             
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Loeschen des Teams!");
                              player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.RED + "Du hast keine Berechtigung dieses Team zu loeschen!");
                        }                                             
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                        player.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
                     }
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can create teams!");
                  }
               }

               return true;
            }

            // SETLEADER to set a new leader for the team - old leader will become a normal member =======================
            if (args[0].equalsIgnoreCase("setleader"))
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
                                       newLeader.sendMessage(ChatColor.GREEN + " Du wurdest zum neuen Teamleiter der " + ChatColor.WHITE + teamOfPlayer.getName()  + " ernannt!");
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
                                    player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim eintragen dieses Spielers als neuer Teamleiter!");
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
                        player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " war nie auf diesem Server!");
                     }                               
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can set a new leader for a team!");
                  }
               }

               return true;
            }

            // SETNAME to set a new team name =======================
            if (args[0].equalsIgnoreCase("setname"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(player != null)
                  {
                     TATeam teamOfPlayer = plugin.getTeamByLeader(player.getName());

                     if(null != teamOfPlayer)
                     {
                        if(checkTeamName(args[1]))
                        {
                           if(teamOfPlayer.setName(args[1]))
                           {
                              player.sendMessage(ChatColor.GREEN + "Dein Team heisst jetzt: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + ".");
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim setzen des Teamnamens!");
                              player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Der Teamname muss zwischen 4 und 20 Zeichen lang sein\n" +
                                 "und darf nur folgende Zeichen enthalten: " + ChatColor.WHITE + "a-z, A-Z, 0-9, _ (keine Leerzeichen)");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
                     }
                  }                                                  
               }
               else
               {
                  sender.sendMessage(TeamAdvantage.logPrefix + "Only players can set a new leader for a team!");
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
                     OfflinePlayer invitee = Bukkit.getServer().getOfflinePlayer(args[1]);

                     if((null != invitee)
                           && (invitee.hasPlayedBefore())) // only known players are allowed
                     {
                        TATeam teamOfPlayer = plugin.getTeamByLeader(player.getName());

                        if(null != teamOfPlayer)
                        {
                           if(!teamOfPlayer.getInvitations().contains(invitee.getName()))
                           {
                              if(!invitee.getName().equals(player.getName())) // team leader may not invite himself
                              {
                                 if(teamOfPlayer.invitePlayer(invitee.getName()))
                                 {
                                    player.sendMessage(ChatColor.WHITE + invitee.getName() + ChatColor.GREEN + " hat eine Einladung in dein Team " + ChatColor.WHITE + teamOfPlayer.getName() + ChatColor.GREEN + " erhalten.");
                                 }
                                 else
                                 {
                                    player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Einladen dieses Spielers!");
                                    player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                                 }
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.YELLOW + "Du kannst dich nicht selbst ein dein Team einladen!");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.WHITE + invitee.getName() + ChatColor.YELLOW + " hat bereits eine Einladung erhalten.");
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

            // UNINVITE player =======================
            if (args[0].equalsIgnoreCase("uninvite"))
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
                              player.sendMessage(ChatColor.GREEN + "Die Einladung fuer " + ChatColor.WHITE + targetedPlayer.getName() + ChatColor.GREEN + " wurde geloescht.");
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
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can uninvite players from a team!");
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
                              if(team.addJoinTeamRequest(player.getName()))
                              {
                                 player.sendMessage(ChatColor.GREEN + "Du hast eine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " geschickt.");
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Beantragen einer Aufnahme in dieses Team!");
                                 player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.YELLOW + "Du hast bereits eine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " geschickt.");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Du bist der Teamleiter.");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                        player.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
                     }
                  }
                  else
                  {
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can request a membership in a team!");
                  }
               }

               return true;
            }

            // DELETE REQUEST for membership of a player for a team =======================
            if (args[0].equalsIgnoreCase("unrequest"))
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
                           if(team.getRequests().contains(player.getName()))
                           {
                              if(team.deleteJoinTeamRequest(player.getName()))
                              {
                                 player.sendMessage(ChatColor.GREEN + "Deine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " wurde geloescht.");
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Beantragen einer Aufnahme in dieses Team!");
                                 player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.YELLOW + "Du hast noch keine Aufnahmeanfrage an Team " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " geschickt.");
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
                           TATeam teamOfPlayer = plugin.getTeamOfPlayer(player.getName());

                           if(null == teamOfPlayer) // requesting player must not be a member of any other team already
                           {
                              if(teamByName.addMember(player.getName()))
                              {
                                 player.sendMessage(ChatColor.GREEN + "Du bist jetzt Mitglied im Team " + ChatColor.WHITE + teamByName.getName() +
                                       ChatColor.GREEN + " !");
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim akzeptieren der Einladung in dieses Team!");
                                 player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.YELLOW + "Du bist bereits Mitglied im Team " + ChatColor.WHITE + teamOfPlayer.getName() +
                                    ChatColor.YELLOW + " !");
                           }

                           teamByName.uninvitePlayer(player.getName());
                           teamByName.deleteJoinTeamRequest(player.getName());                           
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
                        OfflinePlayer targetedPlayer = Bukkit.getServer().getOfflinePlayer(args[1]); // to get correct case of name if player has played before

                        if(teamByLeader.getRequests().contains(targetedPlayer.getName())) // a join request of a player for this team is pending and the leader is accepting by using the player name
                        {
                           TATeam teamOfRequestingPlayer = plugin.getTeamOfPlayer(targetedPlayer.getName());

                           if(null == teamOfRequestingPlayer)
                           {
                              if(teamByLeader.addMember(targetedPlayer.getName()))
                              {
                                 player.sendMessage(ChatColor.GREEN + "Spieler " + ChatColor.WHITE + targetedPlayer.getName() + ChatColor.GREEN + " wurde aufgenommen!");

                                 if((null != targetPlayer && (targetPlayer.isOnline())))
                                 {
                                    targetPlayer.sendMessage(ChatColor.GREEN + "Du wurdest in das Team " + ChatColor.WHITE + teamByLeader.getName() + ChatColor.GREEN + " aufgenommen!");
                                 }
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Aufnehmen eines Spielers in das Team!");
                                 player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + targetedPlayer.getName() + ChatColor.YELLOW + " ist schon im Team " +
                                    ChatColor.WHITE + teamOfRequestingPlayer.getName() + ChatColor.YELLOW + " !");
                           }

                           teamByLeader.uninvitePlayer(args[1]);
                           teamByLeader.deleteJoinTeamRequest(args[1]);
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

               return true;
            }

            // DENY membership invitation of a team / DENY membership request of a player =======================
            if (args[0].equalsIgnoreCase("deny"))
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

                        return true;
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

                        return true;
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
                     sender.sendMessage(TeamAdvantage.logPrefix + "Only players can remove players from a team!");
                  }
               }

               return true;
            }
         }
         else if (args.length == 3)
         {
            // INFO about all members of a team and the leader (Page 2 and following) =================
            if (args[0].equalsIgnoreCase("info"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(!TeamAdvantage.teams.isEmpty())
                  {
                     TATeam team = plugin.getTeamByName(args[1]);

                     if(null != team)
                     {
                        if(plugin.isValidInteger(args[2]))
                        {
                           int currPage = Integer.parseInt(args[2]);
                           int countAll = 1;
                           ArrayList <String> lineList = new ArrayList<String>();
                           lineList.add("" + ChatColor.YELLOW + countAll + ". " + team.getLeader());

                           for (String member : team.getMembers())
                           {
                              countAll++;
                              lineList.add("" + ChatColor.WHITE + countAll + ". " + member);                      
                           }

                           // send list paginated
                           paginateTeamAndMemberList(sender, lineList, currPage, countAll, "Mitglieder");
                        }
                        else
                        {
                           sender.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine positive Zahl sein!");
                        }
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                        sender.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
                     }
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
                  }
               }            

               return true;
            }
         }
         else
         {
            sender.sendMessage(ChatColor.YELLOW + "Invalid argument count.");         
         }

         return false;
      }

      return false;
   }


   // ###################################################################################################

   /**
    * Paginates a string list to display it in chat page-by-page
    * 
    * @param sender The sender to send the list to
    * @param list The list to paginate
    * @param page The page number to display.
    * @param countAll The count of all available entries
    * @param topic The topic of the list to display (e.g. Teams, Members, ...)
    */
   public void paginateTeamAndMemberList(CommandSender sender, ArrayList<String> list, int page, int countAll, String topic)
   {
      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         if(list.size() > 0)
         {
            totalPageCount = list.size() / contentLinesPerPage;
         }      
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      if(page <= totalPageCount)
      {
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
         sender.sendMessage(ChatColor.GREEN + "Liste der " + topic + " - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");      
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
         sender.sendMessage(ChatColor.WHITE + topic + " Gesamt: " + ChatColor.YELLOW + countAll);
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
      }
      else
      {
         String pageTerm = "Seiten";

         if(totalPageCount == 1)
         {
            pageTerm = "Seite";
         }         

         sender.sendMessage(ChatColor.YELLOW + "Die Liste hat nur " + ChatColor.WHITE + totalPageCount + ChatColor.YELLOW + " " + pageTerm + "!");
      }
   }

   /**
    * Sends the HELP as a paginated list of strings in chat to a player
    * 
    * @param sender The sender to send the list to
    * @param list The list to paginate
    * @param page The page number to display.
    * @param countAll The count of all available entries   
    */
   public void paginateHelpList(CommandSender sender, ArrayList<String> list, int page, int countAll)
   {
      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         if(list.size() > 0)
         {
            totalPageCount = list.size() / contentLinesPerPage;
         }      
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      if(page <= totalPageCount)
      {
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
         sender.sendMessage(ChatColor.GREEN + "TeamAdvantage Hilfe - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");      
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
      }
      else
      {
         sender.sendMessage(ChatColor.YELLOW + "Die Hilfe hat nur " + ChatColor.WHITE + totalPageCount + ChatColor.YELLOW + " Seiten!");
      }
   }

   private boolean checkTeamName(String teamName)
   {
      boolean res = false;



      return res;
   }
}
