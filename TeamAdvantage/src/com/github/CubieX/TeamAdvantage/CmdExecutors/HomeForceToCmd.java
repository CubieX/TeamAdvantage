package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class HomeForceToCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         if(null != player)
         {
            TATeam teamOfPlayer = plugin.getTeamOfPlayer(player.getName());

            if(null != teamOfPlayer)
            {
               if(null != teamOfPlayer.getHome())
               {
                  // handle teleport with mount
                  if(player.isInsideVehicle())
                  {
                     if(player.getVehicle() instanceof Horse)
                     {
                        Horse mount = (Horse)player.getVehicle();

                        if(mount.isTamed() &&
                              (null != mount.getInventory().getSaddle())) // player may only warp with a tamed mount with a saddle
                        {
                           // unmount player
                           boolean resUnmount = player.leaveVehicle();
                           // teleport horse and re-mount player (teleporting him in the proccess)           
                           boolean resTele = mount.teleport(teamOfPlayer.getHome());
                           boolean resSetPassenger = mount.setPassenger(player); // will teleport the player to the horses back

                           if(resUnmount && resTele && resSetPassenger)
                           {
                              player.sendMessage("§a" + "Willkommen beim Team-Home von " + "§f" + teamOfPlayer.getName() + "§a" + "!");
                           }
                           else
                           {
                              player.sendMessage("§4" + "Teleport fehlgeschlagen!");
                           }
                        }
                        else
                        {
                           player.sendMessage("§6" + "Du kannst nur auf einem gezaehmten und besatteltem Reittier warpen!");
                        }
                     }
                     else
                     {
                        player.sendMessage("§6" + "Du kannst nur auf einem gezaehmten und besatteltem Reittier warpen!");
                     }
                  }
                  else
                  {
                     if(player.teleport(teamOfPlayer.getHome()))
                     {
                        player.sendMessage("§a" + "Willkommen beim Team-Home von " + "§f" + teamOfPlayer.getName() + "§a" + "!");
                     }
                     else
                     {
                        player.sendMessage("§4" + "Warpen fehlgeschlagen!");
                     }
                  }                           
               }
               else
               {
                  player.sendMessage("§6" + "Es ist kein Home-Punkt fuer dein Team gesetzt!");
               }
            }
            else
            {
               player.sendMessage("§6" + "Du bist kein Mitglied eines Teams!");
            }
         }
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }
}