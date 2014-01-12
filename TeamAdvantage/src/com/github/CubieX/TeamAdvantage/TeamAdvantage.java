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
import java.util.UUID;
import java.util.logging.Logger;
import lib.PatPeter.sqlLibrary.SQLite.sqlCore;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
   public static boolean doBlockDamage = false; // if explosion effects should do block damage
   public static int notificationDelay = 10;    // cycle time to notify team leaders and players of pending requests and invitations
   public static int maxBonusEffectsActivationDistance = 10; // max sllowed distance from other team members to gain bonus effects 
   public static String currencySingular = "";
   public static String currencyPlural = "";
   public static int costsCreateTeam = 0;
   public static int costsPerMemberPerTeamFeeCycle = 0;
   public static int teamFeeCycle = 0; // cycle in days to specify how often the "costsPerMemberPerTeamFeeCycle" are being withdrawn from team account

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
         log.severe(logPrefix + "will be disabled now. Vault was not found!");
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

   public void readConfigValues()
   {      
      boolean exceed = false;
      boolean invalid = false;

      if(getConfig().isSet("debug")){debug = getConfig().getBoolean("debug");}else{invalid = true;}
      if(getConfig().isSet("doBlockDamage")){doBlockDamage = getConfig().getBoolean("doBlockDamage");}else{invalid = true;}

      notificationDelay = cHandler.getConfig().getInt("notificationDelay");
      if(notificationDelay < 0){notificationDelay = 0; exceed = true;}
      if(notificationDelay > 60){notificationDelay = 60; exceed = true;}
      
      maxBonusEffectsActivationDistance = cHandler.getConfig().getInt("maxBonusEffectsActivationDistance");
      if(maxBonusEffectsActivationDistance < 1){maxBonusEffectsActivationDistance = 1; exceed = true;}
      if(maxBonusEffectsActivationDistance > 100){maxBonusEffectsActivationDistance = 100; exceed = true;}

      costsCreateTeam = cHandler.getConfig().getInt("costsCreateTeam");
      if(costsCreateTeam < 0){costsCreateTeam = 0; exceed = true;}
      if(costsCreateTeam > 100000){costsCreateTeam = 100000; exceed = true;}

      costsPerMemberPerTeamFeeCycle = cHandler.getConfig().getInt("costsAffiliateMember");
      if(costsPerMemberPerTeamFeeCycle < 0){costsPerMemberPerTeamFeeCycle = 0; exceed = true;}
      if(costsPerMemberPerTeamFeeCycle > 100000){costsPerMemberPerTeamFeeCycle = 100000; exceed = true;}

      teamFeeCycle = cHandler.getConfig().getInt("teamFeeCycle");
      if(teamFeeCycle < 0){teamFeeCycle = 0; exceed = true;}
      if(teamFeeCycle > 365){teamFeeCycle = 365; exceed = true;}

      if(getConfig().isSet("currencySingular")){currencySingular = getConfig().getString("currencySingular");}else{invalid = true;}
      if(getConfig().isSet("currencyPlural")){currencyPlural = getConfig().getString("currencyPlural");}else{invalid = true;}

      if(exceed)
      {
         log.warning(logPrefix + "One or more config values are exceeding their allowed range. Please check your config file!");
      }

      if(invalid)
      {
         log.warning(logPrefix + "One or more config values are invalid. Please check your config file!");
      }
   }

   @Override
   public void onDisable()
   {
      this.getServer().getScheduler().cancelTasks(this);
      
      try
      {
         globSQLman.getSQLcore().getConnection().close();
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
            if(team.getMembers().contains(playerName)
                  || team.getLeader().contains(playerName))
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
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------\n" +
               ChatColor.GREEN + "Liste der Teams  -  " + "Teams Gesamt: " + ChatColor.WHITE + countAll + ChatColor.GREEN +
               "\nSeite (" + String.valueOf(page) + " von " + totalPageCount + ")\n" +      
               ChatColor.WHITE + "----------------------------------------");

         if(list.isEmpty())
         {
            sender.sendMessage(ChatColor.WHITE + "Keine Eintraege.");
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

         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
      }
      else
      {
         String pageTerm = "Seiten";

         if(totalPageCount == 1)
         {
            pageTerm = "Seite";
         }         

         sender.sendMessage(ChatColor.YELLOW + "Die Liste hat nur " + ChatColor.WHITE + totalPageCount + ChatColor.YELLOW + " " + pageTerm + "!");
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
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
         sender.sendMessage(ChatColor.GREEN + "Team: " + ChatColor.WHITE + team.getName() + ChatColor.GREEN + " Tag: " + ChatColor.WHITE +
               "[" + team.getTag() + "]" + ChatColor.GREEN + " Mitglieder: " + ChatColor.WHITE + countAll +
               ChatColor.GREEN + " Geld: " + ChatColor.WHITE + ((int)team.getMoney()) + " " + TeamAdvantage.currencyPlural +
               ChatColor.GREEN + " XP: " + ChatColor.WHITE + team.getXP());
         sender.sendMessage(ChatColor.GREEN + "Liste der Mitglieder - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");

         if(list.isEmpty())
         {
            sender.sendMessage(ChatColor.WHITE + "Keine Eintraege.");
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

         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
      }
      else
      {
         String pageTerm = "Seiten";

         if(totalPageCount == 1)
         {
            pageTerm = "Seite";
         }

         sender.sendMessage(ChatColor.YELLOW + "Die Liste hat nur " + ChatColor.WHITE + totalPageCount + ChatColor.YELLOW + " " + pageTerm + "!");
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
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
         sender.sendMessage(ChatColor.GREEN + "TeamAdvantage Hilfe - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");      
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");

         if(list.isEmpty())
         {
            sender.sendMessage(ChatColor.WHITE + "Keine Eintraege.");
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

         sender.sendMessage(ChatColor.WHITE + "----------------------------------------");
      }
      else
      {
         sender.sendMessage(ChatColor.YELLOW + "Die Hilfe hat nur " + ChatColor.WHITE + totalPageCount + ChatColor.YELLOW + " Seiten!");
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
}


