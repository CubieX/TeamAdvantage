package com.github.CubieX.TeamAdvantage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import lib.PatPeter.sqlLibrary.SQLite.sqlCore;

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
      if (!sql_Core.checkTable("tbTeams"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbTeams...");
         String query = "CREATE TABLE tbTeams (" +
               "teamID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "teamName VARCHAR(32) UNIQUE NOT NULL," +
               "teamLeader VARCHAR(32) UNIQUE NOT NULL)";         
         sql_Core.createTable(query);
      }

      // Check if the table exists, if it doesn't, create it
      if (!sql_Core.checkTable("tbMembers"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbMembers...");
         String query = "CREATE TABLE tbMembers (" +
               "memberID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "name VARCHAR(32) UNIQUE NOT NULL," +
               "fk_teamID INTEGER NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID))";
         // member must belong to an existing team
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
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName, teamLeader FROM tbTeams ORDER BY teamName COLLATE NOCASE ASC;");
      HashMap<String, String> teamList = new HashMap<String, String>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               teamList.put(resSet.getString("teamName"), resSet.getString("teamLeader"));
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
      ResultSet resSet = sql_Core.sqlQuery("SELECT member FROM tbMembers WHERE fk_teamID = " + sqlGetTeamID(teamName) + ";");
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

   public boolean sqlAddTeam(String teamName, String teamLeader)
   {
      boolean res = false;

      sql_Core.insertQuery("INSERT INTO tbTeams (teamName, teamLeader) VALUES ('" + teamName + "','" + teamLeader + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + sqlGetTeamID(teamName));

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {            
            TeamAdvantage.teams.add(new TATeam(plugin, teamName, teamLeader, new ArrayList<String>()));
            res = true;
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on creating team!");
      }

      return res;
   }   

   public boolean sqlDeleteTeam(TATeam team)
   {
      boolean res = false;

      int teamID = sqlGetTeamID(team.getName());
      sql_Core.deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + teamID + ";");
      sql_Core.deleteQuery("DELETE FROM tbTeams WHERE teamName = '" + team.getName() + "';");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + sqlGetTeamID(team.getName()));
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT member FROM tbMembers WHERE fk_teamID = " + sqlGetTeamID(team.getName()));

      try
      {
         if((!resSetTeam.isBeforeFirst()) && (!resSetMembers.isBeforeFirst())) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {            
            TeamAdvantage.teams.remove(team);
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting team!");
      }

      return res;
   }

   /**
    * Set the name of the team
    * Do NOT call this directly, but only from TATeam.setName()!
    *
    * @param teamName The current name of the team.
    * @param newTeamName The new name of the team.
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamName(String teamName, String newTeamName)
   {
      boolean res = false;

      sql_Core.updateQuery("UPDATE tbTeam SET teamName='" + newTeamName + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamName='" + newTeamName + "';");

      try
      {
         if(!resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no affected row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team name!");
      }

      return res;
   }

   /**
    * Set the new team leader
    * Do NOT call this directly, but only from TATeam.setLeader()!
    *
    * @param teamName The current name of the team.
    * @param teamLeader The new team leader to set
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamLeader(String teamName, String teamLeader)
   {
      boolean res = false;

      sql_Core.updateQuery("UPDATE tbTeam SET teamLeader='" + teamLeader + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamLeader FROM tbTeams WHERE teamName='" + teamName + "' AND teamLeader='" + teamLeader + "';");

      try
      {
         if(!resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no affected row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team name!");
      }

      return res;
   }

   /**
    * Add a member to a team
    * Do NOT call this directly, but only from TATeam.addMember()!
    *
    * @param teamName The current name of the team.
    * @param newMember The new member to add
    * @return res If the update was successful
    * */
   public boolean sqlAddMemberToTeam(String teamName, String newMember)
   {
      boolean res = false;

      int teamID = sqlGetTeamID(teamName);
      sql_Core.insertQuery("INSERT INTO tbMembers (name, fk_teamID) VALUES ('" + newMember + "','" + teamID + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT name FROM tbMembers WHERE name='" + newMember + "' AND fk_teamID = " + teamID);

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {            
            TATeam team = plugin.getTeamByName(teamName);

            if(team.addMember(newMember))
            {
               res = true;  
            }
         }         
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on creating team!");
      }

      return res;
   }

   /**
    * Remove a member from a team
    * Do NOT call this directly, but only from TATeam.removeMember()!
    *
    * @param teamName The name of the team.
    * @param memberToDelete The member to delete
    * @return res If the update was successful
    * */
   public boolean sqlRemoveMemberFromTeam(String teamName, String memberToDelete)
   {
      boolean res = false;

      // TODO

      return res;
   }

   public boolean sqlAddInvitationToTeam(String teamName, String invitedPlayer)
   {
      boolean res = false;

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
      boolean res = false;

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
