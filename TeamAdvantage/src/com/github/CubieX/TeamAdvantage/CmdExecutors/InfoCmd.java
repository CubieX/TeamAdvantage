package com.github.CubieX.TeamAdvantage.CmdExecutors;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class InfoCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
     if(args.length == 2) // Display Page 1
     {
        if(sender.hasPermission("teamadvantage.use"))
        {
           if(!TeamAdvantage.teams.isEmpty())
           {
              TATeam team = plugin.getTeamByName(args[1]);

              if(null != team)
              {
                 int countAll = 1;
                 ArrayList <String> lineList = new ArrayList<String>();
                 lineList.add("" + "§6" + countAll + ". " + team.getLeader());

                 for (String member : team.getMembers())
                 {
                    countAll++;
                    lineList.add("" + "§f" + countAll + ". " + member);                      
                 }

                 // send list paginated
                 plugin.paginateTeamInfoList(sender, lineList, 1, countAll, team);
              }
              else
              {
                 sender.sendMessage("§6" + "Kein Team " + "§f" + args[1] + "§6" + " gefunden!");
                 sender.sendMessage("§6" + "Verwende " + "§f" + "/ta list"  + "§6" + " um eine Liste der Teams zu erhalten.");
              }
           }
           else
           {
              sender.sendMessage("§6" + "Es sind momentan keine Teams angelegt.");
           }
        }
     }
     
     if(args.length == 3) // Display Page 2 and following
     {
        if(sender.hasPermission("teamadvantage.use"))
        {
           if(!TeamAdvantage.teams.isEmpty())
           {
              TATeam team = plugin.getTeamByName(args[1]);

              if(null != team)
              {
                 if(plugin.isValidInteger(args[2]))
                 {
                    int currPage = Integer.parseInt(args[2]);
                    int countAll = 1;
                    ArrayList <String> lineList = new ArrayList<String>();
                    lineList.add("" + "§6" + countAll + ". " + team.getLeader());

                    for (String member : team.getMembers())
                    {
                       countAll++;
                       lineList.add("" + "§f" + countAll + ". " + member);                      
                    }

                    // send list paginated
                    plugin.paginateTeamInfoList(sender, lineList, currPage, countAll, team);
                 }
                 else
                 {
                    sender.sendMessage("§6" + "Das 2. Argument muss eine positive Zahl sein!");
                 }
              }
              else
              {
                 sender.sendMessage("§6" + "Kein Team " + "§f" + args[1] + "§6" + " gefunden!");
                 sender.sendMessage("§6" + "Verwende " + "§f" + "/ta list"  + "§6" + " um eine Liste der Teams zu erhalten.");
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