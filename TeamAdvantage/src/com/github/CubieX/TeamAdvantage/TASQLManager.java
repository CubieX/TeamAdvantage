package com.github.CubieX.TeamAdvantage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
               "teamLeader VARCHAR(32) UNIQUE NOT NULL," +
               "teamMoney INTEGER NOT NULL," +
               "teamHomeX INTEGER," +
               "teamHomeY INTEGER," +
               "teamHomeZ INTEGER," +
               "teamHomeWorld VARCHAR(32));";
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
   public void loadTeamsFromDB(Player p)
   {      
      TeamAdvantage.teams.clear();

      ArrayList<TATeam> teamList = sqlGetTeamList();      
      int memberCount = 0;
      int requestCount = 0;
      int invitationCount = 0;

      for(TATeam team : teamList)
      { 
         // load members from DB
         for(String member : sqlGetMembersOfTeam(team.getName()))
         {
            team.addMember(member);
            memberCount++;
         }

         // load requests from DB
         for(String requestingPlayer : sqlGetRequestsOfTeam(team.getName()))
         {
            team.addJoinTeamRequest(requestingPlayer);
            requestCount++;
         }

         // load invitations from DB
         for(String invitedPlayer : sqlGetInvitationsOfTeam(team.getName()))
         {
            team.invitePlayer(invitedPlayer);
            invitationCount++;
         }

         TeamAdvantage.teams.add(team);
      }

      TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Successfully loaded " + TeamAdvantage.teams.size() + " teams from DB.");
      if(null != p){p.sendMessage(TeamAdvantage.logPrefix + "Successfully loaded " + TeamAdvantage.teams.size() + " teams from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + memberCount + " members from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + requestCount + " requests from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + invitationCount + " invitations from DB.");}
   }

   /**
    * <b>Get the unique ID of a team by name</b>
    * 
    * @param teamName The team to get the ID of
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
            // resSet may be empty
            //e.printStackTrace();
         }
      }

      return teamID;
   }

   /**
    * <b>Get a list of all teams</b>    
    *    
    * @return teams A list of all teams
    * */
   public ArrayList<TATeam> sqlGetTeamList()
   {
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName, teamLeader, teamMoney FROM tbTeams ORDER BY teamMoney DESC;");
      /*ResultSet resSet = sql_Core.sqlQuery("SELECT teamName, teamLeader, teamMoney FROM tbTeams ORDER BY teamName COLLATE NOCASE ASC;");*/
      ArrayList<TATeam> teams = new ArrayList<TATeam>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               teams.add(new TATeam(plugin, resSet.getString("teamName"), resSet.getString("teamLeader"), resSet.getDouble("teamMoney")));
            }
         }
      }
      catch (SQLException e)
      {
         // resSet may be empty
         //e.printStackTrace();
      }

      return teams;
   }

   /**
    * <b>Get a list of all members of a team except for the leader directly from DB</b>    
    *
    * @param teamName The team to get the member list from
    * @return teamMembers A list of all team members except for the leader
    * */
   public ArrayList<String> sqlGetMembersOfTeam(String teamName)
   {         
      int teamID = sqlGetTeamIDbyTeamName(teamName);
      ResultSet resSet = sql_Core.sqlQuery("SELECT name FROM tbMembers WHERE fk_teamID = " + teamID + ";");
      ArrayList<String> teamMembers = new ArrayList<String>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one member found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               teamMembers.add(resSet.getString("name"));
            }
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on reading members of team from DB!");         
      }

      return teamMembers;
   }

   /**
    * <b>Get a list of all requests for this team directly from DB</b>    
    *
    * @param teamName The team to get the requests list from
    * @return requests A list of all requests for this team
    * */
   public ArrayList<String> sqlGetRequestsOfTeam(String teamName)
   {         
      int teamID = sqlGetTeamIDbyTeamName(teamName);
      ResultSet resSet = sql_Core.sqlQuery("SELECT playerName FROM tbRequests WHERE fk_teamID = " + teamID + ";");
      ArrayList<String> requests = new ArrayList<String>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one member found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               requests.add(resSet.getString("playerName"));
            }
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on reading requests of team from DB!");         
      }

      return requests;
   }

   /**
    * <b>Get a list of all invitations of a team directly from DB</b>    
    *
    * @param team The team to get the invitations list from
    * @return invitations A list of all invitations of this team
    * */
   public ArrayList<String> sqlGetInvitationsOfTeam(String teamName)
   {         
      int teamID = sqlGetTeamIDbyTeamName(teamName);
      ResultSet resSet = sql_Core.sqlQuery("SELECT playerName FROM tbInvitations WHERE fk_teamID = " + teamID + ";");
      ArrayList<String> invitations = new ArrayList<String>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one member found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {
               invitations.add(resSet.getString("playerName"));
            }
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on reading invitations of team from DB!");         
      }

      return invitations;
   }

   /**
    * <b>Add a new team</b>    
    *
    * @param teamName The team to delete
    * @param teamLeader The name of the team leader   
    * @return res If the creation was successful
    * */
   public boolean sqlAddTeam(String teamName, String teamLeader)
   {
      boolean res = false;

      sql_Core.insertQuery("INSERT INTO tbTeams (teamName, teamLeader, teamMoney) VALUES ('" + teamName + "','" + teamLeader + "','" + 0.00 + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamName = '" + teamName + "';");

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            TeamAdvantage.teams.add(new TATeam(plugin, teamName, teamLeader, 0.0));
            res = true;
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on creating team in DB!");
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
      sql_Core.deleteQuery("DELETE FROM tbRequests WHERE fk_teamID = " + teamID + ";");
      sql_Core.deleteQuery("DELETE FROM tbInvitations WHERE fk_teamID = " + teamID + ";");
      sql_Core.deleteQuery("DELETE FROM tbTeams WHERE teamID = " + teamID + ";");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + teamID + ";");

      try
      {
         if(!resSetTeam.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {            
            TeamAdvantage.teams.remove(team);
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting team from DB!");
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

      sql_Core.updateQuery("UPDATE tbTeams SET teamName='" + newTeamName + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamName='" + newTeamName + "';");

      try
      {
         if(resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no row.
         {            
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team name in DB!");
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

      sql_Core.updateQuery("UPDATE tbTeams SET teamLeader='" + teamLeader + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamLeader FROM tbTeams WHERE teamName='" + teamName + "' AND teamLeader='" + teamLeader + "';");

      try
      {
         if(resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team name in DB!");
      }

      return res;
   }

   /**
    * <b>Set value of team account money</b>
    * Do NOT call this directly, but only from TATeam.setMoney()!
    *
    * @param teamName The team to set the money for
    * @param amount The amount of money to set
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamMoney(String teamName, double amount)
   {
      boolean res = false;

      sql_Core.updateQuery("UPDATE tbTeams SET teamMoney='" + amount + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamMoney FROM tbTeams WHERE teamName='" + teamName + "' AND teamMoney='" + amount + "';");

      try
      {
         if(resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team money value in DB!");
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
            res = true;
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding member to team in DB!");
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
      sql_Core.deleteQuery("DELETE FROM tbTeams WHERE teamID = " + teamID + ";");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + teamID + ";");

      try
      {
         if(!resSetTeam.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on removing member from team in DB!");
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
      ResultSet resSetMembers = sql_Core.sqlQuery("SELECT name FROM tbMembers WHERE fk_teamID = " + teamID + ";");

      try
      {
         if(!resSetMembers.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {            
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on clearing members of team from DB!");
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
            res = true;           
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding invitation of member to team in DB!");
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
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting invitation of member from team in DB!");
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
            res = true;           
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding invitation of member to team in DB!");
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
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting invitation of member from team in DB!");
      }

      return res;
   }

   /**
    * <b>Set or delete home point of team</b><br>
    * Do NOT call this directly, but only from TATeam.setHome() or TATeam.deleteHome()!
    *
    * @param teamName The name of the team to set the home for
    * @param home The location of the new home (or NULL)
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamHome(String teamName, Location home)
   {
      boolean res = false;
      String homeWorld = "NULL";
      String homeX = "NULL";
      String homeY = "NULL";
      String homeZ = "NULL";

      if(null != home)
      {
         homeWorld = home.getWorld().getName();
         homeX = String.valueOf(home.getX());
         homeY = String.valueOf(home.getY());
         homeZ = String.valueOf(home.getZ());
      }

      sql_Core.updateQuery("UPDATE tbTeams SET teamHomeWorld='" + homeWorld + "', teamHomeX = " + homeX + ", teamHomeY = " + homeY + ", teamHomeZ = " + homeZ + " WHERE teamName='" + teamName + "';");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamHomeWorld FROM tbTeams WHERE teamName='" + teamName + "';");

      try
      {
         resSet.next();

         if(null != home)
         {
            if((null != resSet.getString("teamHomeWorld"))
                  && ("" != resSet.getString("teamHomeWorld"))) // home in DB must also be NOT NULL or not empty
            {
               res = true;
            }
         }
         else
         {
            if((null == resSet.getString("teamHomeWorld"))
                  || ("" == resSet.getString("teamHomeWorld"))) // home in DB must also be NULL or empty
            {
               res = true;
            }
         }

      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding invitation of member to team in DB!");
      }

      return res;
   }   
}
