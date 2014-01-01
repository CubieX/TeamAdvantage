package com.github.CubieX.TeamAdvantage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Location;

/**
 * <b>Team SQL Manager for all DB handling related to a specific TATeam</b>
 * */
public class TATeamSQLManager
{
   private TeamAdvantage plugin = null;

   public TATeamSQLManager(TeamAdvantage plugin)
   {
      this.plugin = plugin;
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
         ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamID FROM tbTeams WHERE teamName = '" + teamName + "';");

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
    * <b>Get a list of all members of a team except for the leader directly from DB</b>    
    *
    * @param teamName The team to get the member list from
    * @return teamMembers A list of all team members except for the leader
    * */
   public ArrayList<String> sqlGetMembersOfTeam(String teamName)
   {         
      int teamID = sqlGetTeamIDbyTeamName(teamName);
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE fk_teamID = " + teamID + ";");
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
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbRequests WHERE fk_teamID = " + teamID + ";");
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
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbInvitations WHERE fk_teamID = " + teamID + ";");
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

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamName='" + newTeamName + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamName FROM tbTeams WHERE teamName='" + newTeamName + "';");

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

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamLeader='" + teamLeader + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamLeader FROM tbTeams WHERE teamName='" + teamName + "' AND teamLeader='" + teamLeader + "';");

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

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamMoney='" + amount + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamMoney FROM tbTeams WHERE teamName='" + teamName + "' AND teamMoney='" + amount + "';");

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
    * <b>Set the next team fee due date time stamp for the team</b><br>
    * Do NOT call this directly, but only from TATeam.scheduleNextTeamFeeDueDate()!
    *
    * @param teamName The name of the team to schedule the nextdue date
    * @param teamFeeDueDateTimestamp The new name of the team
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamFeeDueDate(String teamName, long teamFeeDueDateTimestamp)
   {
      boolean res = false;

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamNextFeeDueDate=" + teamFeeDueDateTimestamp + " WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamNextFeeDueDate FROM tbTeams WHERE teamName='" + teamName + "' AND teamNextFeeDueDate=" + teamFeeDueDateTimestamp + ";");

      try
      {
         if(resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no row.
         {            
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team fee due date timestamp in DB!");
      }

      return res;
   }

   /**
    * <b>Set the bonus effects status of the team</b><br>
    * Do NOT call this directly, but only from TATeam.setTeamBonusEffectsStatus()!
    *
    * @param teamName The current name of the team
    * @param newStatus The new status of the bonus effects
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamBonusEffectsStatus(String teamName, int newStatus)
   {
      boolean res = false;

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamBonusEffectsStatus='" + newStatus + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamBonusEffectsStatus FROM tbTeams WHERE teamName='" + teamName +
            "' AND teamBonusEffectsStatus=" + newStatus + ";");

      try
      {
         if(resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no row.
         {            
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team bonus effects status in DB!");
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
      plugin.getSQLcore().insertQuery("INSERT INTO tbMembers (name, fk_teamID) VALUES ('" + newMember + "','" + teamID + "');");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE name='" + newMember + "' AND fk_teamID = " + teamID + ";");

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
      plugin.getSQLcore().deleteQuery("DELETE FROM tbMembers WHERE name = '" + memberToDelete + "' AND fk_teamID = " + teamID + ";");      
      ResultSet resSetTeam = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE name = '" + memberToDelete + "' AND fk_teamID = " + teamID + ";");      

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
      plugin.getSQLcore().deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + teamID + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE fk_teamID = " + teamID + ";");

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
      plugin.getSQLcore().insertQuery("INSERT INTO tbInvitations (playerName, fk_teamID) VALUES ('" + invitedPlayer + "','" + teamID + "');");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbInvitations WHERE playerName='" + invitedPlayer +
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
      plugin.getSQLcore().deleteQuery("DELETE FROM tbInvitations WHERE playerName = '" + invitedPlayer + "' AND fk_teamID = " + teamID + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbInvitations WHERE playerName = '" + invitedPlayer +
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
      plugin.getSQLcore().insertQuery("INSERT INTO tbRequests (playerName, fk_teamID) VALUES ('" + requestingPlayer + "','" + teamID + "');");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbRequests WHERE playerName='" + requestingPlayer +
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
      plugin.getSQLcore().deleteQuery("DELETE FROM tbRequests WHERE playerName = '" + requestingPlayer + "' AND fk_teamID = " + teamID + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbRequests WHERE playerName = '" + requestingPlayer +
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
      String query = "";

      if(null != home)
      {
         query = "UPDATE tbTeams SET teamHomeWorld='" + home.getWorld().getName() + "', teamHomeX = " + home.getX() + ", teamHomeY = " + home.getY() + ", teamHomeZ = " + home.getZ() + ", teamHomePitch = " + home.getPitch() + ", teamHomeYaw = " + home.getYaw() + " WHERE teamName='" + teamName + "';";
      }
      else
      {      
         query = "UPDATE tbTeams SET teamHomeWorld = null, teamHomeX = null, teamHomeY = null, teamHomeZ = null, teamHomePitch = null, teamHomeYaw = null WHERE teamName='" + teamName + "';";
      }

      plugin.getSQLcore().updateQuery(query);
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamHomeWorld FROM tbTeams WHERE teamName='" + teamName + "';");

      try
      {
         resSet.next();

         if(null != home)
         {
            if(null != resSet.getString("teamHomeWorld")) // home in DB must also be NOT NULL
            {
               res = true;
            }
         }
         else
         {
            if(null == resSet.getString("teamHomeWorld")) // home in DB must also be NULL
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

   /**
    * <b>Set the team chat tag</b><br>
    * Do NOT call this directly, but only from TATeam.setTag()!
    *
    * @param teamName The name of the team to set the home for
    * @param tag The tag to set
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamTag(String teamName, String tag)
   {
      boolean res = false;

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamTag='" + tag + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamTag FROM tbTeams WHERE teamName='" + teamName + "';");

      try
      {
         resSet.next();

         if(resSet.getString("teamTag").equals(tag))
         {
            res = true;
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team tag in DB!");
      }

      return res;
   }
}
