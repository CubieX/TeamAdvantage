package com.github.CubieX.TeamAdvantage.CmdExecutors;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class ListCmd implements ISubCmdExecutor
{   
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {
      if(args.length == 1)
      {
         if(sender.hasPermission("teamadvantage.use"))
         {
            if(!TeamAdvantage.teams.isEmpty())
            {
               int countAll = 0;
               ArrayList <String> lineList = new ArrayList<String>();                  

               for (TATeam team : TeamAdvantage.teams)
               {
                  countAll++;
                  lineList.add("" + ChatColor.WHITE + countAll + ". " + team.getName() + ChatColor.YELLOW +
                        " - Leader: " + ChatColor.WHITE + team.getLeader() + ChatColor.YELLOW + " - Geld: " + ChatColor.WHITE +
                        team.getMoney() + " " + TeamAdvantage.currencyPlural);                      
               }

               // send list paginated
               plugin.paginateTeamAndMemberList(sender, lineList, 1, countAll, "Teams");
            }
            else
            {
               sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
            }
         }
      }
      
      if(args.length == 2)
      {
         if(sender.hasPermission("teamadvantage.use"))
         {
            if(!TeamAdvantage.teams.isEmpty())
            {
               if(plugin.isValidInteger(args[1]))
               {
                  int currPage = Integer.parseInt(args[1]);
                  int countAll = 0;
                  ArrayList <String> lineList = new ArrayList<String>();                  

                  for (TATeam team : plugin.getSQLman().sqlGetTeamList())
                  {
                     countAll++;
                     lineList.add("" + ChatColor.WHITE + countAll + ". " + team.getName() + ChatColor.YELLOW +
                           " - Leader: " + ChatColor.WHITE + team.getLeader() + ChatColor.YELLOW + " - Geld: " +
                           ChatColor.WHITE + team.getMoney() + " " + TeamAdvantage.currencyPlural);
                  }

                  // send list paginated
                  plugin.paginateTeamAndMemberList(sender, lineList, currPage, countAll, "Teams");
               }
               else
               {
                  sender.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine positive Zahl sein!");
               }
            }
            else
            {
               sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
            }
         }
      }
   }
}