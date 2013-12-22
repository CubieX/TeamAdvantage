package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class TAEntityListener implements Listener
{
   private TeamAdvantage plugin = null;
   private TASchedulerHandler schedHandler = null;
   private ArrayList<Integer> exploding = new ArrayList<Integer>();

   public TAEntityListener(TeamAdvantage plugin, TASchedulerHandler schedHandler)
   {        
      this.plugin = plugin;
      this.schedHandler = schedHandler;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
   {
      // TODO die ID des Pfeils muss geloescht werden, nachdem er seinen Bonus-Effekt applied hat
      // aber ProjectileHitEvent feuert VOR dem EntityDamageByEntity event...
      // wenn es nicht anders geht, MetaData verwenden zum markieren des Pfeils. Siehe Plugin "Towerz"
      // und Tutorial: http://forums.bukkit.org/threads/your-total-guide-to-exploding-arrows-your-total-guide-to.168150/
      
      // EXPLODING ARROWS handler =====================================
      if(e.getEntity() instanceof Player)
      {
         Player victim = (Player)e.getEntity();

         if(e.getDamager() instanceof Arrow)
         {
            Arrow arrow = (Arrow)e.getDamager();

            if(null != arrow.getShooter()) // null when arrow was shot by plugin
            {
               if(arrow.getShooter() instanceof Player)
               {
                  Player shooter = (Player)arrow.getShooter();
                  
                  if(exploding.contains(arrow.getEntityId()))
                  {
                     // add damage because arrow is an exploding one
                     e.setDamage(e.getDamage() * 2);

                     if(TeamAdvantage.debug){victim.sendMessage(ChatColor.GOLD + "Hit by exploding arrow! Suffered: " + ChatColor.WHITE + (e.getDamage() * 100 / 100) + ChatColor.GOLD + " damage.");}
                     if(TeamAdvantage.debug){shooter.sendMessage(ChatColor.GOLD + "Your exploding arrow hit a player and inflicted " + ChatColor.WHITE + (e.getDamage() * 100 / 100) + ChatColor.GOLD + " damage.");}
                  }
               }
            }
         }
      }
      // END EXPLODING ARROWS handler ============
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onEntityDamageEvent(EntityDamageEvent e)
   {

   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onProjectileLaunchEvent(ProjectileLaunchEvent e)
   {
      Projectile projectile = e.getEntity();

      if (projectile.getShooter() instanceof Player)
      {
         Player p = (Player)projectile.getShooter();
         TATeam team = plugin.getTeamOfPlayer(p.getName());

         if(null != team)
         {
            // TODO add proximity check to team members here before applying bonus effects
            
            // mark arrow as explosive           
            exploding.add(projectile.getEntityId());            
         }
      }
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onProjectileHitEvent(ProjectileHitEvent e)
   {      
      // read metadata or check list for projectile here to see, if it has special attributes
      // (exploding, more damage...)

      if (e.getEntity() instanceof Arrow)
      {
         if(exploding.contains(e.getEntity().getEntityId()))
         {
            e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), 4.0f);
         }
      }
      
      // schedule projectiles special attribute mark to be deleted in next tick
      schedHandler.startProjectileSpecialAttributeCleanerScheduler_SynchDelayed(e.getEntity().getEntityId());
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onPlayerInteractEvent(PlayerInteractEvent e)
   {

   }

   //================================================================================================    
   /*@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
   public void onAsyncQueryResultRetrievedEvent(final AsyncQueryResultRetrievedEvent e)
   {
      // Beware! This event is asynchronously called!
      // so make sure to use a sync task if you are accessing any Bukkit API methods
      Bukkit.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         @Override
         public void run()
         {            
            if(null != e.getResultSet())
            {
               // do stuff with resultSet
            }
            else
            {
               // BEWARE: sender may be null!
               // e.getSender().sendMessage(TeamAdvantage.logPrefix + "Request timed out! (" + TeamAdvantage.MAX_RETRIEVAL_TIME + " ms)");
            }
         }

      });
   }*/
   
   //############################################################################
   
   public ArrayList<Integer> getExplodingList()
   {
      return exploding;
   }
}
