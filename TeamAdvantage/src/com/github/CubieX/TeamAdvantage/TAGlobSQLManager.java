package com.github.CubieX.TeamAdvantage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.CubieX.TeamAdvantage.TATeam.Status;

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

   // TODO make all SQL queries async (may need to omit SELECT checks after queries...)

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
               "teamXP INTEGER NOT NULL," +
               "teamHomeWorld VARCHAR(32)," +
               "teamHomeX DOUBLE," +
               "teamHomeY DOUBLE," +
               "teamHomeZ DOUBLE," +
               "teamHomePitch DOUBLE," +
               "teamHomeYaw DOUBLE," +
               "teamNextFeeDueDate INTEGER NOT NULL," +
               "teamBonusEffectsStatus INTEGER NOT NULL);";
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
               "playerName VARCHAR(32) NOT NULL," +
               "fk_teamID INTEGER NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID));";
         sql_Core.createTable(query);
      }

      if (!sql_Core.checkTable("tbInvitations"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbInvitations...");
         String query = "CREATE TABLE tbInvitations (" +
               "invitationID INTEGER PRIMARY KEY AUTOINCREMENT," +               
               "fk_teamID INTEGER NOT NULL," +
               "playerName VARCHAR(32) NOT NULL," +
               "FOREIGN KEY(fk_teamID) REFERENCES tbTeams(teamID));";
         sql_Core.createTable(query);
      }

      if (!sql_Core.checkTable("tbDiplomacy"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbDiplomacy...");
         String query = "CREATE TABLE tbDiplomacy (" +
               "diplomacyID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "fk_teamID1 INTEGER NOT NULL," +
               "fk_teamID2 INTEGER NOT NULL," +
               "status INTEGER NOT NULL," +
               "UNIQUE (fk_TeamID1, fk_teamID2) ON CONFLICT ROLLBACK," +               
               "FOREIGN KEY(fk_teamID1) REFERENCES tbTeams(teamID)," +
               "FOREIGN KEY(fk_teamID2) REFERENCES tbTeams(teamID))";
         sql_Core.createTable(query);
      }

      if (!sql_Core.checkTable("tbDiplomacyRequests"))
      {
         TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Creating table tbDiplomacyRequests...");
         String query = "CREATE TABLE tbDiplomacyRequests (" +
               "dRequestID INTEGER PRIMARY KEY AUTOINCREMENT," +
               "fk_teamID_receivingTeam INTEGER NOT NULL," +
               "fk_teamID_sendingTeam INTEGER NOT NULL," +
               "status INTEGER NOT NULL," +
               "UNIQUE (fk_teamID_receivingTeam, fk_teamID_sendingTeam) ON CONFLICT ROLLBACK," +
               "FOREIGN KEY(fk_teamID_receivingTeam) REFERENCES tbTeams(teamID)," +
               "FOREIGN KEY(fk_teamID_sendingTeam) REFERENCES tbTeams(teamID));";
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

      ArrayList<TATeam> teamList_BaseData = sqlReadTeamList_BaseData();
      int memberCount = 0;
      int requestCount = 0;
      int invitationCount = 0;
      int diplomacyStates = 0; // will only show ALLIED and HOSTILE states. NEUTRAL ones are not stored at all.
      int diplomacyStateRequests = 0; // will only show ALLIED and HOSTILE state change requests. NEUTRAL ones are not stored at all.

      // load additional data of all teams from DB
      for(TATeam team : teamList_BaseData)
      {
         // load members from DB
         for(String member : teamSQLman.sqlGetMembersOfTeam(team))
         {
            team.getMembers().add(member);
            memberCount++;
         }

         // load requests from DB
         for(String requestingPlayer : teamSQLman.sqlGetRequestsOfTeam(team))
         {
            team.getRequests().add(requestingPlayer);
            requestCount++;
         }

         // load invitations from DB
         for(String invitedPlayer : teamSQLman.sqlGetInvitationsOfTeam(team))
         {
            team.getInvitations().add(invitedPlayer);
            invitationCount++;
         }

         
         
         // load diplomacy states from DB
         HashMap<String, Status> dState = teamSQLman.sqlGetDiplomacyStatesOfTeam(team);

         for(String otherTeam : dState.keySet())
         {
            if(!otherTeam.equals(team.getName()))
            {
               if(dState.get(otherTeam) == Status.ALLIED)
               {
                  team.getAllies().add(otherTeam);
               }
               else if(dState.get(otherTeam) == Status.HOSTILE)
               {
                  team.getEnemies().add(otherTeam);
               }

               diplomacyStates++;
            }
         }
         
         // load diplomacy state requests from DB
         HashMap<String, Status> dStateReq = teamSQLman.sqlGetReceivedDiplomacyRequestsOfTeam(team);

         for(String otherTeam : dStateReq.keySet())
         {
            if(!otherTeam.equals(team.getName()))
            {
               team.getReceivedDiplomacyRequests().put(otherTeam, dStateReq.get(otherTeam));              
               diplomacyStateRequests++;
            }
         }

         TeamAdvantage.teams.add(team);
      }

      TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + TeamAdvantage.teams.size() + " teams from DB.");
      if(null != p){p.sendMessage(TeamAdvantage.logPrefix + "Loaded " + TeamAdvantage.teams.size() + " teams from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + memberCount + " members from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + requestCount + " requests from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + invitationCount + " invitations from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + diplomacyStates + " diplomacy states from DB.");}
      if(TeamAdvantage.debug){TeamAdvantage.log.info(TeamAdvantage.logPrefix + "Loaded " + diplomacyStateRequests + " diplomacy requests from DB.");}
   }

   /**
    * <b>Read the base data of all teams from DB and return those teams</b>    
    *    
    * @return teams A list of all teams (base data only)
    * */
   public ArrayList<TATeam> sqlReadTeamList_BaseData()
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
                  home = new Location(Bukkit.getWorld(resSet.getString("teamHomeWorld")), resSet.getDouble("teamHomeX"), resSet.getDouble("teamHomeY"),
                        resSet.getDouble("teamHomeZ"), (float)resSet.getDouble("teamHomeYaw"), (float)resSet.getDouble("teamHomePitch"));
               }

               TATeam team = new TATeam(plugin, teamSQLman, resSet.getInt("teamID"), resSet.getString("teamName"), resSet.getString("teamLeader"), resSet.getString("teamTag"), resSet.getDouble("teamMoney"), resSet.getInt("teamXP"), home, resSet.getLong("teamNextFeeDueDate") ,resSet.getInt("teamBonusEffectsStatus"));
               teams.add(team);
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
      long nextDueDateTimestamp = System.currentTimeMillis() + (TeamAdvantage.teamFeeCycle * 24 * 3600 * 1000);

      sql_Core.insertQuery("INSERT INTO tbTeams (teamName, teamLeader, teamTag, teamMoney, teamXP, teamNextFeeDueDate, teamBonusEffectsStatus) VALUES ('" + teamName + "','" + teamLeader + "','" + teamTag + "','" + 0.00 + "','" + 0 + "','" + nextDueDateTimestamp +"','" + 1 + "');");
      ResultSet resSet = sql_Core.sqlQuery("SELECT teamID FROM tbTeams WHERE teamName = '" + teamName + "';");

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {            
            TeamAdvantage.teams.add(new TATeam(plugin, teamSQLman, resSet.getInt("teamID"), teamName, teamLeader, teamTag , 0.0, 0, null, nextDueDateTimestamp, 1));
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

      // FIXME als transaction mit rollback implementieren!      
      sql_Core.deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + team.getTeamID() + ";");
      sql_Core.deleteQuery("DELETE FROM tbRequests WHERE fk_teamID = " + team.getTeamID() + ";");
      sql_Core.deleteQuery("DELETE FROM tbInvitations WHERE fk_teamID = " + team.getTeamID() + ";");
      sql_Core.deleteQuery("DELETE FROM tbDiplomacyRequests WHERE fk_teamID_receivingTeam = " + team.getTeamID() + " OR fk_teamID_sendingTeam = " + team.getTeamID() + ";");
      sql_Core.deleteQuery("DELETE FROM tbDiplomacy WHERE fk_teamID1 = " + team.getTeamID() + " OR fk_teamID2 = " + team.getTeamID() + ";");      
      sql_Core.deleteQuery("DELETE FROM tbTeams WHERE teamID = " + team.getTeamID() + ";");
      ResultSet resSetTeam = sql_Core.sqlQuery("SELECT teamName FROM tbTeams WHERE teamID = " + team.getTeamID() + ";");

      try
      {
         if(!resSetTeam.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            // delete all diplomacy states and requests involving this team from other teams data sets
            for(TATeam cTeam : TeamAdvantage.teams)
            {
               if(!cTeam.getName().equals(team.getName()))
               {
                  cTeam.getAllies().remove(team.getName());               
                  cTeam.getEnemies().remove(team.getName());
                  cTeam.getReceivedDiplomacyRequests().remove(team.getName());                  
               }
            }

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
