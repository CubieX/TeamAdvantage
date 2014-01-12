package com.github.CubieX.TeamAdvantage.CmdExecutors;

import java.util.ArrayList;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class DeleteCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
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
                  // get temporary values for economy actions after deletion
                  ArrayList<String> teamMembersAndLeader = applicableTeam.getMembers();
                  teamMembersAndLeader.add(applicableTeam.getLeader());
                  int teamMoneyPerPlayer = (int)(applicableTeam.getMoney() / teamMembersAndLeader.size());

                  if(plugin.getGlobSQLman().sqlDeleteTeam(applicableTeam))
                  {
                     player.sendMessage(ChatColor.GREEN + "Dein Team: " + ChatColor.WHITE + applicableTeam.getName() + ChatColor.GREEN + " wurde geloescht!");

                     if(teamMoneyPerPlayer >= 1)
                     {
                        for(String playerOfTeam : teamMembersAndLeader)
                        {
                           Player p = Bukkit.getServer().getPlayer(playerOfTeam);
                           EconomyResponse ecoRes = TeamAdvantage.econ.depositPlayer(playerOfTeam, teamMoneyPerPlayer);

                           if(ecoRes.transactionSuccess())
                           {
                              if((null != p) && (p.isOnline()) && (!p.getName().equals(applicableTeam.getLeader())))
                              {
                                 p.sendMessage(ChatColor.GREEN + "Dein Team: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde aufgeloest!\n" +
                                       "Dir wurden " + ChatColor.WHITE + teamMoneyPerPlayer + " " + TeamAdvantage.currencyPlural + ChatColor.GREEN + " vom Teamkonto ueberwiesen.");
                              }
                           }
                           else
                           {
                              if((null != p) && (p.isOnline()))
                              {
                                 p.sendMessage(ChatColor.GREEN + "Dein Team: " + ChatColor.WHITE + args[1] + ChatColor.GREEN + " wurde aufgeloest!\n" +
                                       ChatColor.RED + "FEHLER beim Ueberweisen des anteiligen Team-Vermoegens!\nBitte melde das einem Admin.");
                              }
                           }
                        }
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Loeschen des Teams!\n" +
                           "Bitte melde das einem Admin.");
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
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}