package com.github.CubieX.TeamAdvantage.CmdExecutors;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class FeeCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());

            if(null != teamOfLeader)
            {
               if(teamOfLeader.getTeamBonusEffectsStatus() == 0)
               {
                  int fee = TeamAdvantage.costsPerMemberPerTeamFeeCycle * (1 + teamOfLeader.getMembers().size()); // amount of members including leader * fee per member

                  if(teamOfLeader.setMoney(teamOfLeader.getMoney() - fee))
                  {
                     if(teamOfLeader.setTeamBonusEffectsStatus(1))
                     {
                        player.sendMessage(ChatColor.GREEN + "Du hast die ueberfaellige Teamsteuer bezahlt.\n" +
                              "Team-Boni sind wieder verfuegbar!");
                     }
                     else
                     {
                        teamOfLeader.setMoney(teamOfLeader.getMoney() + fee); // refund team fee

                        player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Setzen des Team-Boni Status!\n" +
                              "Bitte melde das einem Admin!\n" +
                              "Team-Steuer wurde deinem Team zuerueckerstattet.");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.YELLOW + "Nicht genuegend Geld (" + ChatColor.WHITE + fee + TeamAdvantage.currencyPlural + ChatColor.YELLOW +
                           ") um die Steuer zu bezahlen!\n" + "Team-Boni bleiben gesperrt.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Dein Team hat aktuell keine ueberfaellige Team-Steuer zu zahlen.");
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