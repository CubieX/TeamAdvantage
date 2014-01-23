package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;

public class TATeam
{
   private TeamAdvantage plugin = null;
   private TATeamSQLManager teamSQLman = null;
   private final int teamID;
   private String teamName = "";
   private String leader = "";
   private String tag = "";
   private double money = 0.0;
   private int teamXP = 0;
   private long teamFeeDueDateTimestamp = 0;
   Location home = null;
   private int teamBonusEffectsStatus = 1;                           // 0 = suspended, 1 = available
   private ArrayList<String> members = new ArrayList<String>();      // members of this team (does not include the leader)
   private ArrayList<String> invitations = new ArrayList<String>();  // invitations sent to players
   private ArrayList<String> requests = new ArrayList<String>();     // requests received by players
   private HashMap<String, Status> receivedDiplomacyRequests = new HashMap<String, Status>();   // diplomacy requests received from other teams
   private ArrayList<String> enemies = new ArrayList<String>();
   private ArrayList<String> allies = new ArrayList<String>();
   // teams not contained in 'allies' and 'enemies' map are NEUTRAL to this team

   // enum with diplomacy status information
   public enum Status
   {
      NONE(-1),
      ALLIED (0),
      NEUTRAL(1),
      HOSTILE (2);

      private final int value;

      Status(int value)
      {
         this.value = value;
      }

      public static Status getStatusByValue(int value)
      {
         Status status = NONE;

         for(Status s : Status.values())
         {
            if(s.value == value)
            {
               status = s;
               break;
            }
         }
         return (status);
      }

      public static int getValueOfStatus(Status status)
      {         
         return (status.value);
      }
   }

   public TATeam(TeamAdvantage plugin, TATeamSQLManager teamSQLman, int teamID, String teamName, String leader, String tag , double money, int teamXP, Location home, long teamFeeDueDateTimestamp, int teamBonusEffectsStatus)
   {
      this.plugin = plugin;
      this.teamSQLman = teamSQLman;
      this.teamID = teamID;
      this.teamName = teamName;
      this.leader = leader;
      this.money = money;
      this.teamXP = teamXP;
      this.home = home;
      this.tag = tag;
      this.teamFeeDueDateTimestamp = teamFeeDueDateTimestamp;
      this.teamBonusEffectsStatus = teamBonusEffectsStatus;
   }

   /**
    * Returns the teamID<br>
    *
    * @return teamID The teamID
    * 
    * */
   public int getTeamID()
   {
      return (teamID);
   }   

   /**
    * Returns a list of all team members, excluding the leader.<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'addMember()' and 'removeMember()' to modify the list.
    * @return members List of team members, excluding the leader
    * */
   public ArrayList<String> getMembers()
   {
      return (members);
   }
   
   /**
    * Returns a list of all team members and the leader.<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'addMember()' and 'removeMember()' to modify the list.
    * @return members List of team members, including the leader
    * */
   public ArrayList<String> getMembersAndLeader()
   {
      ArrayList<String> allTeamMembers = members;
      allTeamMembers.add(leader);
      
      return (allTeamMembers);
   }

   /**
    * Set the name of the team
    *
    * @param newTeamName The name of the team.
    * @result res Whether or not the action was successful
    * */
   public boolean setName(String newTeamName)
   {
      boolean res = false;

      if((null != newTeamName) && (!newTeamName.equals("")))
      {
         if(teamSQLman.sqlSetTeamName(teamName, newTeamName))
         {
            this.teamName = newTeamName;
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns the name of the team.<br> 
    * <b>Caution:</b> Modifying the returned name does not change the actual name in the DB!<br>
    *          Use 'setName()' to modify the field.
    * @return teamName Name of the team
    * */
   public String getName()
   {
      return (teamName);
   }

   /**
    * Set the leader of the team
    *
    * @param newLeaderName The name of the team leader
    * @result res Whether or not the action was successful
    * */
   public boolean setLeader(String newLeaderName)
   {
      boolean res = false;

      if((null != newLeaderName) && (!newLeaderName.equals("")))
      {
         if(teamSQLman.sqlSetTeamLeader(teamName, newLeaderName))
         {
            this.leader = newLeaderName;         
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns the name of the team leader.<br>
    * <b>Caution:</b> Modifying the returned name does not change the actual name in the DB!<br>
    *          Use 'setLeader()' to modify the field.
    * @return cpyLeaderName Name of the leader
    * */
   public String getLeader()
   {
      return (leader);
   }

   /**
    * Set the amount of money for the team account
    *
    * @param amount The amount of money to set
    * @result res Whether or not the action was successful
    * */
   public boolean setMoney(double amount)
   {
      boolean res = false;

      if((amount >= 0) && (amount <= Integer.MAX_VALUE))
      {
         if(teamSQLman.sqlSetTeamMoney(teamName, amount))
         {
            this.money = amount;
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns the amount of money the team has in team account    
    *
    * @result money The amount of money the team has
    * */
   public double getMoney()
   {  
      return (money);
   }

   /**
    * Set the amount of XP for the team
    *
    * @param amount The amount of XP to set
    * @result res If the action was successful
    * */
   public boolean setXP(int xp)
   {
      boolean res = false;

      if((xp >= 0) && (xp <= Integer.MAX_VALUE))
      {
         if(teamSQLman.sqlSetTeamXP(teamName, xp))
         {
            this.money = xp;
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns the amount of XP the team has
    *
    * @result xp The amount of XP the team has
    * */
   public int getXP()
   {  
      return (teamXP);
   }

   /**
    * Set the chat tag
    *
    * @param newTag The new tag
    * @result res Whether or not the action was successful
    * */
   public boolean setTag(String newTag)
   {
      boolean res = false;

      if((null != newTag) && (!newTag.equals("")))
      {
         if(teamSQLman.sqlSetTeamTag(teamName, newTag))
         {
            this.tag = newTag;
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns the team tag.<br>
    * <b>Caution:</b> Modifying the returned tag does not change the actual tag in the DB!<br>
    *          Use 'setTag()' to modify the field.
    * @return cpyTag The team chat tag
    * */
   public String getTag()
   {
      return (tag);
   }

   /**
    * Set the home point
    *
    * @param newLeaderName The name of the team leader
    * @result res Whether or not the action was successful
    * */
   public boolean setHome(Location home)
   {
      boolean res = false;

      if(null != home)
      {
         if(teamSQLman.sqlSetTeamHome(teamName, home))
         {
            this.home = home;         
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns copy of the home location.<br>
    * <b>Caution:</b> Modifying the returned location does not change the actual location in the DB!<br>
    *          Use 'setHome()' or 'deleteHome()' to modify the field.
    * @return cpyHomeLoc Location of the home point
    * */
   public Location getHome()
   {
      Location cpyHomeLoc = home;

      return (cpyHomeLoc);
   }

   /**
    * Delete the teams home point
    *
    * @result res Whether or not the action was successful
    * */
   public boolean deleteHome()
   {
      boolean res = false;

      if(teamSQLman.sqlSetTeamHome(teamName, null))
      {
         this.home = null;
         res = true;
      }

      return (res);
   }

   /**
    * Schedule the next team fee due date.
    * 
    * @return res If the action was successful
    * */
   public boolean scheduleNextTeamFeeDueDate()
   {
      boolean res = false;
      long ts = 0;

      if(TeamAdvantage.teamFeeCycle > 0)
      {
         ts = System.currentTimeMillis() + (TeamAdvantage.teamFeeCycle * 24 * 3600 * 1000);
      }
      else
      {
         ts = 0;
      }

      if(teamSQLman.sqlSetTeamFeeDueDate(teamName, ts))
      {
         this.teamFeeDueDateTimestamp = ts;
         res = true;
      }

      return (res);
   }

   /**
    * Get the next team fee due date time stamp in ms
    *
    * @return time stamp The time stamp in ms of the next scheduled team fee due date
    * */
   public long getNextTeamFeeDueDateTimestamp()
   {
      return (this.teamFeeDueDateTimestamp);
   }

   /**
    * <b>Set the teams bonus effects status.</b><br>
    * This can be:<br>
    * 0: Bonus effects are deactivated and suspended<br>
    * 1: Bonus effects are available<br>
    *
    * @param status The status to set
    * @return res If the action was successful
    * */
   public boolean setTeamBonusEffectsStatus(int status)
   {
      boolean res = false;

      if(teamSQLman.sqlSetTeamBonusEffectsStatus(teamName, status))
      {
         this.teamBonusEffectsStatus = status;
         res = true;
      }

      return (res);
   }

   /**
    * <b>Get the teams bonus effects status.</b><br>
    * This can be:<br>
    * 0: Bonus effects are deactivated and suspended
    * 1: Bonus effects are available
    *
    * @return status The status of the teams bonus effects
    * */
   public int getTeamBonusEffectsStatus()
   {
      return (this.teamBonusEffectsStatus);
   }

   /**
    * Add a player as member of the team 
    *
    * @param newMember The name of the member to add.
    * @result res Whether or not the action was successful
    * */
   public boolean addMember(String newMember)
   {
      boolean res = false;

      if((null != newMember) && (!newMember.equals("")) && (!members.contains(newMember)))
      {
         if(teamSQLman.sqlAddMemberToTeam(teamName, newMember))
         {
            members.add(newMember);

            if(teamSQLman.sqlDeleteRequestForTeamFromPlayer(teamName, newMember))
            {
               requests.remove(newMember);

               if(teamSQLman.sqlDeleteInvitationForPlayerFromTeam(teamName, newMember))
               {
                  invitations.remove(newMember);
                  res = true;
               }
            }
         }
      }

      return (res);
   }

   /**
    * Remove a player from the team 
    *
    * @param memberToRemove The name of the team member to remove
    * @result res Whether or not the action was successful
    * */
   public boolean removeMember(String memberToRemove)
   {
      boolean res = false;

      if((null != memberToRemove) && (members.contains(memberToRemove)))
      {
         if(teamSQLman.sqlRemoveMemberFromTeam(teamName, memberToRemove))
         {
            members.remove(memberToRemove);

            if(TAChatManager.teamChat.contains(memberToRemove))
            {
               TAChatManager.teamChat.remove(memberToRemove);
            }

            res = true;
         }
      }

      return (res);
   }

   /**
    * Removes all members from a team except for the leader
    *
    * @result res Whether or not the action was successful
    * */
   public boolean clearMembers()
   {
      boolean res = false;

      if(teamSQLman.sqlClearMembers(teamName))
      {
         members.clear();
         res = true;
      }

      return (res);
   }

   /**
    * Create an invitation for a player 
    *
    * @param name The name of the player to invite.
    * @return res If the creation was successful
    * */
   public boolean invitePlayer(String invitedPlayer)
   {
      boolean res = false;

      if((null != invitedPlayer)
            && (!invitedPlayer.equals(""))
            && (!members.contains(invitedPlayer))
            && (!invitations.contains(invitedPlayer)))
      {
         if(teamSQLman.sqlAddInvitationForPlayerToTeam(teamName, invitedPlayer))
         {
            invitations.add(invitedPlayer);
            res = true;
         }
      }

      return (res);
   }

   /**
    * Delete an active invitation for a player 
    *
    * @param name The name of the player to un-invite.
    * @return res If the deletion was successful
    * */
   public boolean uninvitePlayer(String invitedPlayer)
   {
      boolean res = false;

      if((null != invitedPlayer)
            && (!invitedPlayer.equals(""))
            && (invitations.contains(invitedPlayer)))
      {
         if(teamSQLman.sqlDeleteInvitationForPlayerFromTeam(teamName, invitedPlayer))
         {
            invitations.remove(invitedPlayer);
            res = true;
         }
      }

      return (res);
   }

   /**
    * Add a join request for the team 
    *
    * @param requestingPlayer The name of the requesting player
    * @return res If the creation of the request was successful
    * */
   public boolean addJoinTeamRequest(String requestingPlayer)
   {
      boolean res = false;

      if((null != requestingPlayer)
            && (!requestingPlayer.equals(""))
            && (!requests.contains(requestingPlayer)))
      {
         if(teamSQLman.sqlAddRequestFromPlayerToTeam(teamName, requestingPlayer))
         {
            requests.add(requestingPlayer);        
            res = true;
         }
      }

      return (res);
   }

   /**
    * Delete an active request for a team
    *
    * @param requestingPlayer The name of the player to delete the request for
    * @return res If the deletion was successful
    * */
   public boolean deleteJoinTeamRequest(String requestingPlayer)
   {
      boolean res = false;

      if((null != requestingPlayer)
            && (!requestingPlayer.equals(""))
            && (requests.contains(requestingPlayer)))
      {
         if(teamSQLman.sqlDeleteRequestForTeamFromPlayer(teamName, requestingPlayer))
         {
            requests.remove(requestingPlayer);         
            res = true;
         }
      }

      return (res);
   }

   /**
    * Add a RECEIVED diplomacy request for the team 
    *
    * @param requestingTeam The name of the requesting team
    * @param newStatus Requested diplomacy status
    * @return res If the creation of the request was successful
    * */
   public boolean addDiplomacyRequest(String requestingTeam, Status newStatus)
   {
      boolean res = false;

      if((null != requestingTeam)
            && (!requestingTeam.equals("")))
      {
         if(newStatus != Status.NEUTRAL)
         {
            if(teamSQLman.sqlAddReceivedDiplomacyRequest(teamName, requestingTeam, newStatus))
            {
               receivedDiplomacyRequests.put(requestingTeam, newStatus);
               res = true;
            }
         }
         else // Status NEUTRAL will be set directly. (no request)
         {
            if(setDiplomacyStatus(requestingTeam, newStatus))
            {
               receivedDiplomacyRequests.remove(requestingTeam);
               res = true;
            }
         }         
      }

      return (res);
   }

   /**
    * Delete an active diplomacy request from another team
    *
    * @param sendingTeam The name of the team to delete the request for
    * @return res If the deletion was successful
    * */
   public boolean deleteDiplomacyRequest(String sendingTeam)
   {
      boolean res = false;

      if((null != sendingTeam)
            && (!sendingTeam.equals(""))
            && (receivedDiplomacyRequests.containsKey(sendingTeam)))
      {
         if(teamSQLman.sqlDeleteDiplomacyRequest(teamName, sendingTeam))
         {
            receivedDiplomacyRequests.remove(sendingTeam);
            res = true;
         }
      }

      return (res);
   }

   /**
    * Get a list of active join requests for this team<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'addJoinTeamRequest()' and 'deleteJoinTeamRequest()' to modify the list.
    * @return requests All active join requests for this team from players
    * */
   public ArrayList<String> getRequests()
   {
      return (requests);
   }

   /**
    * Get a list of active invitations of this team to players<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'invitePlayer()' and 'uninvitePlayer()' to modify the list.
    *
    * @return invitations All invitations of this team to players
    * */
   public ArrayList<String> getInvitations()
   {
      return (invitations);
   }

   /**
    * Get a map of received diplomacy requests of this team from other teams<br>
    * <b>Caution:</b> Modifying this returned map does NOT impact the actual data in the DB!<br>
    *          Use 'invitePlayer()' and 'uninvitePlayer()' to modify the list.
    *
    * @return receivedDiplomacyRequests All received diplomacy requests of this team
    * */
   public HashMap<String, Status> getReceivedDiplomacyRequests()
   {
      return (receivedDiplomacyRequests);
   }

   /**
    * Get a list of allied teams of this team<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'setDiplomacyStatus()' to modify the list.
    *
    * @return allies All allies of this team
    * */
   public ArrayList<String> getAllies()
   {
      return (allies);
   }

   /**
    * Get a list of enemy teams of this team<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'setDiplomacyStatus()' to modify the list.
    *
    * @return enemies All enemies of this team
    * */
   public ArrayList<String> getEnemies()
   {
      return (enemies);
   }

   /**
    * Set the diplomacy status of team to another team
    *
    * @param otherTeamsName The name of the team to set the diplomacy status for
    * @param newStatus The status to set
    * @result res If the action was successful
    * */
   public boolean setDiplomacyStatus(String otherTeamsName, Status newStatus)
   {
      boolean res = false;

      if((null != otherTeamsName) && (!otherTeamsName.equals("")) && (newStatus != Status.NONE))
      {
         TATeam otherTeam = plugin.getTeamByName(otherTeamsName);

         if(null != otherTeam)
         {
            if(teamSQLman.sqlSetDiplomacyStatus(teamName, otherTeamsName, newStatus))
            {
               if(teamSQLman.sqlDeleteDiplomacyRequest(teamName, otherTeamsName))
               {
                  receivedDiplomacyRequests.remove(otherTeamsName);

                  switch (newStatus)
                  {
                  case ALLIED:
                     enemies.remove(otherTeamsName);
                     otherTeam.getEnemies().remove(teamName);

                     if(!allies.contains(otherTeamsName))
                     {
                        allies.add(otherTeamsName);                  
                        otherTeam.getAllies().add(teamName);
                     }
                     res = true;
                     break;
                  case NEUTRAL: // status NEUTRAL is not stored (no entry means: NEUTRAL)
                     enemies.remove(otherTeamsName);
                     otherTeam.getEnemies().remove(teamName);
                     allies.remove(otherTeamsName);
                     otherTeam.getAllies().remove(teamName);
                     res = true;
                     break;
                  case HOSTILE:
                     allies.remove(otherTeamsName);
                     otherTeam.getAllies().remove(teamName);

                     if(!enemies.contains(otherTeamsName))
                     {
                        enemies.add(otherTeamsName);
                        otherTeam.getEnemies().add(teamName);
                     }
                     res = true;
                     break;
                  default:
                     // should never be reached
                     TeamAdvantage.log.severe(TeamAdvantage.logPrefix + "ERROR! Tried to set invalid diplomacy status!");
                  }                  
               }               
            }
         }
      }

      return (res);
   }

   /**
    * Returns the diplomacy status of the team to another team.<br> 
    * <b>Caution:</b> Modifying the returned status does not change the actual status in the DB!<br>
    *          Use 'setDiplomacyStatus()' to modify the field.
    * @return teamName Name of the team
    * */
   public Status getDiplomacyStatus(String otherTeam)
   {
      Status status;

      if(allies.contains(otherTeam))
      {
         status = Status.ALLIED;
      }
      else if(enemies.contains(otherTeam))
      {
         status = Status.HOSTILE;
      }
      else
      {
         status = Status.NEUTRAL;
      }

      return (status);
   }
}