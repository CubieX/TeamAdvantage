package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class InviteCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(player != null)
         {
            OfflinePlayer invitee = Bukkit.getServer().getOfflinePlayer(args[1]);

            if((null != invitee)
                  && (invitee.hasPlayedBefore())) // only known players are allowed
            {
               TATeam teamOfPlayer = plugin.getTeamByLeader(player.getName());

               if(null != teamOfPlayer)
               {
                  if(!teamOfPlayer.getInvitations().contains(invitee.getName()))
                  {
                     if(!invitee.getName().equals(player.getName())) // team leader may not invite himself
                     {
                        if(teamOfPlayer.invitePlayer(invitee.getName()))
                        {
                           player.sendMessage(ChatColor.WHITE + invitee.getName() + ChatColor.GREEN + " hat eine Einladung in dein Team " + ChatColor.WHITE + teamOfPlayer.getName() + ChatColor.GREEN + " erhalten.");
                        }
                        else
                        {
                           player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim Einladen dieses Spielers!");
                           player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du kannst dich nicht selbst ein dein Team einladen!");
                     }
                  }
                  else
                  {
                     player.sendMessage(ChatColor.WHITE + invitee.getName() + ChatColor.YELLOW + " hat bereits eine Einladung erhalten.");
                  }                          
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Spieler " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " war nie auf diesem Server!");
            }                               
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can invite players into a team!");
         }
      }
   }
}