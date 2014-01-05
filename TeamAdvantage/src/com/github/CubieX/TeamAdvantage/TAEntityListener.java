package com.github.CubieX.TeamAdvantage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
import org.bukkit.metadata.FixedMetadataValue;
import com.github.CubieX.MACViewer.AsyncUUIDRetrievedEvent;

public class TAEntityListener implements Listener
{
   private TeamAdvantage plugin = null;
   private TAChatManager chatMan = null;
   Set<Player> teamChatRecipients = new HashSet<Player>();
   private enum Effect {TA_EXPLODING, TA_PIERCING, TA_SEEKER}

   public TAEntityListener(TeamAdvantage plugin, TAChatManager chatMan)
   {
      this.plugin = plugin;
      this.chatMan = chatMan;
      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e)
   {
      // check for special attributes of weapons or projectiles or passive bonus effects

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

                  // EXPLODING ARROWS handler ============
                  if(arrow.hasMetadata(Effect.TA_EXPLODING.name()))
                  {
                     // apply special effects

                     double dmgFactor = 1.0;

                     if(teamMembersNear(victim))
                     {
                        // apply defence effects of victim
                        dmgFactor = 0.75;
                        if(TeamAdvantage.debug){victim.sendMessage(ChatColor.GOLD + "Damage reduced to 75% (team proximity)");}
                     }
                     else
                     {
                        dmgFactor = 1.5;
                        if(TeamAdvantage.debug){victim.sendMessage(ChatColor.GOLD + "Damage increased to 150% (no team proximity)");}
                     }

                     e.setDamage(e.getDamage() * dmgFactor);

                     if(TeamAdvantage.debug){victim.sendMessage(ChatColor.GOLD + "Hit by exploding arrow! Suffered: " + ChatColor.WHITE + (e.getDamage() * 100 / 100) + ChatColor.GOLD + " damage.");}
                     if(TeamAdvantage.debug){shooter.sendMessage(ChatColor.GOLD + "Your exploding arrow hit a player and inflicted " + ChatColor.WHITE + (e.getDamage() * 100 / 100) + ChatColor.GOLD + " damage.");}
                  }
               }
            }
         }
      }

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
            if(teamMembersNear(p))
            {
               // add special attack attributes to projectile

               // TODO add proximity check to team members here before applying bonus effects

               // EXPLODING ARROW HANDLER ================================
               if(projectile instanceof Arrow)
               {
                  projectile.setMetadata(Effect.TA_EXPLODING.name(), new FixedMetadataValue(plugin, true));
               }
               // END EXPLOSIVE ARROW HANDLER ============================
            }
         }
      }
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onProjectileHitEvent(ProjectileHitEvent e)
   {
      // EXPLODING ARROW HANDLER ================================
      if(e.getEntity().hasMetadata(Effect.TA_EXPLODING.name()))
      {
         e.getEntity().getWorld().createExplosion(e.getEntity().getLocation().getX(), e.getEntity().getLocation().getY(),
               e.getEntity().getLocation().getZ(), 4.0f, false, TeamAdvantage.doBlockDamage);
         // 4.0f is about the strength of a TNT block. Only affects block damage. Not player damage! (does 0.5 damage)
         // does also provide knockback
      }
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e)
   {      
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
   @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
   public void onAsyncUUIDRetrievedEvent(final AsyncUUIDRetrievedEvent e)
   {
      // CAUTION! This event is asynchronously called!
      // so make sure to use a sync task if you are accessing any Bukkit API methods

      if(null != e.getPlayerUUID()) // will be null if request timed out (-> TeamAdvabtage.MAX_RETRIEVAL_TIME was exceeded)
      {
         if(!e.getPlayerUUID().equals(""))
         {
            if(TeamAdvantage.debug){TeamAdvantage.log.info(ChatColor.GREEN + "UUID von " + ChatColor.WHITE + e.getPlayerName() + ChatColor.GREEN + ":\n" + ChatColor.GREEN + e.getPlayerUUID());}
         }
         else
         {
            if(TeamAdvantage.debug){TeamAdvantage.log.info(ChatColor.YELLOW + "Dieser Spieler ist nicht bei Mojang registriert!");}
         }
      }

      /*
      Bukkit.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         @Override
         public void run()
         {

         }
      });*/
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

   public void setTeamChatRecipients(Set<Player> recipients)
   {
      this.teamChatRecipients = null;
      this.teamChatRecipients = recipients;
   }

   public boolean teamMembersNear(Player p)
   {
      boolean res = false;
      List<Entity> entities = p.getNearbyEntities(TeamAdvantage.maxBonusEffectsActivationDistance,
            TeamAdvantage.maxBonusEffectsActivationDistance,
            TeamAdvantage.maxBonusEffectsActivationDistance);      

      if(!entities.isEmpty())
      {
         TATeam team = plugin.getTeamOfPlayer(p.getName());

         if(null != team) // player is a team member
         {
            for(Entity ent : entities)
            {
               if(ent instanceof Player)
               {
                  Player nearP = (Player)ent;

                  if(team.getMembers().contains(nearP.getName()) || team.getLeader().equals(nearP.getName()))
                  {
                     res = true;
                     break;
                  }
               }
            }
         }
      }

      return res;
   }
}
