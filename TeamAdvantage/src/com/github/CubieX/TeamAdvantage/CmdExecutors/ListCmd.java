package com.github.CubieX.TeamAdvantage.CmdExecutors;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class ListCmd implements ISubCmdExecutor
{   
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {
      if(args.length == 1) // display Page 1
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
                  lineList.add("" + "§f" + countAll + ". " + team.getName() + "§6" +
                        " - Leiter: " + "§f" + team.getLeader() + "§6" + " - Geld: " + "§f" +
                        ((int)team.getMoney()) + " " + TeamAdvantage.currencyPlural);                      
               }

               // send list paginated
               plugin.paginateTeamList(sender, lineList, 1, countAll);
            }
            else
            {
               sender.sendMessage("§6" + "Es sind momentan keine Teams angelegt.");
            }
         }
      }
      
      if(args.length == 2) // display Page 2 and following
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

                  for (TATeam team : TeamAdvantage.teams)
                  {
                     countAll++;
                     lineList.add("" + "§f" + countAll + ". " + team.getName() + "§6" +
                           " - Leiter: " + "§f" + team.getLeader() + "§6" + " - Geld: " +
                           "§f" + team.getMoney() + " " + TeamAdvantage.currencyPlural);
                  }

                  // send list paginated
                  plugin.paginateTeamList(sender, lineList, currPage, countAll);
               }
               else
               {
                  sender.sendMessage("§6" + "Das 2. Argument muss eine positive Zahl sein!");
               }
            }
            else
            {
               sender.sendMessage("§6" + "Es sind momentan keine Teams angelegt.");
            }
         }
      }
   }
}