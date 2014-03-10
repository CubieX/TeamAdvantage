/*
 * TeamAdvantage - A CraftBukkit plugin that provides bonus effects and abilities for teamed up players
 * Copyright (C) 2014  CubieX
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.github.CubieX.TeamAdvantage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import lib.PatPeter.sqlLibrary.SQLite.sqlCore;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.CubieX.MACViewer.MACViewer;

public class TeamAdvantage extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   public static final String logPrefix = "[TeamAdvantage] "; // Prefix to go in front of all log entries
   public static final int MAX_RETRIEVAL_TIME = 2000;  // max time in ms to wait for an async SELECT query to deliver its result
   // This prevents async task jam in case DB is unreachable or connection is very slow
   public static final int MAX_CHAT_TAG_LENGTH = 5;
   public static ArrayList<TATeam> teams = new ArrayList<TATeam>();
   public static Chat chat = null;
   public static Economy econ = null;
   public static Permission perm = null;
   public static MACViewer accMan = null;

   private TACommandHandler comHandler = null;
   private TAConfigHandler cHandler = null;
   private TAChatManager chatMan = null;
   private TAEntityListener eListener = null;
   private TASchedulerHandler schedHandler = null;
   private TAGlobSQLManager globSQLman = null; // Global SQL Manager for wrapping non-team-related query actions
   private TATeamSQLManager teamSQLman = null; // Team SQL Manager for wrapping team related query actions
   private final int contentLinesPerPage = 10;

   // config values
   public static boolean debug = false;
   // key is world name from config 'pvpWorlds' and value=TRUE means, there are currently at least two hostile teams in this world  
   public static HashMap<String, Boolean> pvpWorlds = new HashMap<String, Boolean>(); 
   public static boolean doBlockDamage = false; // if explosion effects should do block damage
   public static int notificationDelay = 10;    // cycle time to notify team leaders and players of pending requests and invitations
   public static int maxBonusEffectsActivationDistance = 10; // max sllowed distance from other team members to gain bonus effects 
   public static String currencySingular = "";
   public static String currencyPlural = "";
   public static int costsCreateTeam = 0;
   public static int costsPerMemberPerTeamFeeCycle = 0;
   public static int teamFeeCycle = 0; // cycle in days to specify how often the "costsPerMemberPerTeamFeeCycle" are being withdrawn from team account
   public static int costSetTeamHome = 0;
   public static HashMap<String, TABonusEffect> availableBonusEffects = new HashMap<String, TABonusEffect>(); // Predefined set of all implemented bonus effects
   public static HashMap<String, TABonusEffect> bonusEffects = new HashMap<String, TABonusEffect>(); // set read from config to define available bonus effects
   public static enum Category {ATTACK, DEFENCE, FARM};

   //*************************************************
   private final String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   // TODO Statt Spielername die Mojang UUID holen mit player.getUniqueID() -> Geht nur wenn Server im Online-Mode und Spieler online!
   // um fuer die Aenderung von namen die Mojang bald erlaubt vorbereitet zu sein!
   // diese muss auch in die DB (evt. beides lokal und in der DB vorhalten)
   // Rueckumwandlung der UUID in einen Spieler per loop ueber die online-playerliste und Vergleich der UUID. (siehe unten Hilfmethode getPlayerByUUID())

   @Override
   public void onEnable()
   {
      cHandler = new TAConfigHandler(this); 
      teamSQLman = new TATeamSQLManager(this);
      globSQLman = new TAGlobSQLManager(this, teamSQLman);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         disablePlugin();
         return;
      }

      if (!setupPermissions())
      {
         log.info(logPrefix + "- Disabled because could not hook Vault to permission system!");
         disablePlugin();
         return;
      }

      if (!setupEconomy())               
      {
         log.severe(logPrefix + "- Disabled because could not hook Vault to economy system!");
         disablePlugin();
         return;
      }

      if (!setupChat())
      {
         log.info(logPrefix + "- Disabled because could not hook Vault to chat system!");
         disablePlugin();
         return;
      }

      if (!getMACViewer())
      {
         log.info(logPrefix + "- Disabled because could not hook into MACViewer!");
         disablePlugin();
         return;
      }

      initAvailableBonusEffectsList();

      readConfigValues();

      globSQLman.initializeSQLite();
      globSQLman.loadTeamsFromDB(null);
      schedHandler = new TASchedulerHandler(this);
      chatMan = new TAChatManager(schedHandler);
      eListener = new TAEntityListener(this, chatMan);      
      comHandler = new TACommandHandler(this, cHandler);      
      getCommand("ta").setExecutor(comHandler);

      schedHandler.startNotifierScheduler_SynchRepeating();

      if(teamFeeCycle > 0)
      {
         schedHandler.startTeamFeeManagerScheduler_SyncRep();
      }

      log.info(logPrefix + "version " + getDescription().getVersion() + " is enabled!");            
   }

   private boolean checkConfigFileVersion()
   {      
      boolean configOK = false;     

      if(getConfig().isSet("config_version"))
      {
         String configVersion = cHandler.getConfig().getString("config_version");

         if(configVersion.equals(usedConfigVersion))
         {
            configOK = true;
         }
      }

      return (configOK);
   }

   private boolean setupPermissions()
   {
      if (getServer().getPluginManager().getPlugin("Vault") == null)
      {
         return false;
      }
      RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
      if (permissionProvider != null)
      {
         perm = permissionProvider.getProvider();
      }
      return (perm != null);
   }

   private boolean setupChat()
   {
      if (getServer().getPluginManager().getPlugin("Vault") == null)
      {
         return false;
      }
      RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
      if (chatProvider != null) {
         chat = chatProvider.getProvider();
      }

      return (chat != null);
   }

   private boolean setupEconomy()
   {
      if (getServer().getPluginManager().getPlugin("Vault") == null)
      {
         return false;
      }
      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp == null)
      {
         return false;
      }
      econ = rsp.getProvider();
      return (econ != null);
   }

   private boolean getMACViewer()
   {  
      accMan = (MACViewer)Bukkit.getServer().getPluginManager().getPlugin("MACViewer");

      return (accMan != null);
   }

   private void initAvailableBonusEffectsList()
   {
      availableBonusEffects.put("EXPARR", new TABonusEffect("EXPARR", "Pfeile explodieren beim Aufschlag.", Category.ATTACK.name())); // exploding arrows
      availableBonusEffects.put("PARMOR", new TABonusEffect("PARMOR", "Erhoehter Schutz gegen Projektile.", Category.DEFENCE.name())); // armor against projectiles
      availableBonusEffects.put("DBOOST", new TABonusEffect("DBOOST", "Mehr Dops beim Rohstoffabbau.", Category.FARM.name())); // Drop boost
   }

   public void readConfigValues()
   {      
      boolean exceed = false;
      boolean invalid = false;

      if(getConfig().isSet("debug")){debug = getConfig().getBoolean("debug");}else{invalid = true;}
      if(getConfig().isSet("doBlockDamage")){doBlockDamage = getConfig().getBoolean("doBlockDamage");}else{invalid = true;}

      pvpWorlds.clear();

      if(cHandler.getConfig().contains("pvpWorlds"))
      {
         for(String world : cHandler.getConfig().getStringList("pvpWorlds"))
         {  
            if(null != Bukkit.getServer().getWorld(world))
            {
               pvpWorlds.put(Bukkit.getServer().getWorld(world).getName(), false); // get world with correct cases
            }
            else
            {
               invalid = true;
            }
         }
      }
      else
      {
         invalid = true;
      }

      if(cHandler.getConfig().isSet("notificationDelay"))
      {
         notificationDelay = cHandler.getConfig().getInt("notificationDelay");      
         if(notificationDelay < 0){notificationDelay = 0; exceed = true;}
         if(notificationDelay > 60){notificationDelay = 60; exceed = true;}
      }
      else
      {
         invalid = true;
      }

      if(cHandler.getConfig().isSet("maxBonusEffectsActivationDistance"))
      {
         maxBonusEffectsActivationDistance = cHandler.getConfig().getInt("maxBonusEffectsActivationDistance");
         if(maxBonusEffectsActivationDistance < 1){maxBonusEffectsActivationDistance = 1; exceed = true;}
         if(maxBonusEffectsActivationDistance > 100){maxBonusEffectsActivationDistance = 100; exceed = true;}
      }
      else
      {
         invalid = true;
      }

      if(cHandler.getConfig().isSet("costsCreateTeam"))
      {
         costsCreateTeam = cHandler.getConfig().getInt("costsCreateTeam");
         if(costsCreateTeam < 0){costsCreateTeam = 0; exceed = true;}
         if(costsCreateTeam > 100000){costsCreateTeam = 100000; exceed = true;}
      }
      else
      {
         invalid = true;
      }

      if(cHandler.getConfig().isSet("costsPerMemberPerTeamFeeCycle"))
      {
         costsPerMemberPerTeamFeeCycle = cHandler.getConfig().getInt("costsAffiliateMember");
         if(costsPerMemberPerTeamFeeCycle < 0){costsPerMemberPerTeamFeeCycle = 0; exceed = true;}
         if(costsPerMemberPerTeamFeeCycle > 100000){costsPerMemberPerTeamFeeCycle = 100000; exceed = true;}
      }
      else
      {
         invalid = true;
      }

      if(cHandler.getConfig().isSet("teamFeeCycle"))
      {
         teamFeeCycle = cHandler.getConfig().getInt("teamFeeCycle");
         if(teamFeeCycle < 0){teamFeeCycle = 0; exceed = true;}
         if(teamFeeCycle > 365){teamFeeCycle = 365; exceed = true;}
      }
      else
      {
         invalid = true;
      }

      if(cHandler.getConfig().isSet("costSetTeamHome"))
      {
         costSetTeamHome = cHandler.getConfig().getInt("costSetTeamHome");
         if(costSetTeamHome < 0){costSetTeamHome = 0; exceed = true;}
         if(costSetTeamHome > 100000){costSetTeamHome = 100000; exceed = true;}
      }
      else
      {
         invalid = true;
      }

      if(getConfig().isSet("currencySingular")){currencySingular = getConfig().getString("currencySingular");}else{invalid = true;}
      if(getConfig().isSet("currencyPlural")){currencyPlural = getConfig().getString("currencyPlural");}else{invalid = true;}

      // read bonus effects
      bonusEffects.clear();

      if(cHandler.getConfig().contains("bonusEffects"))
      {
         int eCount = 0;
         int price = 0;
         int durationSeconds = 0;
         int durOriginal = 0;
         String durTempString = "";

         for(String effect : cHandler.getConfig().getConfigurationSection("bonusEffects").getKeys(false))
         { 
            if(availableBonusEffects.containsKey(effect.toUpperCase()))
            {
               if(getConfig().isSet("bonusEffects." + effect + ".price"))
               {
                  price = getConfig().getInt("bonusEffects." + effect + ".price");                  
                  if(price < 0){price = 0; exceed = true;}
                  if(price > 1000000){price = 1000000; exceed = true;}
               }
               else
               {
                  invalid = true;
                  break;
               }

               if(getConfig().isSet("bonusEffects." + effect + ".duration"))
               {
                  durTempString = getConfig().getString("bonusEffects." + effect + ".duration");
                  
                  if(isPositiveInteger(durTempString.substring(0, durTempString.length() - 1)))
                  {
                     durOriginal = Integer.parseInt(durTempString.substring(0, durTempString.length() - 1));
                     if(durOriginal < 1){durOriginal = 1; exceed = true;}
                     if(durOriginal > 3600){durOriginal = 3600; exceed = true;}
                  }

                  durationSeconds = getDurationInSeconds(String.valueOf(durOriginal) + durTempString.substring(durTempString.length() - 1, durTempString.length()));                  
               }
               else
               {
                  invalid = true;
                  break;
               }

               // add all info of effect to list
               bonusEffects.put(effect.toUpperCase(), availableBonusEffects.get(effect.toUpperCase())); // copy reference of effect to list
               // add configured price and duration to current effect object
               bonusEffects.get(effect.toUpperCase()).setPrice(price);
               bonusEffects.get(effect.toUpperCase()).setDuration(durationSeconds); // FIXME duration wird nicht korrekt geladen von Config!
               eCount++;
            }
            else
            {
               invalid = true;
               break;
            }
         }

         if(debug){TeamAdvantage.log.info("Initialized " + eCount + " configured bonus effects.");}

         if(eCount == 0) // no or no valid effects found in config
         {
            invalid = true;
         }
      }
      else
      {
         invalid = true;
      }

      if(exceed)
      {
         log.warning(logPrefix + "One or more config values are exceeding their allowed range. Please check your config file!");
      }

      if(invalid)
      {
         log.severe(logPrefix + "One or more config values are invalid. Please check your config file!");
         disablePlugin();
      }
   }

   @Override
   public void onDisable()
   {
      Bukkit.getServer().getScheduler().cancelTasks(this);

      try
      {
         if((null != globSQLman.getSQLcore()) && (null != globSQLman.getSQLcore().getConnection()))
         {
            globSQLman.getSQLcore().getConnection().close();
         }
      }
      catch (SQLException ex)
      {
         log.severe(TeamAdvantage.logPrefix + "ERROR on disconnecting from DB! Trace:\n" + ex.getMessage());
         ex.printStackTrace();
      }

      econ = null;
      cHandler = null;
      eListener = null;
      comHandler = null;
      schedHandler = null;
      globSQLman = null;
      teamSQLman = null;
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }

   private void disablePlugin()
   {
      getServer().getPluginManager().disablePlugin(this);        
   }

   // ####################################################################################################
   // ####################################################################################################

   /**
    * <b>Get instance of global SQL manager</b>
    * 
    * @return globSQLman Instance of team SQL manager
    * */
   public TAGlobSQLManager getGlobSQLman()
   {
      return globSQLman;
   }

   /**
    * <b>Get instance of sqlCore</b>    
    *    
    * @return sql_Core Instance of sqlCore
    * */
   public sqlCore getSQLcore()
   {
      return (globSQLman.getSQLcore());
   }

   /**
    * Returns the team matching the given name
    * 
    * @param teamName the team to get    * 
    * @return res applicableTeam the requested team
    * */
   public TATeam getTeamByName(String teamName)
   {
      TATeam applicableTeam = null;

      if((null != teamName) && (!teamName.equals("")))
      {
         for(TATeam team : teams)
         {
            if(team.getName().equalsIgnoreCase(teamName))
            {
               applicableTeam = team;
               break;
            }
         }
      }

      return applicableTeam;
   }

   /**
    * Returns the team where a given player is leader of
    * 
    * @param playerName the player to get the team and leader    * 
    * @return res applicableTeam the team this player is leader of
    * */
   public TATeam getTeamByLeader(String playerName)
   {
      TATeam applicableTeam = null;

      if((null != playerName) && (!playerName.equals("")))
      {
         for(TATeam team : TeamAdvantage.teams)
         {
            if(team.getLeader().equals(playerName))
            {
               applicableTeam = team;
               break;
            }
         }
      }

      return applicableTeam;
   }

   /**
    * Returns the team a player is member of
    * 
    * @param playerName the player to check 
    * @return applicableTeam The team of the player (or null)
    * */
   public TATeam getTeamOfPlayer(String playerName)
   {
      TATeam applicableTeam = null;

      if((null != playerName) && (!playerName.equals("")))
      {
         for(TATeam team : teams)
         {
            if(team.getMembersAndLeader().contains(playerName))
            {
               applicableTeam = team;
               break;
            }
         }
      }

      return applicableTeam;
   }

   /**
    * Returns the team name by given teamID
    * 
    * @param id the teamID to search for 
    * @return applicableTeamsName The team name of the team with this ID
    * */
   public String getTeamNameByTeamID(int id)
   {
      String applicableTeamsName = "";

      if(id >= 0)
      {
         for(TATeam team : teams)
         {
            if(team.getTeamID() == id)
            {
               applicableTeamsName = team.getName();
               break;
            }
         }
      }

      return (applicableTeamsName);
   }

   /**
    * Checks if a given string is a valid integer
    * 
    * @return res TRUE if given string is a valid integer
    * */
   public boolean isValidInteger(String value)
   {
      boolean res = false;

      try
      {
         Integer.parseInt(value);
         res = true;
      }
      catch (NumberFormatException ex)
      {
         // not a valid integer
      }

      return res;
   }

   /**
    * Paginates a string list to display it in chat page-by-page
    * 
    * @param sender The sender to send the list to
    * @param list The list to paginate
    * @param page The page number to display.
    * @param countAll The count of all available entries    
    */
   public void paginateTeamList(CommandSender sender, ArrayList<String> list, int page, int countAll)
   {
      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         if(list.size() > 0)
         {
            totalPageCount = list.size() / contentLinesPerPage;
         }      
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      if(page <= totalPageCount)
      {
         sender.sendMessage("§f" + "----------------------------------------\n" +
               "§a" + "Liste der Teams  -  " + "Teams Gesamt: " + "§f" + countAll + "§a" +
               "\nSeite (" + String.valueOf(page) + " von " + totalPageCount + ")\n" +      
               "§f" + "----------------------------------------");

         if(list.isEmpty())
         {
            sender.sendMessage("§f" + "Keine Eintraege.");
         }
         else
         {
            int i = 0, k = 0;
            page--;

            for (String entry : list)
            {
               k++;
               if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1)))
               {
                  i++;
                  sender.sendMessage(entry);
               }
            }
         }

         sender.sendMessage("§f" + "----------------------------------------");
      }
      else
      {
         String pageTerm = "Seiten";

         if(totalPageCount == 1)
         {
            pageTerm = "Seite";
         }         

         sender.sendMessage("§6" + "Die Liste hat nur " + "§f" + totalPageCount + "§6" + " " + pageTerm + "!");
      }
   }

   /**
    * Paginates a string list to display it in chat page-by-page
    * 
    * @param sender The sender to send the list to
    * @param list The list to paginate
    * @param page The page number to display.
    * @param countAll The count of all available entries
    */
   public void paginateTeamInfoList(CommandSender sender, ArrayList<String> list, int page, int countAll, TATeam team)
   {
      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         if(list.size() > 0)
         {
            totalPageCount = list.size() / contentLinesPerPage;
         }      
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      if(page <= totalPageCount)
      {
         sender.sendMessage("§f" + "----------------------------------------");
         sender.sendMessage("§a" + "Team: " + "§f" + team.getName() + "§a" + " Tag: " + "§f" +
               "[" + team.getTag() + "]" + "§a" + " Mitglieder: " + "§f" + countAll +
               "§a" + " Geld: " + "§f" + ((int)team.getMoney()) + " " + TeamAdvantage.currencyPlural +
               "§a" + " XP: " + "§f" + team.getXP());
         sender.sendMessage("§a" + "Liste der Mitglieder - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");
         sender.sendMessage("§f" + "----------------------------------------");
         // TODO Aktive Effekte anzeigen mit Restdauer

         if(list.isEmpty())
         {
            sender.sendMessage("§f" + "Keine Eintraege.");
         }
         else
         {
            int i = 0, k = 0;
            page--;

            for (String entry : list)
            {
               k++;
               if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1)))
               {
                  i++;
                  sender.sendMessage(entry);
               }
            }
         }

         sender.sendMessage("§f" + "----------------------------------------");
      }
      else
      {
         String pageTerm = "Seiten";

         if(totalPageCount == 1)
         {
            pageTerm = "Seite";
         }

         sender.sendMessage("§6" + "Die Liste hat nur " + "§f" + totalPageCount + "§6" + " " + pageTerm + "!");
      }
   }

   /**
    * Sends the HELP as a paginated list of strings in chat to a player
    * 
    * @param sender The sender to send the list to
    * @param list The list to paginate
    * @param page The page number to display.
    * @param countAll The count of all available entries   
    */
   public void paginateHelpList(CommandSender sender, ArrayList<String> list, int page, int countAll)
   {
      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         if(list.size() > 0)
         {
            totalPageCount = list.size() / contentLinesPerPage;
         }      
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      if(page <= totalPageCount)
      {
         sender.sendMessage("§f" + "----------------------------------------");
         sender.sendMessage("§a" + "TeamAdvantage Hilfe - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");      
         sender.sendMessage("§f" + "----------------------------------------");

         if(list.isEmpty())
         {
            sender.sendMessage("§f" + "Keine Eintraege.");
         }
         else
         {
            int i = 0, k = 0;
            page--;

            for (String entry : list)
            {
               k++;
               if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1)))
               {
                  i++;
                  sender.sendMessage(entry);
               }
            }
         }

         sender.sendMessage("§f" + "----------------------------------------");
      }
      else
      {
         sender.sendMessage("§6" + "Die Hilfe hat nur " + "§f" + totalPageCount + "§6" + " Seiten!");
      }
   }

   /**
    * Checks the new team name for correct format.
    * 
    * @param teamName The new team name to check 
    * @return res TRUE if the check was successful, otherwise FALSE   
    */
   public boolean checkTeamName(String teamName)
   {
      boolean res = false;

      if((teamName.length() >= 4) && (teamName.length() <= 20))
      {
         if(teamName.matches("^[a-zA-Z0-9_]+$"))
         {
            if(!Bukkit.getServer().getOfflinePlayer(teamName).hasPlayedBefore())
            {
               res = true;
            }
         }
      }

      return res;
   }

   /**
    * Checks the new team tag for correct format and uniqueness.
    * 
    * @param tag The new team tag to check 
    * @return res TRUE if the check was successful, otherwise FALSE   
    */
   public boolean checkTeamTag(String tag)
   {
      boolean res = false;

      if((tag.length() >= 1) && (tag.length() <= MAX_CHAT_TAG_LENGTH))
      {
         if(tag.matches("^[a-zA-Z0-9_]+$"))
         {            
            boolean unique = true;

            for(TATeam team : TeamAdvantage.teams)
            {
               if(team.getTag().equalsIgnoreCase(tag))
               {
                  unique = false;
                  break;
               }
            }

            if(unique)
            {
               res = true;
            }
         }
      }

      return res;
   }

   /**
    * <b>Send message to player synchronously</b><br>   
    * Use this for sending messages to a player from out an async task
    * 
    * @param player The player to send the message to
    * */
   public void sendSyncChatMessage(Player player, String message)
   {
      schedHandler.sendSyncChatMessageToPlayer(player, message);
   }

   /** Get players UUID from Bukkit (use only if player is online)
    * Used for parameters of PHP scripts for example.
    * 
    * @param playerName The players name to get the UUID from
    * @return uuid The UUID of the player
    * */
   public String getUUIDbyBukkit(String playerName)
   {
      String uuid = null;
      Player p = Bukkit.getServer().getPlayer(playerName);

      if((null != p) && (p.isOnline()))
      {
         uuid = p.getUniqueId().toString().toLowerCase().replace("-", "");
      }

      return (uuid);
   }

   /**
    * <b>Utility method to get Player by his Mojang UUID</b><br>   
    * Use this whenever you need to retrieve a player from a saved UUID<br>
    * CAUTION: Works only if server is in online-mode and player is online!
    * 
    * @param player The player to get the Mojang UUID from
    * @return p The player if a matching UUID was found
    * */
   public Player getPlayerByUUID(UUID uuid)
   {
      Player p = null;

      for(Player player : Bukkit.getServer().getOnlinePlayers())
      {
         if(player.getUniqueId().equals(uuid))
         {
            p = player;
            break;   
         }
      }

      return p;
   }

   /**
    * <b>Check if given bonus effect coategory exists</b><br>   
    * 
    * @param category The bonus effect category to check for
    * @return res TRUE if category was found, else FALSE
    * */
   public boolean categoryExists(String category)
   {
      boolean res = false;

      for(Category cat : Category.values())
      {
         if(cat.name().equalsIgnoreCase(category))
         {
            res = true;
            break;   
         }
      }

      return res;
   }

   /**
    * <b>Get duration in seconds by ginven config duration string</b>
    * 
    * @param dur Duration as config string (e.g. 1d)
    * @return durationSeconds The duration in seconds or -1 if invalid config string
    * */
   public int getDurationInSeconds(String dur)
   {
      int durInSeconds = -1;
      int durTemp = 0;
      String unit = "";

      if(dur.length() >= 2)
      {
         unit = dur.substring(dur.length() - 1, dur.length()); // get last character e.g. from 200m

         if(isPositiveInteger(dur.substring(0, dur.length() - 1)))
         {
            durTemp = Integer.parseInt(dur.substring(0, dur.length() - 1));
         }

         switch(unit)
         {
         case "d":
            durInSeconds = durTemp * 3600 * 24;
            break;
         case "h":
            durInSeconds = durTemp * 3600;
            break;
         case "m":
            durInSeconds = durTemp * 60;
            break;
         case "s":
            durInSeconds = durTemp;
         default:
            // invalid
         }
      }

      return (durInSeconds);
   }
   
   /**
    * <b>Get duration as time string e.g. 1h or 20m</b>
    * 
    * @param seconds Duration as integer in seconds
    * @return durationString The string representation of this duration with the most suitable unit
    * */
   public String getDurationAsStringWithUnits(int seconds)
   {
      String durString = "";
      
      if(seconds < 60)
      {
         durString = seconds + "s";
      }
      else if(seconds < 3600)
      {
         durString = (seconds / 60) + "m";
         
         if((seconds % 60) > 0) // get remainder
         {
            durString = durString + " " + (seconds % 60) + "s";
         }
      }
      else if(seconds < 86400)
      {
         durString = (seconds / 3600) + "h";
         
         if((seconds % 3600) > 0) // get remainder
         {
            durString = durString + " " + ((seconds % 3600) / 60) + "m";
         }
      }
      else
      {
         durString = (seconds / 3600 / 24) + "d";
         
         if((seconds % 86400) > 0) // get remainder
         {
            durString = durString + " " + ((seconds % 86400) / 3600) + "h";
         }
      }
      
      return (durString);
   }

   /**
    * <b>Check if string is a valid positive integer</b>
    * 
    * @param numStrig The string representing the possible integer value
    * @return res TRUE if the string represents a valid positive integer, else FALSE
    * */
   public boolean isPositiveInteger(String numString)
   {
      boolean res = false;
      int num = 0;

      try
      {
         num = Integer.parseInt(numString);

         if(num >= 0)
         {
            res = true;
         }
      }
      catch (NumberFormatException nex)
      {
         // not a valid positive integer
      }

      return (res);
   }
}