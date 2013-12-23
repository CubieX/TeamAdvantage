package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class TagCmd implements ISubCmdExecutor
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
               if(plugin.checkTeamTag(args[1]))
               {
                  if(teamOfLeader.setTag(args[1]))
                  {
                     player.sendMessage(ChatColor.GREEN + "Neuer Team-Chat-Tag: " + ChatColor.WHITE + args[1]);
                  }
                  else
                  {
                     player.sendMessage(ChatColor.RED + "Datenbank-Fehler beim setzen des Team-Chat-Tags!");
                     player.sendMessage(ChatColor.RED + "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Der Team-Chat-Tag darf max. " + TeamAdvantage.MAX_CHAT_TAG_LENGTH + " Zeichen haben\n" +
                        "und darf nur folgende Zeichen enthalten:\n" + ChatColor.WHITE + "a-z, A-Z, 0-9, _\n" +
                        ChatColor.YELLOW + "(keine Leerzeichen, noch nicht von anderem Team genutzt,\n" +
                        "[ ] werden automatisch hinzugefuegt)");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Du bist kein Teamleiter!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }      
   }
}