package com.github.CubieX.TeamAdvantage.CmdExecutors;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class CreateCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
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
                  if(TeamAdvantage.econ.getBalance(player.getName()) >= TeamAdvantage.costsCreateTeam)
                  {                     
                     if(plugin.checkTeamName(args[1]))
                     {
                        if(plugin.checkTeamTag(args[2]))
                        {
                           EconomyResponse ecoRes = TeamAdvantage.econ.withdrawPlayer(player.getName(), TeamAdvantage.costsCreateTeam);

                           if(ecoRes.transactionSuccess())
                           {
                              if(plugin.getGlobSQLman().sqlAddTeam(args[1], player.getName(), args[2]))
                              {
                                 player.sendMessage(ChatColor.GREEN + "Dein Team: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde erstellt!\n"+
                                       "Dir wurden " + ChatColor.WHITE + TeamAdvantage.costsCreateTeam + " " + TeamAdvantage.currencyPlural + ChatColor.GREEN + " abgezogen.");
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Erstellen des Teams!\n" +
                                       "Bitte melde das einem Admin.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Fehler beim Abziehen des Betrags von deinem Konto!\n" +
                                    "Bitte melde das einem Admin!");
                           }
                        }
                        else                     
                        {
                           player.sendMessage(ChatColor.YELLOW + "Der Team-Chat-Tag darf max. " + TeamAdvantage.MAX_CHAT_TAG_LENGTH + " Zeichen haben\n" +
                                 "und darf nur folgende Zeichen enthalten:\n" + ChatColor.WHITE + "a-z, A-Z, 0-9, _\n" +
                                 ChatColor.YELLOW + "(keine Leerzeichen, noch nicht von anderem Team genutzt,\n" +
                                 "[ ] werden automatisch hinzugefuegt)");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Der Teamname muss zwischen 4 und 20 Zeichen lang sein\n" +
                              "und darf nur folgende Zeichen enthalten:\n" + ChatColor.WHITE + "a-z, A-Z, 0-9, _\n" +
                              "(keine Leerzeichen und kein Spielername)");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Das Erstellen eines Teams kostet " + ChatColor.WHITE + TeamAdvantage.costsCreateTeam + " " +
                           TeamAdvantage.currencyPlural + ChatColor.YELLOW + "\nDu hast nicht genuegend Geld!");
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
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}