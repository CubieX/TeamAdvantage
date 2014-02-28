package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class DeleteHomeCmd implements ISubCmdExecutor
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
               if(teamOfLeader.deleteHome())
               {
                  player.sendMessage("§a" + "Der Home-Punkt deines Teams wurde geloescht.");
               }
               else
               {
                  player.sendMessage("§4" + "Datenbank-Fehler beim Loeschen des Home-Punkts deines Teams!\n" +                           
                        "Bitte melde das einem Admin.");
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