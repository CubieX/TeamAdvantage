package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TAEntityListener implements Listener
{
   private TeamAdvantage plugin = null;
   private TASchedulerHandler schedHandler = null;
   private Economy econ = null;
   private TAChatManager chatMan = null;
   Set<Player> teamChatRecipients = new HashSet<Player>();
   private ArrayList<Integer> exploding = new ArrayList<Integer>();

   public TAEntityListener(TeamAdvantage plugin, TASchedulerHandler schedHandler, Economy econ, TAChatManager chatMan)
   {        
      this.plugin = plugin;
      this.schedHandler = schedHandler;
      this.econ = econ;
      this.chatMan = chatMan;
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
            e.getEntity().getWorld().createExplosion(e.getEntity().getLocation().getX(), e.getEntity().getLocation().getY(),
                  e.getEntity().getLocation().getZ(), 4.0f, false, TeamAdvantage.doBlockDamage); // 4.0f is about the strength of a TNT block.
            // Only affects block damage. Not player damage!
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
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e)
   {
      // FIXME HeroChat horcht auf MONITOR, canceled das event und interessiert sich nicht fuer die recipient-Liste! -> Workaround?
      if(TAChatManager.teamChat.contains(e.getPlayer().getName()))
      {
         TATeam teamOfSender = plugin.getTeamOfPlayer(e.getPlayer().getName());

         if(null != teamOfSender)
         {
            e.setCancelled(true);
            chatMan.handleTeamChat(e.getPlayer(), teamOfSender, e.getMessage());
         }
         else
         {
            // should never happen, but anyway
            TAChatManager.teamChat.remove(e.getPlayer().getName());
         }
      }
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

   public void setTeamChatRecipients(Set<Player> recipients)
   {
      this.teamChatRecipients = null;
      this.teamChatRecipients = recipients;
   }   
}
