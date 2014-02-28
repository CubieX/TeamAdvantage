package com.github.CubieX.TeamAdvantage.CmdExecutors;

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
                     player.sendMessage("§a" + "Neuer Team-Chat-Tag: " + "§f" + args[1]);
                  }
                  else
                  {
                     player.sendMessage("§4" + "Datenbank-Fehler beim Setzen des Team-Chat-Tags!\n" +
                           "Bitte melde das einem Admin.");
                  }
               }
               else
               {
                  player.sendMessage("§6" + "Der Team-Chat-Tag darf max. " + TeamAdvantage.MAX_CHAT_TAG_LENGTH + " Zeichen haben\n" +
                        "und darf nur folgende Zeichen enthalten:\n" + "§f" + "a-z, A-Z, 0-9, _\n" +
                        "§6" + "(keine Leerzeichen, noch nicht von anderem Team genutzt,\n" +
                        "[ ] werden automatisch hinzugefuegt)");
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