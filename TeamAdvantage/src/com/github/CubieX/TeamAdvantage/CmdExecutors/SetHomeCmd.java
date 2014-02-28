package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class SetHomeCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(null != player)
         {
            TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());

            if(null != teamOfLeader)
            {
               if(teamOfLeader.getMoney() >= TeamAdvantage.costSetTeamHome)
               {
                  if(teamOfLeader.setMoney(teamOfLeader.getMoney() - TeamAdvantage.costSetTeamHome))
                  {
                     if(teamOfLeader.setHome(player.getLocation()))
                     {
                        player.sendMessage("§a" + "Home-Punkt deines Teams wurde gesetzt!");
                     }
                     else
                     {
                        player.sendMessage("§4" + "Datenbank-Fehler beim Setzen des Home-Punkts deines Teams!\n" +                           
                              "Bitte melde das einem Admin.");
                     }
                  }
                  else
                  {
                     player.sendMessage("§6" + "Fehler beim Abziehen des Betrags vom Teamkonto!\n" +
                           "Bitte melde das einem Admin!");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Dein Team hat nicht genug Geld (" + TeamAdvantage.costSetTeamHome + " " + TeamAdvantage.currencyPlural + ") um den Home-Punkt zu setzen!");
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