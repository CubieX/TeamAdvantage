package com.github.CubieX.TeamAdvantage.CmdExecutors;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class PayCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());
            OfflinePlayer offTargetPlayer = Bukkit.getServer().getOfflinePlayer(args[1]);
            TATeam teamOfTargetPlayer = plugin.getTeamOfPlayer(args[1]);

            if(null != teamOfLeader)
            {
               if(null != teamOfTargetPlayer)
               {
                  if(plugin.isValidInteger(args[2]))
                  {
                     int amount = Integer.parseInt(args[2]);

                     if((amount > 0) && (amount <= 1e6))
                     {
                        if(teamOfLeader.setMoney(teamOfLeader.getMoney() - amount))
                        {
                           EconomyResponse ecoResToPlayer = TeamAdvantage.econ.depositPlayer(offTargetPlayer.getName(), amount);

                           if(ecoResToPlayer.transactionSuccess())
                           {
                              player.sendMessage(ChatColor.GREEN + "Du hast " + ChatColor.WHITE + amount + " " + TeamAdvantage.currencyPlural + ChatColor.GREEN + " vom Teamkonto\n" +
                                    "auf das Konto von " + ChatColor.WHITE + offTargetPlayer.getName() + ChatColor.GREEN + " ueberwiesen.");
                              
                              if(offTargetPlayer.hasPlayedBefore() && offTargetPlayer.isOnline())
                              {
                                 Player targetPlayer = (Player)offTargetPlayer;
                                 targetPlayer.sendMessage(ChatColor.GREEN + "Du hast " + ChatColor.WHITE + amount + " " + TeamAdvantage.currencyPlural + ChatColor.GREEN + " von deinem Team erhalten.");
                              }
                           }
                           else
                           {
                              player.sendMessage(ChatColor.RED + "Fehler beim Ueberweisen des Betrags auf das Konto von " + ChatColor.WHITE + offTargetPlayer.getName() +
                                    "Bitte melde das einem Admin!");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Dein Team hat nicht genug Geld!");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Der Betrag muss positiv und <= 100.000.000 sein.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Hilfe: 1. Argument: Spielername, 2. Argument: Betrag\n" +
                           "Beispiel: /pay Spielername Betrag");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + ChatColor.YELLOW + " ist kein Teammitglied!");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}