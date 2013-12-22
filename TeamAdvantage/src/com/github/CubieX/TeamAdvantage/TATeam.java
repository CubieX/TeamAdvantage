package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;

import org.bukkit.Location;

public class TATeam
{
   private TeamAdvantage plugin = null;
   private String teamName = "";
   private String leader = "";
   private double money = 0.0;
   Location home = null;
   private ArrayList<String> members = new ArrayList<String>();      // members of this team (does not include the leader)
   private ArrayList<String> invitations = new ArrayList<String>();  // invitations sent to players
   private ArrayList<String> requests = new ArrayList<String>();     // requests received by players

   public TATeam(TeamAdvantage plugin, String teamName, String leader, double money)
   {
      this.plugin = plugin;
      this.teamName = teamName;
      this.leader = leader;
      this.money = money;
   }

   /**
    * Returns a copy of the list of all team members, excluding the leader.<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'addMember()' and 'removeMember()' to modify the list.
    * @return ArrayList<String> members
    * */
   public ArrayList<String> getMembers()
   {
      ArrayList<String> cpyMembers = new ArrayList<String>(members.size());

      for(String m : members)
      {
         cpyMembers.add(m);
      }

      return (cpyMembers);
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
         if(plugin.getSQLman().sqlSetTeamName(teamName, newTeamName))
         {
            this.teamName = newTeamName;
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns a copy of the name of the team.<br> 
    * <b>Caution:</b> Modifying the returned name does not change the actual name in the DB!<br>
    *          Use 'setName()' to modify the field.
    * @return teamName Name of the team
    * */
   public String getName()
   {
      String cpyTeamName = teamName;

      return (cpyTeamName);
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
         if(plugin.getSQLman().sqlSetTeamLeader(teamName, newLeaderName))
         {
            this.leader = newLeaderName;         
            res = true;
         }
      }

      return (res);
   }

   /**
    * Returns copy of the name of the team leader.<br>
    * <b>Caution:</b> Modifying the returned name does not change the actual name in the DB!<br>
    *          Use 'setLeader()' to modify the field.
    * @return cpyLeaderName Name of the leader
    * */
   public String getLeader()
   {
      String cpyLeaderName = leader;

      return (cpyLeaderName);
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

      if((amount > 0) && (amount < Integer.MAX_VALUE))
      {
         if(plugin.getSQLman().sqlSetTeamMoney(teamName, amount))
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
         if(plugin.getSQLman().sqlSetTeamHome(teamName, home))
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

      if(plugin.getSQLman().sqlSetTeamHome(teamName, null))
      {
         this.home = null;
         res = true;
      }

      return (res);
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
         if(plugin.getSQLman().sqlAddMemberToTeam(teamName, newMember))
         {
            members.add(newMember);         
            res = true;
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
         if(plugin.getSQLman().sqlRemoveMemberFromTeam(teamName, memberToRemove))
         {
            members.remove(memberToRemove);         
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

      if(plugin.getSQLman().sqlClearMembers(teamName))
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
         if(plugin.getSQLman().sqlAddInvitationForPlayerToTeam(teamName, invitedPlayer))
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
         if(plugin.getSQLman().sqlDeleteInvitationForPlayerFromTeam(teamName, invitedPlayer))
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
         if(plugin.getSQLman().sqlAddRequestFromPlayerToTeam(teamName, requestingPlayer))
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
         if(plugin.getSQLman().sqlDeleteRequestForTeamFromPlayer(teamName, requestingPlayer))
         {
            requests.remove(requestingPlayer);         
            res = true;
         }
      }

      return (res);
   }

   /**
    * Get a copy of the list of active join requests for this team<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'addJoinTeamRequest()' and 'deleteJoinTeamRequest()' to modify the list.
    * @return requests All active join requests for this team from players
    * */
   public ArrayList<String> getRequests()
   {
      ArrayList<String> cpyRequest = new ArrayList<String>(requests.size());

      for(String req : requests)
      {
         cpyRequest.add(req);
      }

      return (cpyRequest);
   }

   /**
    * Get a copy of the list of active invitations of this team to players<br>
    * <b>Caution:</b> Modifying this returned list does NOT impact the actual list in the DB!<br>
    *          Use 'invitePlayer()' and 'uninvitePlayer()' to modify the list.
    *
    * @return invitations All invitations of this team to players
    * */
   public ArrayList<String> getInvitations()
   {
      ArrayList<String> cpyInvitations = new ArrayList<String>(invitations.size());

      for(String inv : invitations)
      {
         cpyInvitations.add(inv);
      }

      return (cpyInvitations);
   }
}