package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
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
         TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());

         if(null != teamOfLeader)
         {
            if(teamOfLeader.setHome(player.getLocation()))
            {
               player.sendMessage(ChatColor.GREEN + "Der Home-Punkt deines Teams wurde gesetzt.");
            }
            else
            {
               player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Setzen des Home-Punkts deines Teams!");                           
               player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
            }
         }
         else
         {
            player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
         }
      }
   }
}