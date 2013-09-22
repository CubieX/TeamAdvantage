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
   static enum TABLES {tbTeams, tbMemberships};

   private TeamAdvantage plugin = null;
   private TACommandHandler comHandler = null;
   private TAConfigHandler cHandler = null;
   private TAEntityListener eListener = null;
   private TASchedulerHandler schedHandler = null;
   private File pFolder; // Folder to store plugin settings file and database
   private sqlCore sqlMan; // SQLite handler

   public static boolean debug = false;

   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {
      this.plugin = this;
      cHandler = new TAConfigHandler(this);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }

      pFolder = new File("plugins" + File.separator + this.getDescription().getName());
      initializeSQLite();
      loadTeamsFromDB();
      eListener = new TAEntityListener(this, sqlMan);      
      comHandler = new TACommandHandler(this, cHandler);      
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

   private void initializeSQLite()
   {
      // Initializing SQLite
      if(debug){log.info(logPrefix + "SQLite Initializing");}

      // Declare SQLite handler
      this.sqlMan = new sqlCore(log, "TeamsDB", pFolder.getPath(), this);

      // Initialize SQLite handler
      this.sqlMan.initialize();

      // Check if the table exists, if it doesn't, create it
      if (!this.sqlMan.checkTable(TABLES.tbTeams.name()))
      {
         log.info(logPrefix + "Creating table tbTeams...");
         String query = "CREATE TABLE tbTeams (" +
               "teamID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "teamName VARCHAR(32) UNIQUE NOT NULL," +
               "leader VARCHAR(32) NOT NULL)"; // holds the team name and the leader
         // only one leader per team allowed
         this.sqlMan.createTable(query);
      }

      // Check if the table exists, if it doesn't, create it
      if (!this.sqlMan.checkTable(TABLES.tbMemberships.name()))
      {
         log.info(logPrefix + "Creating table tbMemberships...");
         String query = "CREATE TABLE tbMemberships (" +
               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
               "fk_teamID INTEGER NOT NULL," +         		
               "member VARCHAR(32) NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID))";
         // membership must reference an existing team
         this.sqlMan.createTable(query);
      }
   }

   private void loadTeamsFromDB()
   {
      HashMap<String, String> teamList= sqlGetTeamList();

      for(String teamName : teamList.keySet())
      {
         teams.add(new TATeam(teamName, teamList.get(teamName), sqlGetMembersOfTeam("teamName")));
      }

      log.info(logPrefix + "Successfully loaded " + teams.size() + " teams from DB."); 
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

   public int sqlGetTeamID(String teamName)
   {
      int teamID = 0;

      if((null != teamName) && (!teamName.equals("")))
      {
         ResultSet resSet = sqlMan.sqlQuery("SELECT teamID FROM tbTeams WHERE teamName = '" + teamName + "';");

         try
         {
            resSet.next();
            teamID = resSet.getInt("teamID");
         }
         catch (SQLException e)
         {
            // may be empty
            //e.printStackTrace();
         }
      }

      return teamID;
   }

   public HashMap<String, String> sqlGetTeamList()
   {
      ResultSet resSet = sqlMan.sqlQuery("SELECT teamName, leader FROM tbTeams ORDER BY teamName COLLATE NOCASE ASC;");
      HashMap<String, String> teamList = new HashMap<String, String>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               teamList.put(resSet.getString("teamName"), resSet.getString("leader"));
            }
         }
      }
      catch (SQLException e)
      {
         // may be empty
         //e.printStackTrace();
      }

      return teamList;
   }

   public ArrayList<String> sqlGetMembersOfTeam(String teamName)
   {         
      ResultSet resSet = sqlMan.sqlQuery("SELECT member FROM tbMemberships WHERE fk_teamID = " + sqlGetTeamID(teamName) + ";");
      ArrayList<String> teamMembers = new ArrayList<String>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one member found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               teamMembers.add(resSet.getString("member"));
            }
         }
      }
      catch (SQLException e)
      {
         // resSet may be empty
         //e.printStackTrace();
      }

      return teamMembers;
   }

   public boolean sqlAddTeam(String teamName, String leader)
   {
      boolean res = true;

      sqlMan.insertQuery("INSERT INTO " + TeamAdvantage.TABLES.tbTeams.name() + " (teamName, leader) VALUES ('" + teamName + "','" + leader + "');");
      ResultSet resSet = sqlMan.sqlQuery("SELECT teamName FROM " + TeamAdvantage.TABLES.tbTeams.name() + " WHERE teamID = " + sqlGetTeamID(teamName));

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            TeamAdvantage.teams.add(new TATeam(teamName, leader, new ArrayList<String>()));
         }
         else
         {
            res = false;
            log.severe(logPrefix + "ERROR on deleting team!");
         }
      }
      catch (SQLException e)
      {
         log.severe(logPrefix + "DB ERROR on creating team!");
      }

      return res;
   }

   public boolean sqlDeleteTeam(TATeam team)
   {
      boolean res = false;

      int teamID = plugin.sqlGetTeamID(team.getName());
      sqlMan.deleteQuery("DELETE FROM " + TeamAdvantage.TABLES.tbMemberships.name() + " WHERE fk_teamID = " + teamID + ";");
      sqlMan.deleteQuery("DELETE FROM " + TeamAdvantage.TABLES.tbTeams.name() + " WHERE teamName = '" + team.getName() + "';");
      ResultSet resSetTeam = sqlMan.sqlQuery("SELECT teamName FROM " + TeamAdvantage.TABLES.tbTeams.name() + " WHERE teamID = " + sqlGetTeamID(team.getName()));
      ResultSet resSetMembers = sqlMan.sqlQuery("SELECT member FROM " + TeamAdvantage.TABLES.tbMemberships.name() + " WHERE fk_teamID = " + sqlGetTeamID(team.getName()));

      try
      {
         if((!resSetTeam.isBeforeFirst()) && (!resSetMembers.isBeforeFirst())) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
            TeamAdvantage.teams.remove(team);
         }
      }
      catch (SQLException e)
      {         
         log.severe(logPrefix + "DB ERROR on deleting team!");
      }

      return res;
   }
}


