/*
 * TeamAdvantage - A CraftBukkit plugin that provides bonus effects and abilities for teamed up players
 * Copyright (C) 2013  CubieX
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.github.CubieX.TeamAdvantage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import lib.PatPeter.sqlLibrary.SQLite.sqlCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamAdvantage extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   public static final String logPrefix = "[TeamAdvantage] "; // Prefix to go in front of all log entries
   public static final int MAX_RETRIEVAL_TIME = 1000;  // max time in ms to wait for a SELECT query to deliver its result
   // This prevents async task jam in case DB is unreachable or connection is very slow
   static ArrayList<TATeam> teams = new ArrayList<TATeam>();
   static enum TABLES {tbTeams, tbMemberships, tbInvitations, tbRequests};

   private TeamAdvantage plugin = null;
   private TACommandHandler comHandler = null;
   private TAConfigHandler cHandler = null;
   private TAEntityListener eListener = null;
   private TASchedulerHandler schedHandler = null;
   private TASQLManager sqlMan = null; // SQL Manager for wrapping query actions

   public static boolean debug = false;

   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {
      this.plugin = this;
      cHandler = new TAConfigHandler(this);      
      sqlMan = new TASQLManager(this);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }
      
      sqlMan.initializeSQLite();
      sqlMan.loadTeamsFromDB();
      eListener = new TAEntityListener(this);      
      comHandler = new TACommandHandler(this, cHandler, sqlMan);      
      getCommand("ta").setExecutor(comHandler);
      schedHandler = new TASchedulerHandler(this);
      readConfigValues();

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

   public void readConfigValues()
   {      
      boolean exceed = false;
      boolean invalid = false;

      if(getConfig().isSet("debug")){debug = getConfig().getBoolean("debug");}else{invalid = true;}

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
      cHandler = null;
      eListener = null;
      comHandler = null;
      schedHandler = null;
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }

   // ####################################################################################################

   public TASQLManager getSQLman()
   {
      return sqlMan;
   }
   
   /**
    * Returns the team matching the given name
    * 
    * @param teamName the team to get
    * 
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
    * @param playerName the player to get the team and leader
    * 
    * @return res applicableTeam the team this player is leader of
    * */
   public TATeam getTeamByLeader(String playerName)
   {
      TATeam applicableTeam = null;
      
      for(TATeam team : TeamAdvantage.teams)
      {
         if(team.getLeader().equals(playerName))
         {
            applicableTeam = team;
            break;
         }
      }

      return applicableTeam;
   }
   
   /**
    * Returns if this player is member of a specified team.
    * 
    * @param playerName the player to check
    * @param teamName the team to check against
    * 
    * @return res TRUE if the player is member of the given team
    * */
   public boolean isMemberOfSpecificTeam(String playerName, String teamName)
   {
      boolean res = false;
      
      for(TATeam team : TeamAdvantage.teams)
      {
         if(team.getMembers().contains(teamName))
         {
            res = true;
            break;
         }
      }

      return res;
   }   
}


