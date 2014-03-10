package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.CubieX.TeamAdvantage.TABonusEffect;
import com.github.CubieX.TeamAdvantage.TATeam;
import com.github.CubieX.TeamAdvantage.TeamAdvantage;

public class BuyCmd implements ISubCmdExecutor
{
   @Override
   public void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args)
   {      
      if(sender.hasPermission("teamadvantage.use"))
      {
         TATeam teamByLeader = null;

         if(args.length == 1) // show list of all effects enabled in config // TODO add pagination
         {
            sender.sendMessage("§2" + "Effekte -------------------------");

            for(String effName : TeamAdvantage.availableBonusEffects.keySet())
            {
               TABonusEffect tbe = TeamAdvantage.availableBonusEffects.get(effName);

               sender.sendMessage("§2" + tbe.getCategory() + " - " + tbe.getName() + ": §f" + tbe.getDescription() + " Dauer: " +
                     plugin.getDurationAsStringWithUnits(tbe.getDuration()) + ", Preis: " + tbe.getPrice() + " " + TeamAdvantage.currencyPlural);
            }

            return;
         }

         if(args.length == 2) // show list of all enabled effects in given category // TODO add pagination
         {
            if(plugin.categoryExists(args[1]))
            {
               sender.sendMessage("§2" + args[1].toUpperCase() + "-Effekte: -------------------------"); 

               for(String effName : TeamAdvantage.availableBonusEffects.keySet())
               {
                  TABonusEffect tbe = TeamAdvantage.availableBonusEffects.get(effName);

                  if(tbe.getCategory().equalsIgnoreCase(args[1].toUpperCase()))
                  {
                     sender.sendMessage("§2" + tbe.getName() + ": §f" + tbe.getDescription() + " Dauer: " +
                           plugin.getDurationAsStringWithUnits(tbe.getDuration()) + ", Preis: " + tbe.getPrice() + " " + TeamAdvantage.currencyPlural);
                  }
               }
            }

            return;
         }

         if(args.length == 3)
         {
            if(args[1].equalsIgnoreCase("info")) // display details about given bonus effect
            {
               if(TeamAdvantage.availableBonusEffects.containsKey(args[2].toUpperCase()))
               {
                  TABonusEffect tbe = TeamAdvantage.availableBonusEffects.get(args[2].toUpperCase());
                  sender.sendMessage("§2" + tbe.getCategory() + "-Effekt " +  "§2" + tbe.getName() + ": §f" + tbe.getDescription() + " Dauer: " +
                        plugin.getDurationAsStringWithUnits(tbe.getDuration()) + ", Preis: " + tbe.getPrice() + " " + TeamAdvantage.currencyPlural);
               }

               return;
            }

            // check if argument is an available effect, so player seems to be about to buy an effect
            if(TeamAdvantage.availableBonusEffects.containsKey(args[1].toUpperCase())) // buy an effect
            {
               if(player != null)
               {
                  teamByLeader = plugin.getTeamByLeader(player.getName());
                  // TODO kaufen implementieren
               }
               else
               {
                  sender.sendMessage(TeamAdvantage.logPrefix + "Only players can use this command!");
               }

               return;
            }
         }         
      }
   }
}