package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class MeCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {

      if(sender.hasPermission("teamadvantage.use"))
      {
         if(null != player)
         {
            if(!TeamAdvantage.teams.isEmpty())
            {
               TATeam teamOfLeader = plugin.getTeamByLeader(player.getName());
               TATeam team = plugin.getTeamOfPlayer(player.getName());

               if(null != teamOfLeader)
               {
                  player.sendMessage(ChatColor.GREEN + "--------------------\nDu bist Teamleiter des Teams: " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + "\n--------------------");
                  sendRequestInvitationsReport(player, teamOfLeader);
               }
               else
               {
                  if(null != team)
                  {
                     player.sendMessage(ChatColor.GREEN + "--------------------\nDu bist Mitglied im Team: " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + "\n--------------------");
                  }
                  else
                  {
                     player.sendMessage(ChatColor.GREEN + "--------------------\nDu bist kein Mitglied eines Teams.\n \n");

                     sendRequestInvitationsReport(player, null);                     
                  }
               }              
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Es sind momentan keine Teams angelegt.");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");         
         }
      }
   }

   private void sendRequestInvitationsReport(Player player, TATeam teamOfLeader)
   {
      String requestToken = "AN Teams";
      String invitationToken = "VON Teams";
      int req = 0;
      int inv = 0;
      String requests = ChatColor.GREEN + "Offene Aufnahme-Anfragen " + requestToken + ":\n" + ChatColor.WHITE;
      String invitations = ChatColor.GREEN + " \nOffene Einladungen " + invitationToken + ":\n" + ChatColor.WHITE;


      if(null != teamOfLeader)
      {
         requestToken = "VON Spielern";
         invitationToken = "AN Spieler";         

         for(String openRequests : teamOfLeader.getRequests())
         {
            requests += openRequests + " ";
            req++;
         }

         for(String openInvitations : teamOfLeader.getInvitations())
         {
            invitations += openInvitations + " ";
            inv++;
         }
      }
      else
      {
         for(TATeam currTeam : TeamAdvantage.teams)
         {
            if(currTeam.getRequests().contains(player.getName()))
            {
               requests += currTeam.getName() + " ";
               req++;
            }

            if(currTeam.getInvitations().contains(player.getName()))
            {
               invitations += currTeam.getName() + " ";
               inv++;
            }
         }
      }

      if(req == 0)
      {                        
         requests += "- keine -";                        
      }

      if(inv == 0)
      {                        
         invitations += "- keine -";
      }

      player.sendMessage(requests + "\n\n" + invitations + ChatColor.GREEN + "\n--------------------");
   }
}