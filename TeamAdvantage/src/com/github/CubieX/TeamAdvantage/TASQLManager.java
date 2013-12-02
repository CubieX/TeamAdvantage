package com.github.CubieX.TeamAdvantage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.scoreboard.Team;

import lib.PatPeter.sqlLibrary.SQLite.sqlCore;

import com.github.CubieX.TeamAdvantage.TeamAdvantage.TABLES;

public class TASQLManager
{
   private TeamAdvantage plugin = null;
   private sqlCore sql_Core = null;
   private File pFolder; // Folder to store plugin settings file and database

   public TASQLManager(TeamAdvantage plugin)
   {
      this.plugin = plugin;
      pFolder = new File("plugins" + File.separator + plugin.getDescription().getName());
   }

   public void initializeSQLite()
   {
      // Initializing SQLite
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "SQLite Initializing");}

      // Declare SQLite handler
      sql_Core = new sqlCore(TeamAdvantage.log, "TeamsDB", pFolder.getPath(), plugin);

      // Initialize SQLite handler
      sql_Core.initialize();

      // Check if the table exists, if it doesn't, create it
      if (!sql_Core.checkTable(TABLES.tbTeams.name()))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbTeams...");
         String query = "CREATE TABLE tbTeams (" +
               "teamID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "teamName VARCHAR(32) UNIQUE NOT NULL," +
               "leader VARCHAR(32) NOT NULL)"; // holds the team name and the leader
         // only one leader per team allowed
         sql_Core.createTable(query);
      }

      // Check if the table exists, if it doesn't, create it
      if (!sql_Core.checkTable(TABLES.tbMemberships.name()))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbMemberships...");
         String query = "CREATE TABLE tbMemberships (" +
               "id INTEGER PRIMARY KEY AUTOINCREMENT," +
               "fk_teamID INTEGER NOT NULL," +              
               "member VARCHAR(32) NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID))";
         // membership must reference an existing team
         sql_Core.createTable(query);
      }
   }

   public void loadTeamsFromDB()
   {
      HashMap<String, String> teamList = sqlGetTeamList();

      for(String teamName : teamList.keySet())
      {
         TeamAdvantage.teams.add(new TATeam(plugin, teamName, teamList.get(teamName), sqlGetMembersOfTeam("teamName")));
      }

      TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Successfully loaded " + TeamAdvantage.teams.size() + " teams from DB."); 
   }

   public int sqlGetTeamID(String teamName)
   {
      int teamID = 0;

      if((null != teamName) && (!teamName.equals("")))
      {
         ResultSet resSet = sql_Core.sqlQuery("SELECT teamID FROM tbTeams WHERE teamName = '" + teamName + "';");

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
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName, leader FROM tbTeams ORDER BY teamName COLLATE NOCASE ASC;");
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
      ResultSet resSet = sql_Core.sqlQuery("SELECT member FROM tbMemberships WHERE fk_teamID = " + sqlGetTeamID(teamName) + ";");
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

      sql_Core.insertQuery("INSERT INTO " + TeamAdvantage.TABLES.tbTeams.name() + " (teamName, leader) VALUES ('" + teamName + "','" + leader + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM " + TeamAdvantage.TABLES.tbTeams.name() + " WHERE teamID = " + sqlGetTeamID(teamName));

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            TeamAdvantage.teams.add(new TATeam(plugin, teamName, leader, new ArrayList<String>()));
         }
         else
         {
            res = false;
            TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "ERROR on deleting team!");
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on creating team!");
      }

      return res;
   }
   
   public boolean sqlSetTeamName(String teamName)
   {
      boolean res = true;

      // TODO

      return res;
   }

   public boolean sqlDeleteTeam(TATeam team)
   {
      boolean res = false;

      int teamID = sqlGetTeamID(team.getName());
      sql_Core.deleteQuery("DELETE FROM " + TeamAdvantage.TABLES.tbMemberships.name() + " WHERE fk_teamID = " + teamID + ";");
      sql_Core.deleteQuery("DELETE FROM " + TeamAdvantage.TABLES.tbTeams.name() + " WHERE teamName = '" + team.getName() + "';");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM " + TeamAdvantage.TABLES.tbTeams.name() + " WHERE teamID = " + sqlGetTeamID(team.getName()));
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT member FROM " + TeamAdvantage.TABLES.tbMemberships.name() + " WHERE fk_teamID = " + sqlGetTeamID(team.getName()));

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
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting team!");
      }

      return res;
   }
   
   public boolean sqlSetTeamLeader(String teamName, String teamLeader)
   {
      boolean res = true;

      // TODO

      return res;
   }

   public boolean sqlAddMemberToTeam(String teamName, String newMember)
   {
      boolean res = true;

      // TODO

      return res;
   }

   public boolean sqlRemoveMemberFromTeam(String teamName, String memberToDelete)
   {
      boolean res = false;

      // TODO

      return res;
   }

   public boolean sqlAddInvitationToTeam(String teamName, String invitedPlayer)
   {
      boolean res = true;

      // TODO

      return res;
   }

   public boolean sqlDeleteInvitationFromTeam(String teamName, String invitedMember)
   {
      boolean res = false;

      // TODO

      return res;
   }

   public boolean sqlAddRequestFromPlayerForTeam(String teamName, String requestingPlayer)
   {
      boolean res = true;

      // TODO

      return res;
   }

   public boolean sqlDeleteRequestForTeamFromPlayer(String teamName, String requestingPlayer)
   {
      boolean res = false;

      // TODO

      return res;
   }
}
