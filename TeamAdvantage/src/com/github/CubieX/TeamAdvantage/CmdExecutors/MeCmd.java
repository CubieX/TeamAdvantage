package com.github.CubieX.TeamAdvantage.CmdExecutors;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TATeam.Status;
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
                  player.sendMessage("§f----------------------------\n" +
                        "§aDu bist Teamleiter des Teams: §f" + team.getName() +
                        "§f\n----------------------------");
                  sendRequestInvitationsReport(player, teamOfLeader);
               }
               else
               {
                  if(null != team)
                  {
                     player.sendMessage("§f----------------------------\n" +
                           "§aDu bist Mitglied im Team: §f" + team.getName() +
                           "§f\n----------------------------");
                  }
                  else
                  {
                     player.sendMessage("§f----------------------------\n§eDu bist kein Mitglied eines Teams.\n \n");

                     sendRequestInvitationsReport(player, null);
                  }
               }
            }
            else
            {
               player.sendMessage("§eEs sind momentan keine Teams angelegt.");
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
      int diplReq = 0;
      String requests = "§aOffene Aufnahme-Anfragen " + requestToken + ":\n§f";
      String invitations = "\n§aOffene Einladungen " + invitationToken + ":\n§f";
      String diplomacyRequests = "\n§aOffene Diplomatie-Anfragen " + invitationToken + ":\n§f";

      if(null != teamOfLeader) // issuing player is a team leader
      {
         requestToken = "VON Spielern";
         invitationToken = "AN Spieler";         

         for(String openRequest : teamOfLeader.getRequests())
         {
            requests += openRequest + " ";
            req++;
         }

         for(String openInvitation : teamOfLeader.getInvitations())
         {
            invitations += openInvitation + " ";
            inv++;
         }

         HashMap<String, Status> diplReqs = teamOfLeader.getReceivedDiplomacyRequests();

         for(String openDiploRequest : diplReqs.keySet())
         {
            if(diplReqs.get(openDiploRequest) == Status.ALLIED)
            {
               diplomacyRequests += "§a" + openDiploRequest + " ";
            }
            else
            {
               diplomacyRequests += "§c" + openDiploRequest + " ";
            }

            diplReq++;
         }
      }
      else // issuing player is normal player
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

      if(diplReq == 0)
      {
         diplomacyRequests += "- keine -";
      }

      if(null != teamOfLeader)
      {
         player.sendMessage(requests +
               "\n \n" + invitations +
               "\n \n" + diplomacyRequests +
               "§f\n----------------------------");  
      }
      else
      {
         player.sendMessage(requests +
               "\n \n" + invitations +
               "§f\n----------------------------");
      }      
   }
}