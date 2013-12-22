package com.github.CubieX.TeamAdvantage.CmdExecutors;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class InfoCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
     if(args.length == 2)
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
                 lineList.add("" + ChatColor.YELLOW + countAll + ". " + team.getLeader());

                 for (String member : team.getMembers())
                 {
                    countAll++;
                    lineList.add("" + ChatColor.WHITE + countAll + ". " + member);                      
                 }

                 // send list paginated
                 plugin.paginateTeamAndMemberList(sender, lineList, 1, countAll, "Mitglieder");
              }
              else
              {
                 sender.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                 sender.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
              }
           }
           else
           {
              sender.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
           }
        }
     }
     
     if(args.length == 3)
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
                    lineList.add("" + ChatColor.YELLOW + countAll + ". " + team.getLeader());

                    for (String member : team.getMembers())
                    {
                       countAll++;
                       lineList.add("" + ChatColor.WHITE + countAll + ". " + member);                      
                    }

                    // send list paginated
                    plugin.paginateTeamAndMemberList(sender, lineList, currPage, countAll, "Mitglieder");
                 }
                 else
                 {
                    sender.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine positive Zahl sein!");
                 }
              }
              else
              {
                 sender.sendMessage(ChatColor.YELLOW + "Kein Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " gefunden!");
                 sender.sendMessage(ChatColor.YELLOW + "Verwende " + ChatColor.WHITE + "/ta list"  + ChatColor.YELLOW + " um eine Liste der Teams zu erhalten.");
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