package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;
import com.github.CubieX.TeamAdvantage.TATeam.Status;

public class PvpCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {
      // possible arguments:
      // pvp
      // pvp <teamName> alliance
      // pvp <teamName> neutral
      // pvp <teamName> hostile
      // pvp <teamName> accept
      // pvp <teamName> deny

      String cmdHlpMsg = "§eMoegliche Befehle:\n§a" +
            "/team pvp\n" +
            "/team pvp <Teamname> alliance/allianz/freund\n" +
            "/team pvp <Teamname> neutral\n" +
            "/team pvp <Teamname> hostile/feind\n" +
            "/team pvp <Teamname> accept/ja\n" +
            "/team pvp <Teamname> deny/nein";

      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            if(args.length == 1) // list allies and enemies
            {
               TATeam teamOfMember = plugin.getTeamOfPlayer(player.getName());

               if(null != teamOfMember)
               {
                  String alliesList = "";
                  String enemiesList = "";

                  if(!teamOfMember.getAllies().isEmpty())
                  {
                     for(String ally : teamOfMember.getAllies())
                     {
                        alliesList += ally + " ";
                     }
                  }
                  else
                  {
                     alliesList = " - keine -";
                  }

                  if(!teamOfMember.getEnemies().isEmpty())
                  {
                     for(String enemy : teamOfMember.getEnemies())
                     {
                        enemiesList += enemy + " ";
                     }
                  }
                  else
                  {
                     enemiesList = " - keine -";
                  }

                  // show diplomacy state of team
                  player.sendMessage("§f----------------------------------------\n" +
                        "§aAlliierte:\n" + alliesList + "\n\n" +
                        "§cFeinde:\n" + enemiesList + "\n" + 
                        "§f----------------------------------------\n");
               }
               else
               {
                  player.sendMessage("§eDu bist kein Mitglied eines Teams!");
               }
            }
            else if(args.length == 3)
            {
               TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());
               TATeam targetTeam = plugin.getTeamByName(args[1]);

               if(null != teamOfLeader)
               {
                  if(null != targetTeam)
                  {
                     // Send ALLINACE REQUEST =========================================
                     if(args[2].equalsIgnoreCase("alliance") || args[2].equalsIgnoreCase("allianz") || args[2].equalsIgnoreCase("freund"))
                     {
                        if(!teamOfLeader.getName().equals(targetTeam.getName()))
                        {
                           if(!teamOfLeader.getAllies().contains(targetTeam.getName()))
                           {
                              if(targetTeam.addDiplomacyRequest(teamOfLeader.getName(), Status.ALLIED))
                              {
                                 player.sendMessage("§aAllianz-Anfrage an Team §f" + targetTeam.getName() + "§a versendet!");

                                 // inform leader of targeted team
                                 Player targetTeamLeader = Bukkit.getServer().getPlayer(targetTeam.getLeader());

                                 if((null != targetTeamLeader) && (targetTeamLeader.isOnline()))
                                 {
                                    targetTeamLeader.sendMessage("§aDein Team hat eine Allianz-Anfrage von Team §f" + teamOfLeader.getName() + "§a erhalten.");
                                 }
                              }
                              else
                              {
                                 player.sendMessage("§cDatenbank-Fehler beim Erstellen der Diplomatie-Anfrage!\n" +
                                       "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage("§eDein Team ist schon alliiert mit §f" + targetTeam.getName() + " §e!");
                           }
                        }
                        else
                        {
                           player.sendMessage("§eDu kannst keine Anfrage an dein eigenes Team stellen!");
                        }
                     }
                     // Set state to NEUTRAL =========================================
                     else if(args[2].equalsIgnoreCase("neutral") || args[2].equalsIgnoreCase("frieden"))
                     {
                        if(!teamOfLeader.getName().equals(targetTeam.getName()))
                        {
                           if((!teamOfLeader.getAllies().contains(targetTeam.getName())) && (!teamOfLeader.getEnemies().contains(targetTeam.getName())))
                           {
                              if(targetTeam.addDiplomacyRequest(teamOfLeader.getName(), Status.NEUTRAL)) // this call will be relayed to "TATeam.setDiplomacyStatus()" because NEUTRAL status
                              {
                                 player.sendMessage("§fNichtangriffspakt§a mit Team §f" + targetTeam.getName() + "§a geschlossen!");

                                 // inform members of both teams
                                 for(Player p : Bukkit.getServer().getOnlinePlayers())
                                 {
                                    TATeam team = plugin.getTeamOfPlayer(p.getName());

                                    if(null != team)
                                    {
                                       if(team.getMembersAndLeader().contains(p.getName()))
                                       {
                                          if(team.getName().equals(teamOfLeader.getName())) // inform members of own team
                                          {
                                             if(!team.getLeader().equals(teamOfLeader.getLeader()))
                                             {
                                                p.sendMessage("§aDein Team hat einen §fNichtangriffspakt §amit Team §f" + team.getName() + "§a geschlossen.");
                                             }
                                          }
                                          else // inform members of other team
                                          {
                                             p.sendMessage("§aTeam §f" + teamOfLeader.getName() + "§a hat einen §fNichtangriffspakt §amit deinem Team geschlossen.");
                                          }
                                       }
                                    }
                                 }
                              }
                              else
                              {
                                 player.sendMessage("§cDatenbank-Fehler beim Aendern des Diplomatie-Status!\n" +
                                       "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage("§eDein Team hat schon einen Nichtangriffspakt mit §f" + targetTeam.getName() + " §e!");
                           }
                        }
                        else
                        {
                           player.sendMessage("§eDu kannst keine Anfrage an dein eigenes Team stellen!");
                        }
                     }
                     // Send HOSTILE REQUEST to enable PvP =========================================
                     else if(args[2].equalsIgnoreCase("hostile") || args[2].equalsIgnoreCase("feind") || args[2].equalsIgnoreCase("krieg"))
                     {
                        if(!teamOfLeader.getName().equals(targetTeam.getName()))
                        {
                           if(!teamOfLeader.getAllies().contains(targetTeam.getName()))
                           {
                              if(targetTeam.addDiplomacyRequest(teamOfLeader.getName(), Status.HOSTILE))
                              {
                                 player.sendMessage("§cKriegserklaerung §aan Team §f" + targetTeam.getName() + "§a versendet!");

                                 // inform leader of targeted team
                                 Player targetTeamLeader = Bukkit.getServer().getPlayer(targetTeam.getLeader());

                                 if((null != targetTeamLeader) && (targetTeamLeader.isOnline()))
                                 {
                                    targetTeamLeader.sendMessage("§aDein Team hat eine §cKriegserklaerung §avon Team §f" + teamOfLeader.getName() + "§a erhalten.");
                                 }
                              }
                              else
                              {
                                 player.sendMessage("§cDatenbank-Fehler beim Erstellen der Diplomatie-Anfrage!\n" +
                                       "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage("§eDein Team ist bereits verfeindet mit §f" + targetTeam.getName() + " §e!");
                           }
                        }
                        else
                        {
                           player.sendMessage("§eDu kannst keine Anfrage an dein eigenes Team stellen!");
                        }
                     }
                     // ACCEPT diplomacy change request =========================================
                     else if(args[2].equalsIgnoreCase("accept") || args[2].equalsIgnoreCase("ja"))
                     {
                        if(teamOfLeader.getReceivedDiplomacyRequests().containsKey(targetTeam.getName()))
                        {
                           Status acceptedStatus = teamOfLeader.getReceivedDiplomacyRequests().get(targetTeam.getName());

                           if(teamOfLeader.setDiplomacyStatus(targetTeam.getName(), acceptedStatus))
                           {
                              switch (acceptedStatus)
                              {
                              case ALLIED:
                                 player.sendMessage("§aAllianz mit Team §f" + targetTeam.getName() + "§a geschlossen!\n" +
                                       "Eure Teams teilen sich nun einige Bonus-Effekte.");

                                 // inform members of targeted team
                                 for(Player p : Bukkit.getServer().getOnlinePlayers())
                                 {
                                    TATeam team = plugin.getTeamOfPlayer(p.getName());

                                    if(null != team)
                                    {
                                       if(team.getMembersAndLeader().contains(p.getName()))
                                       {
                                          if(team.getName().equals(teamOfLeader.getName())) // inform members of own team
                                          {
                                             if(!team.getLeader().equals(teamOfLeader.getLeader()))
                                             {
                                                p.sendMessage("§aDein Team hat eine Allianz mit Team §f" + team.getName() + "§a geschlossen!\n" +
                                                      "Eure Teams teilen sich nun einige Bonus-Effekte.");
                                             }
                                          }
                                          else // inform members of other team
                                          {
                                             p.sendMessage("§aTeam §f" + teamOfLeader.getName() + "§a hat eine Allianz mit deinem Team geschlossen!\n" +
                                                   "Eure Teams teilen sich nun einige Bonus-Effekte.");
                                          }
                                       }
                                    }
                                 }

                                 break;
                              case HOSTILE:
                                 player.sendMessage("§aEs herrscht jetzt §cKrieg §azwischen deinem Team\n" +
                                       "und Team §f" + targetTeam.getName() + "§a !\n" +
                                       "§cPvP §aist jetzt zwischen euren Teams moeglich.");

                                 // inform members of targeted team
                                 for(Player p : Bukkit.getServer().getOnlinePlayers())
                                 {
                                    TATeam team = plugin.getTeamOfPlayer(p.getName());

                                    if(null != team)
                                    {
                                       if(team.getMembersAndLeader().contains(p.getName()))
                                       {
                                          if(team.getName().equals(teamOfLeader.getName())) // inform members of own team
                                          {
                                             if(!team.getLeader().equals(teamOfLeader.getLeader()))
                                             {
                                                p.sendMessage("§aTeam §f" + teamOfLeader.getName() + "§a hat die §cKriegserklaerung §aakzeptiert!\n" +
                                                      "§cPvP §aist jetzt zwischen euren Teams moeglich.");
                                             }
                                          }
                                          else // inform members of other team
                                          {
                                             p.sendMessage("§aTeam §f" + teamOfLeader.getName() + "§a hat die §cKriegserklaerung §aakzeptiert!\n" +
                                                   "§cPvP §aist jetzt zwischen euren Teams moeglich.");
                                          }
                                       }
                                    }
                                 }

                                 break;
                              default:
                                 // should never be reached
                              }
                           }
                           else
                           {
                              player.sendMessage("§cDatenbank-Fehler beim Aendern des Diplomatie-Status!\n" +
                                    "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage("§eEs liegt keine Diplomatie-Anfrage von Team " + "§f" + targetTeam.getName() + "§e vor!");
                        }
                     }
                     // DENY diplomacy change request =========================================
                     else if(args[2].equalsIgnoreCase("deny") || args[2].equalsIgnoreCase("nein"))
                     {
                        if(teamOfLeader.getReceivedDiplomacyRequests().containsKey(targetTeam.getName()))
                        {
                           Status deniedStatus =  teamOfLeader.getReceivedDiplomacyRequests().get(targetTeam.getName());

                           if(teamOfLeader.deleteDiplomacyRequest(targetTeam.getName()))
                           {
                              // inform leader of targeted team
                              Player targetTeamLeader = Bukkit.getServer().getPlayer(targetTeam.getLeader());

                              switch (deniedStatus)
                              {
                              case ALLIED:
                                 player.sendMessage("§aAllianz mit Team §f" + targetTeam.getName() + "§c abgelehnt!");

                                 if((null != targetTeamLeader) && (targetTeamLeader.isOnline()))
                                 {
                                    targetTeamLeader.sendMessage("§aTeam §f" + teamOfLeader.getName() + "§a hat eine Allianz mit deinem Team abgelehnt!");
                                 }

                                 break;
                              case HOSTILE:
                                 player.sendMessage("§cKriegserklaerung von Team §f" + targetTeam.getName() + "§c abgelehnt!");

                                 if((null != targetTeamLeader) && (targetTeamLeader.isOnline()))
                                 {
                                    targetTeamLeader.sendMessage("§aTeam §f" + teamOfLeader.getName() + "§a hat deine Kriegserklaerung abgelehnt!");
                                 }

                                 break;
                              default:
                                 // should never be reached
                              }
                           }
                           else
                           {
                              player.sendMessage("§cDatenbank-Fehler beim Loeschen der Diplomatie-Anfrage!\n" +
                                    "Bitte melde das einem Admin.");
                           }
                        }
                        else
                        {
                           player.sendMessage("§eEs liegt keine Diplomatie-Anfrage von Team " + "§f" + targetTeam.getName() + "§e vor!");
                        }
                     }
                     else
                     {
                        player.sendMessage(cmdHlpMsg);
                     }
                  }
                  else
                  {
                     player.sendMessage("§eKein Team §f" + args[1] + "§e gefunden!");
                     player.sendMessage("§eVerwende §f/ta list §e um eine Liste der Teams zu erhalten.");
                  }
               }
               else
               {
                  player.sendMessage("§eDu bist kein Teamleiter!");
               }
            }                       
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}