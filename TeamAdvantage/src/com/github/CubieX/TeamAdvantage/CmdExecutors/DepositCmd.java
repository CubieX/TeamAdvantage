package com.github.CubieX.TeamAdvantage.CmdExecutors;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class DepositCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam teamOfPlayer = plugin.getTeamOfPlayer(player.getName());
            OfflinePlayer offTeamLeader = null;

            if(null != teamOfPlayer)
            {
               offTeamLeader = Bukkit.getServer().getOfflinePlayer(teamOfPlayer.getLeader());

               if(plugin.isValidInteger(args[1]))
               {
                  int amount = Integer.parseInt(args[1]);

                  if((amount > 0) && (amount <= 1e6))
                  {
                     if(TeamAdvantage.econ.getBalance(player.getName()) >= amount)
                     {
                        EconomyResponse ecoResFromPlayer = TeamAdvantage.econ.withdrawPlayer(player.getName(), amount);

                        if(ecoResFromPlayer.transactionSuccess())
                        {
                           if(teamOfPlayer.setMoney(teamOfPlayer.getMoney() + amount))
                           {
                              player.sendMessage(ChatColor.GREEN + "Du hast " + ChatColor.WHITE + amount + " " + TeamAdvantage.currencyPlural + ChatColor.GREEN + " auf das Teamkonto von " + ChatColor.WHITE + teamOfPlayer.getName() + ChatColor.GREEN + " eingezahlt.");

                              if((null != offTeamLeader)
                                    && offTeamLeader.hasPlayedBefore()
                                    && offTeamLeader.isOnline())
                              {
                                 Player targetPlayer = (Player)offTeamLeader;
                                 targetPlayer.sendMessage(ChatColor.WHITE + player.getName() + ChatColor.GREEN + " hat " + ChatColor.WHITE + amount + " " + TeamAdvantage.currencyPlural + ChatColor.GREEN + " auf das Teamkonto eingezahlt.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Einzahlen des Betrags auf das Team-Konto!\n +" +
                                    "Bitte melde das einem Admin!");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.RED + "Fehler beim Abziehen des Betrags von deinem Konto!\n +" +
                                 "Bitte melde das einem Admin!");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du hast nicht genug Geld!");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Der Betrag muss positiv und <= 100.000.000 sein.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine Zahl sein!");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Du bist kein Mitglied eines Teams!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}