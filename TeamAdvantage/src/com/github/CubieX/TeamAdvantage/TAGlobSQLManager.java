package com.github.CubieX.TeamAdvantage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import lib.PatPeter.sqlLibrary.SQLite.sqlCore;

/**
 * <b>Global SQL Manager for all DB handling NOT related to a specific TATeam</b>
 * */
public class TAGlobSQLManager
{
   private TeamAdvantage plugin = null;
   private sqlCore sql_Core = null;
   private TATeamSQLManager teamSQLman = null;
   
   private File pFolder; // Folder to store plugin settings file and database
   
   public TAGlobSQLManager(TeamAdvantage plugin, TATeamSQLManager teamSQLman)
   {
      this.plugin = plugin;
      this.teamSQLman = teamSQLman;
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
               "teamTag VARCHAR(16) UNIQUE NOT NULL," +
               "teamMoney DOUBLE NOT NULL," +
               "teamHomeWorld VARCHAR(32)," +
               "teamHomeX DOUBLE," +
               "teamHomeY DOUBLE," +
               "teamHomeZ DOUBLE," +
               "teamHomePitch DOUBLE," +
               "teamHomeYaw DOUBLE);";
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
    * <b>Returns SQL core instance</b>
    *      
    * @return sql_Core The sqlCore instance
    * */
   public sqlCore getSQLcore()
   {
      return sql_Core;
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
         for(String member : teamSQLman.sqlGetMembersOfTeam(team.getName()))
         {
            team.addMember(member);
            memberCount++;
         }

         // load requests from DB
         for(String requestingPlayer : teamSQLman.sqlGetRequestsOfTeam(team.getName()))
         {
            team.addJoinTeamRequest(requestingPlayer);
            requestCount++;
         }

         // load invitations from DB
         for(String invitedPlayer : teamSQLman.sqlGetInvitationsOfTeam(team.getName()))
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
    * <b>Get a list of all teams</b>    
    *    
    * @return teams A list of all teams
    * */
   public ArrayList<TATeam> sqlGetTeamList()
   {
      ResultSet resSet = sql_Core.sqlQuery("SELECT * FROM tbTeams ORDER BY teamMoney DESC;");
      /*ResultSet resSet = sql_Core.sqlQuery("SELECT teamName, teamLeader, teamMoney FROM tbTeams ORDER BY teamName COLLATE NOCASE ASC;");*/
      ArrayList<TATeam> teams = new ArrayList<TATeam>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            Location home = null;

            while(resSet.next())
            {
               if((null != resSet.getString("teamHomeWorld"))
                     && (null != resSet.getString("teamHomeX"))
                     && (null != resSet.getString("teamHomeY"))
                     && (null != resSet.getString("teamHomeZ")))
               {
                  home = new Location(Bukkit.getWorld(resSet.getString("teamHomeWorld")), resSet.getDouble("teamHomeX"), resSet.getDouble("teamHomeY"), resSet.getDouble("teamHomeZ"));
                  Float pitch = (float)resSet.getDouble("teamHomePitch");
                  Float yaw = (float)resSet.getDouble("teamHomeYaw");
                  home.setPitch(pitch);
                  home.setYaw(yaw);
               }
               
               teams.add(new TATeam(teamSQLman, resSet.getString("teamName"), resSet.getString("teamLeader"), resSet.getString("teamTag"), resSet.getDouble("teamMoney"), home));
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
    * <b>Add a new team</b>    
    *
    * @param teamName The team to delete
    * @param teamLeader The name of the team leader
    * @param teamTag The chat tag of the team 
    * @return res If the creation was successful
    * */
   public boolean sqlAddTeam(String teamName, String teamLeader, String teamTag)
   {
      boolean res = false;

      sql_Core.insertQuery("INSERT INTO tbTeams (teamName, teamLeader, teamTag, teamMoney) VALUES ('" + teamName + "','" + teamLeader + "','" + teamTag + "','" + 0.00 + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamName = '" + teamName + "';");

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            TeamAdvantage.teams.add(new TATeam(teamSQLman, teamName, teamLeader, teamTag , 0.0, null));
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

      int teamID = teamSQLman.sqlGetTeamIDbyTeamName(team.getName());
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
}
