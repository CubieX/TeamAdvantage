package com.github.CubieX.TeamAdvantage;

public class TASchedulerHandler
{
   private TeamAdvantage plugin = null;

   public TASchedulerHandler(TeamAdvantage plugin)
   {
      this.plugin = plugin;
   }
   
   public void startBlockChangeDelayerScheduler_SynchRepeating()
   {
      plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable()
      {
         public void run()
         {
            
         }
      }, 20L, 20L); // 1 second delay, 1 second cycle
   }
}
