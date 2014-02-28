package com.github.CubieX.TeamAdvantage.CmdExecutors;

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
                  int fee = TeamAdvantage.costsPerMemberPerTeamFeeCycle * (teamOfLeader.getMembersAndLeader().size()); // amount of members including leader * fee per member

                  if(teamOfLeader.setMoney(teamOfLeader.getMoney() - fee))
                  {
                     if(teamOfLeader.setTeamBonusEffectsStatus(1))
                     {
                        // schedule next due date
                        if(teamOfLeader.scheduleNextTeamFeeDueDate())
                        {
                           player.sendMessage("§a" + "Du hast die ueberfaellige Teamsteuer bezahlt.\n" +
                                 "Team-Boni sind wieder verfuegbar!");
                        }
                        else
                        {
                           player.sendMessage("§4" + "Datenbank-Fehler beim Planen des naechsten Teamsteuer-Faelligkeitsdatums!\n" +
                                 "Bitte melde das einem Admin!");                           
                        }
                     }
                     else
                     {
                        teamOfLeader.setMoney(teamOfLeader.getMoney() + fee); // refund team fee

                        player.sendMessage("§4" + "Datenbank-Fehler beim Setzen des Team-Boni Status!\n" +
                              "Bitte melde das einem Admin!\n" +
                              "Team-Steuer wurde deinem Team zuerueckerstattet.");
                     }
                  }
                  else
                  {
                     player.sendMessage("§6" + "Nicht genuegend Geld (" + "§f" + fee + TeamAdvantage.currencyPlural + "§6" +
                           ") um die Steuer zu bezahlen!\n" + "Team-Boni bleiben gesperrt.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Dein Team hat aktuell keine ueberfaellige Team-Steuer zu zahlen.");
               }
            }
            else
            {
               player.sendMessage("§6" + "Du bist kein Teamleiter!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}