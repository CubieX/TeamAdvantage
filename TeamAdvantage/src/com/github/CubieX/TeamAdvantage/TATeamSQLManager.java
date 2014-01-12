package com.github.CubieX.TeamAdvantage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import com.github.CubieX.TeamAdvantage.TATeam.Status;

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

   // TODO make all SQL queries async (may need to omit SELECT checks after queries...)

   /**
    * <b>Get a list of all members of a team except for the leader directly from DB</b>    
    *
    * @param team The team to get the member list from
    * @return teamMembers A list of all team members except for the leader
    * */
   public ArrayList<String> sqlGetMembersOfTeam(TATeam team)
   {
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE fk_teamID = " + team.getTeamID() + ";");
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
    * @param team The team to get the requests list from
    * @return requests A list of all requests for this team
    * */
   public ArrayList<String> sqlGetRequestsOfTeam(TATeam team)
   {      
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbRequests WHERE fk_teamID = " + team.getTeamID() + ";");
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
   public ArrayList<String> sqlGetInvitationsOfTeam(TATeam team)
   {
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbInvitations WHERE fk_teamID = " + team.getTeamID() + ";");
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
    * <b>Set value of team XP</b>
    * Do NOT call this directly, but only from TATeam.setXP()!
    *
    * @param teamName The team to set the XP for
    * @param xp The amount of XP to set
    * @return res If the update was successful
    * */
   public boolean sqlSetTeamXP(String teamName, int xp)
   {
      boolean res = false;

      plugin.getSQLcore().updateQuery("UPDATE tbTeams SET teamXP='" + xp + "' WHERE teamName='" + teamName + "';");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT teamXP FROM tbTeams WHERE teamName='" + teamName + "' AND teamXP='" + xp + "';");

      try
      {
         if(resSet.isBeforeFirst()) // Check if update was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting new team XP value in DB!");
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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().insertQuery("INSERT INTO tbMembers (name, fk_teamID) VALUES ('" + newMember + "','" + team.getTeamID() + "');");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE name='" + newMember + "' AND fk_teamID = " + team.getTeamID() + ";");

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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().deleteQuery("DELETE FROM tbMembers WHERE name = '" + memberToDelete + "' AND fk_teamID = " + team.getTeamID() + ";");      
      ResultSet resSetTeam = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE name = '" + memberToDelete + "' AND fk_teamID = " + team.getTeamID() + ";");      

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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().deleteQuery("DELETE FROM tbMembers WHERE fk_teamID = " + team.getTeamID() + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT name FROM tbMembers WHERE fk_teamID = " + team.getTeamID() + ";");

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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().insertQuery("INSERT INTO tbInvitations (playerName, fk_teamID) VALUES ('" + invitedPlayer + "','" + team.getTeamID() + "');");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbInvitations WHERE playerName='" + invitedPlayer +
            "' AND fk_teamID = " + team.getTeamID() + ";");

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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().deleteQuery("DELETE FROM tbInvitations WHERE playerName = '" + invitedPlayer + "' AND fk_teamID = " + team.getTeamID() + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbInvitations WHERE playerName = '" + invitedPlayer +
            "' AND fk_teamID = " + team.getTeamID() + ";");

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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().insertQuery("INSERT INTO tbRequests (playerName, fk_teamID) VALUES ('" + requestingPlayer + "','" + team.getTeamID() + "');");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbRequests WHERE playerName='" + requestingPlayer +
            "' AND fk_teamID = " + team.getTeamID());

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

      TATeam team = plugin.getTeamByName(teamName);
      plugin.getSQLcore().deleteQuery("DELETE FROM tbRequests WHERE playerName = '" + requestingPlayer + "' AND fk_teamID = " + team.getTeamID() + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT playerName FROM tbRequests WHERE playerName = '" + requestingPlayer +
            "' AND fk_teamID = " + team.getTeamID() + ";");

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

   /**
    * <b>Set the diplomacy status of a team to another team</b><br>
    * Do NOT call this directly, but only from TATeam.setDiplomacyStatus()!
    *
    * @param teamName The name of the 1st team to set the diplomacy status for
    * @param otherTeamName The name of the 2nd team to set the diplomacy status for
    * @param newStatus The status to set (0=ALLIED, 1=NEUTRAL (delete DB entry in this case!), 2=HOSTILE)
    * @return res If the update was successful
    * */
   public boolean sqlSetDiplomacyStatus(String teamName, String otherTeamName, Status newStatus)
   {
      boolean res = false;

      if(newStatus != Status.NONE)
      {
         TATeam team = plugin.getTeamByName(teamName);
         TATeam otherTeam = plugin.getTeamByName(otherTeamName);

         if((null != team) && (null != otherTeam))
         {            
            ResultSet resSet = null;
            
            switch (newStatus)
            {
            case ALLIED:
            case HOSTILE:
               if((team.getAllies().contains(otherTeamName)) || (team.getEnemies().contains(otherTeamName))) // get team ID in left column as base
               {
                  plugin.getSQLcore().updateQuery("UPDATE tbDiplomacy SET status=" + Status.getValueOfStatus(newStatus) + " WHERE teamID1=" + team.getTeamID() + " AND teamID2=" + otherTeam.getTeamID() + ";");
                  resSet = plugin.getSQLcore().sqlQuery("SELECT fK_TeamID1 FROM tbDiplomacy WHEREfK_TeamID1 = " + team.getTeamID() + " AND fK_TeamID2 = " + otherTeam.getTeamID() + " AND status = " + newStatus + ";");
               }
               else if((otherTeam.getAllies().contains(teamName)) || (otherTeam.getEnemies().contains(teamName))) // get team ID in right column as base
               {
                  plugin.getSQLcore().updateQuery("UPDATE tbDiplomacy SET status=" + Status.getValueOfStatus(newStatus) + " WHERE teamID1=" + otherTeam.getTeamID() + " AND teamID2=" + team.getTeamID() + ";");
                  resSet = plugin.getSQLcore().sqlQuery("SELECT fK_TeamID1 FROM tbDiplomacy WHEREfK_TeamID1 = " + otherTeam.getTeamID() + " AND fK_TeamID2 = " + team.getTeamID() + " AND status = " + newStatus + ";");
               }
               else
               {
                  // teams are currently NEUTRAL, so insert new entry
                  plugin.getSQLcore().insertQuery("INSERT INTO tbDiplomacy (fK_TeamID1, fk_teamID2, status) VALUES ('" + team.getTeamID() + "','" + otherTeam.getTeamID() + "','" + Status.getValueOfStatus(newStatus) + "');");
               }
               
               try
               {
                  resSet.next();

                  if(Status.getStatusByValue(resSet.getInt("status")) == newStatus)
                  {
                     res = true;
                  }
               }
               catch (SQLException e)
               {
                  TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting team diplomacy status ALLIED or HOSTILE in DB!");
               }

               break;
            case NEUTRAL:
               if((team.getAllies().contains(otherTeamName)) || (team.getEnemies().contains(otherTeamName))) // get team ID in left column as base
               {
                  plugin.getSQLcore().deleteQuery("DELETE FROM tbDiplomacy WHERE fK_TeamID1 = " + team.getTeamID() + " AND fK_TeamID2 = " + otherTeam.getTeamID() + ";");
                  resSet = plugin.getSQLcore().sqlQuery("SELECT fK_TeamID1 FROM tbDiplomacy WHEREfK_TeamID1 = " + team.getTeamID() + " AND fK_TeamID2 = " + otherTeam.getTeamID() + " AND status = " + newStatus + ";");
               }
               else if((otherTeam.getAllies().contains(teamName)) || (otherTeam.getEnemies().contains(teamName))) // get team ID in right column as base
               {
                  plugin.getSQLcore().deleteQuery("DELETE FROM tbDiplomacy WHERE fK_TeamID1 = " + otherTeam.getTeamID() + " AND fK_TeamID2 = " + team.getTeamID() + ";");
                  resSet = plugin.getSQLcore().sqlQuery("SELECT fK_TeamID1 FROM tbDiplomacy WHEREfK_TeamID1 = " + otherTeam.getTeamID() + " AND fK_TeamID2 = " + team.getTeamID() + " AND status = " + newStatus + ";");
               }
               else
               {
                  // nothing to do
               }
               
               try
               {
                  if(!resSet.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
                  {                  
                     res = true;
                  }
               }
               catch (SQLException e)
               {
                  TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on setting team diplomacy status NEUTRAL in DB by deleting entry!");
               }
                               
               break;
            default:
               // should never happen
               TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "ERROR on setting team diplomacy status: Invalid status!");
            }                    
         }
      }

      return res;
   }

   /**
    * <b>Get a list of all diplomacy states of a team directly from DB</b>    
    *
    * @param team The team to get the diplomacy states list from
    * @return diplomacyStates A list of all diplomacy states of this team
    * */
   public HashMap<String, Status> sqlGetDiplomacyStatesOfTeam(TATeam team)
   {
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT fK_TeamID1, fk_TeamID2, status FROM tbDiplomacy WHERE fk_teamID1 = " + team.getTeamID() + " OR fk_teamID2 = " + team.getTeamID() + ";");
      HashMap<String, Status> diplomacyStates = new HashMap<String, Status>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one entry found. isBeforeFirst() will return true if the cursor is before an existing row.
         {                  
            while(resSet.next())
            {  
               if(team.getTeamID() == resSet.getInt("fk_TeamID1")) // get correct column (the other team)
               {
                  String otherTeamsName = plugin.getTeamNameByTeamID(resSet.getInt("fk_teamID2"));

                  if(!otherTeamsName.equals(""))
                  {
                     diplomacyStates.put(otherTeamsName, Status.getStatusByValue(resSet.getInt("status")));
                  }
               }
               else
               {
                  String otherTeamsName = plugin.getTeamNameByTeamID(resSet.getInt("fk_teamID1"));

                  if(!otherTeamsName.equals(""))
                  {
                     diplomacyStates.put(otherTeamsName, Status.getStatusByValue(resSet.getInt("status")));
                  }
               }
            }
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on reading diplomacy states of team from DB!");         
      }

      return (diplomacyStates);
   }

   /**
    * <b>Add received diplomacy request for a team</b><br>
    * Do NOT call this directly, but only from TATeam.sqlAddReceivedDiplomacyRequest()!
    *
    * @param teamName The name of the team.
    * @param requestingPlayer The player that requested to join
    * @param newStatus Requested diplomacy status
    * @return res If the update was successful
    * */
   public boolean sqlAddReceivedDiplomacyRequest(String teamName, String requestingTeamName, Status newStatus)
   {
      boolean res = false;

      TATeam requestingTeam = plugin.getTeamByName(requestingTeamName);
      TATeam targetTeam = plugin.getTeamByName(teamName);
      plugin.getSQLcore().insertQuery("INSERT INTO tbDiplomacyRequests (fk_teamID_receivingTeam, fk_teamID_sendingTeam, status) VALUES ('" + targetTeam.getTeamID() +
            "','" + requestingTeam.getTeamID() + "','" + Status.getValueOfStatus(newStatus) + ");");
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT fk_teamID_receivingTeam FROM tbDiplomacyRequests WHERE fk_teamID_receivingTeam = " +
            targetTeam.getTeamID() + " AND fk_teamID_sendingTeam = " + requestingTeam.getTeamID() + ";");

      try
      {
         if(resSet.isBeforeFirst()) // check if there is a team found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on adding diplomacy request to DB!");
      }

      return res;
   }

   /**
    * <b>Delete a RECEIVED diplomacy request</b><br>
    * Do NOT call this directly, but only from TATeam.deleteDiplomacyRequest()!
    *
    * @param receivingTeamName The receiving team to delete the request
    * @param sendingTeamName The name of the sending team
    * @return res If the deletion was successful
    * */
   public boolean sqlDeleteDiplomacyRequest(String receivingTeamName, String sendingTeamName)
   {
      boolean res = false;
      TATeam receivingTeam = plugin.getTeamByName(receivingTeamName);
      TATeam sendingTeam = plugin.getTeamByName(sendingTeamName);

      plugin.getSQLcore().deleteQuery("DELETE FROM tbDiplomacyRequests WHERE fk_teamID_receivingTeam = " + receivingTeam.getTeamID() +
            " AND fk_teamID_sendingTeam = " + sendingTeam.getTeamID() + ";");     
      ResultSet resSetMembers = plugin.getSQLcore().sqlQuery("SELECT fk_teamID_receivingTeam FROM tbDiplomacyRequests WHERE fk_teamID_receivingTeam = " +
            receivingTeam.getTeamID() + " AND fk_teamID_sendingTeam = " + sendingTeam.getTeamID() + ";");

      try
      {
         if(!resSetMembers.isBeforeFirst()) // Check if deletion was successful. isBeforeFirst() will be false if there is no row.
         {
            res = true;
         }
      }
      catch (SQLException e)
      {         
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on deleting team diplomacy request from DB!");
      }

      return res;
   }

   /**
    * <b>Get a list of all received diplomacy requests of a team directly from DB</b>    
    *
    * @param team The team to get the received diplomacy requests from
    * @return diplomacyRequests A list of all diplomacy requests of this team
    * */
   public HashMap<String, Status> sqlGetReceivedDiplomacyRequestsOfTeam(String teamName)
   {
      TATeam team = plugin.getTeamByName(teamName);
      ResultSet resSet = plugin.getSQLcore().sqlQuery("SELECT fk_teamID_sendingTeam, status FROM tbDiplomacyRequests WHERE fk_teamID_receivingTeam = " + team.getTeamID() + ";");
      HashMap<String, Status> diplomacyRequests = new HashMap<String, Status>();

      try
      {
         if(resSet.isBeforeFirst()) // check if there is at least one request found. isBeforeFirst() will return true if the cursor is before an existing row.
         {
            while(resSet.next())
            {               
               diplomacyRequests.put(plugin.getTeamNameByTeamID(resSet.getInt("fk_teamID_receivingTeam")), Status.getStatusByValue(resSet.getInt("status")));
            }
         }
      }
      catch (SQLException e)
      {
         TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "DB ERROR on reading diplomacy requests of team from DB!");         
      }

      return diplomacyRequests;
   }
}
