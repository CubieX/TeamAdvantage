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
         if(null != player)
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

                              if(resUnmount && resTele && resSetPassenger)
                              {
                                 player.sendMessage(ChatColor.GREEN + "Willkommen beim Team-Home von " + ChatColor.WHITE + teamOfPlayer.getName() + ChatColor.GREEN + "!");
                              }
                              else
                              {
                                 player.sendMessage(ChatColor.RED + "Teleport fehlgeschlagen!");
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
                        if(player.teleport(teamOfPlayer.getHome()))
                        {
                           player.sendMessage(ChatColor.GREEN + "Willkommen beim Team-Home von " + ChatColor.WHITE + teamOfPlayer.getName() + ChatColor.GREEN + "!");
                        }
                        else
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
         else
         {
            sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
         }
      }
   }

   private boolean checkIfTeleportDestinationIsSave(Location loc)
   {
      boolean res = false;
      Material matBelow = loc.getBlock().getRelative(BlockFace.DOWN).getType();
      if(TeamAdvantage.debug){TeamAdvantage.log.info(loc.getBlock().getRelative(BlockFace.DOWN).getX() + " " + loc.getBlock().getRelative(BlockFace.DOWN).getY() + " " + loc.getBlock().getRelative(BlockFace.DOWN).getZ() + " " + loc.getBlock().getRelative(BlockFace.DOWN).getType().name());}
      Material matLegs = loc.getBlock().getType(); 
      Material matHead = loc.getBlock().getRelative(BlockFace.UP).getType();
      Material matOverHead = loc.getBlock().getRelative(BlockFace.UP, 2).getType(); 

      if((matBelow != Material.AIR)
            && (matBelow != Material.LAVA)
            && (matBelow != Material.STATIONARY_LAVA)
            && (matBelow != Material.WATER)
            && (matBelow != Material.STATIONARY_WATER)
            && (matBelow != Material.CACTUS)
            && (matBelow != Material.WATER)
            && (matBelow != Material.TORCH)
            && (matBelow != Material.WALL_SIGN)
            && (matBelow != Material.FIRE)
            && (matBelow != Material.ITEM_FRAME)
            && (matBelow != Material.PAINTING))
      {
         if((matLegs == Material.AIR)
               || (matLegs == Material.LONG_GRASS)
               || (matLegs == Material.YELLOW_FLOWER)
               || (matLegs == Material.RED_ROSE)
               || (matLegs == Material.CROPS)
               || (matLegs == Material.DEAD_BUSH)
               || (matLegs == Material.SUGAR_CANE)
               || (matLegs == Material.WHEAT)
               || (matLegs == Material.WALL_SIGN)
               || (matLegs == Material.SIGN_POST)
               || (matLegs == Material.VINE)
               || (matLegs == Material.SOIL)
               || (matLegs == Material.SEEDS)
               || (matLegs == Material.RAILS)
               || (matLegs == Material.TORCH)
               || (matLegs == Material.MAP)
               || (matLegs != Material.ITEM_FRAME)
               || (matLegs != Material.PAINTING))
         {
            if((matHead == Material.AIR)
                  || (matHead == Material.LONG_GRASS)
                  || (matHead == Material.YELLOW_FLOWER)
                  || (matHead == Material.RED_ROSE)
                  || (matHead == Material.CROPS)
                  || (matHead == Material.DEAD_BUSH)
                  || (matHead == Material.SUGAR_CANE)
                  || (matHead == Material.WHEAT)
                  || (matHead == Material.WALL_SIGN)
                  || (matHead == Material.SIGN_POST)
                  || (matHead == Material.VINE)
                  || (matHead == Material.SOIL)
                  || (matHead == Material.SEEDS)
                  || (matHead == Material.RAILS)
                  || (matHead == Material.TORCH)
                  || (matHead == Material.MAP)
                  || (matHead != Material.ITEM_FRAME)
                  || (matHead != Material.PAINTING))
            {
               if((matOverHead == Material.AIR)
                     || (matOverHead == Material.LONG_GRASS)
                     || (matOverHead == Material.YELLOW_FLOWER)
                     || (matOverHead == Material.RED_ROSE)
                     || (matOverHead == Material.CROPS)
                     || (matOverHead == Material.DEAD_BUSH)
                     || (matOverHead == Material.SUGAR_CANE)
                     || (matOverHead == Material.WHEAT)
                     || (matOverHead == Material.WALL_SIGN)
                     || (matOverHead == Material.SIGN_POST)
                     || (matOverHead == Material.VINE)
                     || (matOverHead == Material.SOIL)
                     || (matOverHead == Material.SEEDS)
                     || (matOverHead == Material.RAILS)
                     || (matOverHead == Material.TORCH)
                     || (matOverHead == Material.MAP)
                     || (matOverHead != Material.ITEM_FRAME)
                     || (matOverHead != Material.PAINTING))
               {
                  res = true; // probably save
               }
            }
         }
      }

      return res;
   }
}