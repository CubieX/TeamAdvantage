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

   /**
    * <b>Initializes the database and runs checks on existing DB</b>
    *
    * */
   public void initializeSQLite()
   {
      // Initializing SQLite
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "SQLite Initializing");}

      // Declare SQLite handler
      sql_Core = new sqlCore(TeamAdvantage.log, "TeamsDB", pFolder.getPath(), plugin);

      // Initialize SQLite handler
      sql_Core.initialize();

      // Check if all tables exist. If one does not, create it
      if (!sql_Core.checkTable("tbTeams"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbTeams...");
         String query = "CREATE TABLE tbTeams (" +
               "teamID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "teamName VARCHAR(32) UNIQUE NOT NULL," +
               "teamLeader VARCHAR(32) UNIQUE NOT NULL);";         
         sql_Core.createTable(query);
      }
      
      if (!sql_Core.checkTable("tbMembers"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbMembers...");
         String query = "CREATE TABLE tbMembers (" +
               "memberID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "name VARCHAR(32) UNIQUE NOT NULL," +
               "fk_teamID INTEGER NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID));";         
         sql_Core.createTable(query);
      }
      
      if (!sql_Core.checkTable("tbRequests"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbRequests...");
         String query = "CREATE TABLE tbRequests (" +
               "requestID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "playerName VARCHAR(32) UNIQUE NOT NULL," +
               "fk_teamID INTEGER NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID));";         
         sql_Core.createTable(query);
      }
      
      if (!sql_Core.checkTable("tbInvitations"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbInvitations...");
         String query = "CREATE TABLE tbInvitations (" +
               "invitationID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "playerName VARCHAR(32) UNIQUE NOT NULL," +
               "fk_teamID INTEGER NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID));";         
         sql_Core.createTable(query);
      }
   }

   /**
    * <b>Loads all teams from DB to the teams HashMap</b>
    *      
    * */
   public void loadTeamsFromDB()
   {
      HashMap<String, String> teamList = sqlGetTeamList();

      for(String teamName : teamList.keySet())
      {
         TeamAdvantage.teams.add(new TATeam(plugin, teamName, teamList.get(teamName), sqlGetMembersOfTeam("teamName")));
      }

      TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Successfully loaded " + TeamAdvantage.teams.size() + " teams from DB."); 
   }

   /**
    * <b>Get the unique ID of a team by name</b>
    *    
    * @return teamID The unique ID of a team in database
    * */
   public int sqlGetTeamIDbyTeamName(String teamName)
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

   /**
    * <b>Get a list of all teams</b>    
    *    
    * @return teamList A list of all teams
    * */
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

   /**
    * <b>Get a list of all members of a team except for the leader</b>    
    *
    * @param team The team to get the member list from
    * @return teamMembers A list of all team members except for the leader
    * */
   public ArrayList<String> sqlGetMembersOfTeam(String teamName)
   {         
      int teamID = sqlGetTeamIDbyTeamName(teamName);
      ResultSet resSet = sql_Core.sqlQuery("SELECT member FROM tbMembers WHERE fk_teamID = " + teamID + ";");
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

   /**
    * <b>Add a new team</b>    
    *
    * @param team The team to delete
    * @param teamLeader The name of the team leaser   
    * @return res If the creation was successful
    * */
   public boolean sqlAddTeam(String teamName, String teamLeader)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.insertQuery("INSERT INTO tbTeams (teamName, teamLeader) VALUES ('" + teamName + "','" + teamLeader + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + teamID + ";");

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

   /**
    * <b>Delete a team</b>    
    *
    * @param team The team to delete    
    * @return res If the deletion was successful
    * */
   public boolean sqlDeleteTeam(TATeam team)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(team.getName());
      sql_Core.deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + teamID + ";");
      sql_Core.deleteQuery("DELETE FROM tbTeams WHERE teamName = '" + team.getName() + "';");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + teamID + ";");
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT member FROM tbMembers WHERE fk_teamID = " + teamID + ";");

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
    * <b>Set the name of the team</b><br>
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
    * <b>Set the new team leader</b><br>
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
    * <b>Add a member to a team</b><br>
    * Do NOT call this directly, but only from TATeam.addMember()!
    *
    * @param teamName The current name of the team.
    * @param newMember The new member to add
    * @return res If the update was successful
    * */
   public boolean sqlAddMemberToTeam(String teamName, String newMember)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.insertQuery("INSERT INTO tbMembers (name, fk_teamID) VALUES ('" + newMember + "','" + teamID + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT name FROM tbMembers WHERE name='" + newMember + "' AND fk_teamID = " + teamID + ";");

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
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding member to team!");
      }

      return res;
   }

   /**
    * <b>Remove a member from a team</b><br>
    * Do NOT call this directly, but only from TATeam.removeMember()!
    *
    * @param teamName The name of the team.
    * @param memberToDelete The member to delete
    * @return res If the update was successful
    * */
   public boolean sqlRemoveMemberFromTeam(String teamName, String memberToDelete)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + teamID + ";");
      sql_Core.deleteQuery("DELETE FROM tbTeams WHERE teamName = '" + teamName + "';");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + teamID + ";");
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT member FROM tbMembers WHERE fk_teamID = " + teamID + ";");

      try
      {
         if((!resSetTeam.isBeforeFirst()) && (!resSetMembers.isBeforeFirst())) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on removing member from team!");
      }

      return res;
   }

   /**
    * <b>Clear all members of a team except for the leader</b><br>
    * Do NOT call this directly, but only from TATeam.clearMembers()!
    *
    * @param teamName The name of the team.    
    * @return res If the update was successful
    * */
   public boolean sqlClearMembers(String teamName)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + teamID + ";");     
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT member FROM tbMembers WHERE fk_teamID = " + teamID + ";");

      try
      {
         if(!resSetMembers.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {            
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on clearing members of team!");
      }

      return res;
   }

   /**
    * <b>Add invitation into a team</b><br>
    * Do NOT call this directly, but only from TATeam.invitePlayer()!
    *
    * @param teamName The name of the team.
    * @param invitedPlayer The player to invite
    * @return res If the update was successful
    * */
   public boolean sqlAddInvitationForPlayerToTeam(String teamName, String invitedPlayer)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.insertQuery("INSERT INTO tbInvitations (playerName, fk_teamID) VALUES ('" + invitedPlayer + "','" + teamID + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT playerName FROM tbInvitations WHERE playerName='" + invitedPlayer +
                                           "' AND fk_teamID = " + teamID + ";");

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {            
            TATeam team = plugin.getTeamByName(teamName);

            if(team.addMember(invitedPlayer))
            {
               res = true;
            }
         }         
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding invitation of member to team!");
      }

      return res;
   }

   /**
    * <b>Delete existing invitation into a team</b><br>
    * Do NOT call this directly, but only from TATeam.uninvitePlayer()!
    *
    * @param teamName The name of the team.
    * @param invitedPlayer The player to uninvite
    * @return res If the update was successful
    * */
   public boolean sqlDeleteInvitationForPlayerFromTeam(String teamName, String invitedPlayer)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.deleteQuery("DELETE FROM tbInvitations WHERE playerName = '" + invitedPlayer + "' AND fk_teamID = " + teamID + ";");     
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT playerName FROM tbInvitations WHERE playerName = '" + invitedPlayer +
                                                   "' AND fk_teamID = " + teamID + ";");

      try
      {
         if(!resSetMembers.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting invitation of member from team!");
      }

      return res;
   }

   /**
    * <b>Add request for joining a team</b><br>
    * Do NOT call this directly, but only from TATeam.addJoinTeamRequest()!
    *
    * @param teamName The name of the team.
    * @param requestingPlayer The player that requested to join
    * @return res If the update was successful
    * */
   public boolean sqlAddRequestFromPlayerToTeam(String teamName, String requestingPlayer)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.insertQuery("INSERT INTO tbRequests (playerName, fk_teamID) VALUES ('" + requestingPlayer + "','" + teamID + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT playerName FROM tbRequests WHERE playerName='" + requestingPlayer +
                                           "' AND fk_teamID = " + teamID);

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {            
            TATeam team = plugin.getTeamByName(teamName);

            if(team.addMember(requestingPlayer))
            {
               res = true;
            }
         }         
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding invitation of member to team!");
      }

      return res;
   }

   /**
    * <b>Delete existing request for joining a team</b><br>
    * Do NOT call this directly, but only from TATeam.deleteJoinTeamRequest()!
    *
    * @param teamName The name of the team.
    * @param requestingPlayer The player to delete his request
    * @return res If the update was successful
    * */
   public boolean sqlDeleteRequestForTeamFromPlayer(String teamName, String requestingPlayer)
   {
      boolean res = false;

      int teamID = sqlGetTeamIDbyTeamName(teamName);
      sql_Core.deleteQuery("DELETE FROM tbRequests WHERE playerName = '" + requestingPlayer + "' AND fk_teamID = " + teamID + ";");     
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT playerName FROM tbRequests WHERE playerName = '" + requestingPlayer +
                                                  "' AND fk_teamID = " + teamID + ";");

      try
      {
         if(!resSetMembers.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting invitation of member from team!");
      }

      return res;
   }
}
