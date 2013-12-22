package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class HomeCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         TATeam teamOfPlayer = plugin.getTeamOfPlayer(player.getName());

         if(null != teamOfPlayer)
         {
            if(null != teamOfPlayer.getHome())
            {
               if(checkIfTeleportDestinationIsSave(teamOfPlayer.getHome()))
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

                           if(!resUnmount && !resTele && !resSetPassenger)
                           {
                              player.sendMessage(ChatColor.RED + "Warpen fehlgeschlagen!");
                           }
                        }
                        else
                        {
                           player.sendMessage(ChatColor.YELLOW + "Du kannst nur auf einem gezaehmten und besatteltem Reittier warpen!");
                        }
                     }
                     else
                     {
                        player.sendMessage(ChatColor.YELLOW + "Du kannst nur auf einem gezaehmten und besatteltem Reittier warpen!");
                     }
                  }
                  else
                  {
                     if(!player.teleport(teamOfPlayer.getHome()))
                     {
                        player.sendMessage(ChatColor.RED + "Warpen fehlgeschlagen!");
                     }
                  }
               }
               else
               {
                  player.sendMessage(ChatColor.YELLOW + "Der Home-Punkt ist moeglicherweise nicht sicher!\n" +
                        "Verwende 'home-force-to' um dennoch dorthin zu teleportieren.");
               }
            }
            else
            {
               player.sendMessage(ChatColor.YELLOW + "Es ist kein Home-Punkt fuer dein Team gesetzt!");
            }
         }
         else
         {
            player.sendMessage(ChatColor.YELLOW + "Du bist kein Mitglied eines Teams!");
         }
      }
   }
   
   private boolean checkIfTeleportDestinationIsSave(Location loc)
   {
      boolean res = false;
      Material matBelow = loc.getBlock().getRelative(BlockFace.DOWN).getType();
      Material matLegs = loc.getBlock().getType(); 
      Material matHead = loc.getBlock().getRelative(BlockFace.UP).getType();
      Material matOverHead = loc.getBlock().getRelative(BlockFace.UP, 2).getType(); 

      if((matBelow != Material.LAVA)
            && (matBelow != Material.CACTUS)
            && (matBelow != Material.WATER)
            && (matBelow != Material.TORCH)
            && (matBelow != Material.WALL_SIGN)
            && (matBelow != Material.FIRE))
      {
         if((matLegs == Material.AIR)
               || (matBelow == Material.LONG_GRASS)
               || (matBelow == Material.YELLOW_FLOWER)
               || (matBelow == Material.RED_ROSE)
               || (matBelow == Material.CROPS)
               || (matBelow == Material.DEAD_BUSH)
               || (matBelow == Material.SUGAR_CANE)
               || (matBelow == Material.WHEAT)
               || (matBelow == Material.WALL_SIGN)
               || (matBelow == Material.SIGN_POST)
               || (matBelow == Material.VINE)
               || (matBelow == Material.SOIL)
               || (matBelow == Material.SEEDS)
               || (matBelow == Material.RAILS)
               || (matBelow == Material.TORCH)
               || (matBelow == Material.MAP))
         {
            if((matHead == Material.AIR)
                  || (matBelow == Material.LONG_GRASS)
                  || (matBelow == Material.YELLOW_FLOWER)
                  || (matBelow == Material.RED_ROSE)
                  || (matBelow == Material.CROPS)
                  || (matBelow == Material.DEAD_BUSH)
                  || (matBelow == Material.SUGAR_CANE)
                  || (matBelow == Material.WHEAT)
                  || (matBelow == Material.WALL_SIGN)
                  || (matBelow == Material.SIGN_POST)
                  || (matBelow == Material.VINE)
                  || (matBelow == Material.SOIL)
                  || (matBelow == Material.SEEDS)
                  || (matBelow == Material.RAILS)
                  || (matBelow == Material.TORCH)
                  || (matBelow == Material.MAP))
            {
               if((matOverHead == Material.AIR)
                     || (matBelow == Material.LONG_GRASS)
                     || (matBelow == Material.YELLOW_FLOWER)
                     || (matBelow == Material.RED_ROSE)
                     || (matBelow == Material.CROPS)
                     || (matBelow == Material.DEAD_BUSH)
                     || (matBelow == Material.SUGAR_CANE)
                     || (matBelow == Material.WHEAT)
                     || (matBelow == Material.WALL_SIGN)
                     || (matBelow == Material.SIGN_POST)
                     || (matBelow == Material.VINE)
                     || (matBelow == Material.SOIL)
                     || (matBelow == Material.SEEDS)
                     || (matBelow == Material.RAILS)
                     || (matBelow == Material.TORCH)
                     || (matBelow == Material.MAP))
               {
                  res = true; // probably save
               }
            }
         }
      }

      return res;
   }
}