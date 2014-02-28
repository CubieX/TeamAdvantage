package com.github.CubieX.TeamAdvantage.CmdExecutors;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
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
                              player.sendMessage("§a" + "Du hast " + "§f" + amount + " " + TeamAdvantage.currencyPlural + "§a" + " auf das Teamkonto von " + "§f" + teamOfPlayer.getName() + "§a" + " eingezahlt.");

                              if((null != offTeamLeader)
                                    && offTeamLeader.hasPlayedBefore()
                                    && offTeamLeader.isOnline())
                              {
                                 Player targetPlayer = (Player)offTeamLeader;
                                 targetPlayer.sendMessage("§f" + player.getName() + "§a" + " hat " + "§f" + amount + " " + TeamAdvantage.currencyPlural + "§a" + " auf das Teamkonto eingezahlt.");
                              }
                           }
                           else
                           {
                              player.sendMessage("§4" + "Datenbank-Fehler beim Einzahlen des Betrags auf das Team-Konto!\n +" +
                                    "Bitte melde das einem Admin!");
                           }
                        }
                        else
                        {
                           player.sendMessage("§4" + "Fehler beim Abziehen des Betrags von deinem Konto!\n +" +
                                 "Bitte melde das einem Admin!");
                        }
                     }
                     else
                     {
                        player.sendMessage("§6" + "Du hast nicht genug Geld!");
                     }
                  }
                  else
                  {
                     player.sendMessage("§6" + "Der Betrag muss positiv und <= 100.000.000 sein.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Das 2. Argument muss eine Zahl sein!");
               }
            }
            else
            {
               player.sendMessage("§6" + "Du bist kein Mitglied eines Teams!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}