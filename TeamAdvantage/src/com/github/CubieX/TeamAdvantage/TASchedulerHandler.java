package com.github.CubieX.TeamAdvantage;

public class TASchedulerHandler
{
   private TeamAdvantage plugin = null;

   public TASchedulerHandler(TeamAdvantage plugin)
   {
      this.plugin = plugin;
   }
   
   public void startProjectileSpecialAttributeCleanerScheduler_SynchDelayed(final Integer entityID)
   {
      plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable()
      {
         public void run()
         {
            plugin.getExplodingList().remove(entityID);
         }
      }, 1L); // 1 tick delay
   }
}
