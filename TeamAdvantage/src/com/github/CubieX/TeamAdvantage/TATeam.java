package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;

public class TATeam
{
   private TeamAdvantage plugin = null;
   private String teamName = "";
   private String leader = "";
   private ArrayList<String> members = new ArrayList<String>();      // members of this team (does not include the leader)
   private ArrayList<String> invitations = new ArrayList<String>();  // invitations sent to players
   private ArrayList<String> requests = new ArrayList<String>();     // requests received by players

   public TATeam(TeamAdvantage plugin, String teamName, String leader, ArrayList<String> members)
   {
      this.plugin = plugin;
      this.teamName = teamName;
      this.leader = leader;
      this.members = members;
   }

   /**
    * Returns the name of the team. 
    *
    * @return String teamName
    * */
   public String getName()
   {  
      return teamName;
   }

   /**
    * Returns the name of the team leader. 
    *
    * @return String leaderName
    * */
   public String getLeader()
   {  
      return leader;
   }

   /**
    * Returns a list of all team members, excluding the leader. 
    *
    * @return ArrayList<String> members
    * */
   public ArrayList<String> getMembers()
   {  
      return members;
   }

   /**
    * Set the name of the team 
    *
    * @param name The name of the team.
    * */
   public boolean setName(String name)
   {
      boolean res = false;

      if((null != name) && (!name.equals("")))
      {
         if(plugin.getSQLman().sqlSetTeamName(name))
         {
            this.teamName = name;
            res = true;
         }
      }

      return res;
   }

   /**
    * Set the leader of the team
    *
    * @param name The name of the team leader to set.
    * */
   public boolean setLeader(String leaderName)
   {
      boolean res = false;

      if((null != leaderName) && (!leaderName.equals("")))
      {
         if(plugin.getSQLman().sqlSetTeamLeader(teamName, leaderName))
         {
            this.leader = leaderName;         
            res = true;
         }
      }

      return res;
   }

   /**
    * Add a player as member of the team 
    *
    * @param name The name of the player to add.
    * */
   public boolean addMember(String name)
   {
      boolean res = false;

      if((null != name) && (!name.equals("")) && (!members.contains(name)))
      {
         members.add(name);
         // TODO also add member to DB in tbMemberships
         res = true;
      }

      return res;
   }

   /**
    * Remove a player from the team 
    *
    * @param name The name of the team member to remove.
    * */
   public boolean removeMember(String name)
   {
      boolean res = false;

      if((null != name) && (members.contains(name)))
      {
         members.remove(name);
         // TODO also remove member from DB in tbMemberships
         res = true;
      }

      return res;
   }

   /**
    * Removes all members from a team, except for the team leader 
    *
    * @param name The name of the team to clear all members from.
    * */
   public void clearMembers()
   {
      members.clear();
      // TODO also remove all members of this team from DB in tbMemberships
   }

   /**
    * Create an invitation for a player 
    *
    * @param name The name of the player to invite.
    * @return res If the creation was successful
    * */
   public boolean invitePlayer(String name)
   {
      boolean res = false;

      if((null != name)
            && (!name.equals(""))
            && (!members.contains(name))
            && (!invitations.contains(name)))
      {
         invitations.add(name);
         // TODO also add invitation to DB in tbInvitations
         res = true;
      }

      return res;
   }

   /**
    * Delete an active invitation for a player 
    *
    * @param name The name of the player to un-invite.
    * @return res If the deletion was successful
    * */
   public boolean uninvitePlayer(String name)
   {
      boolean res = false;

      if((null != name)
            && (invitations.contains(name)))
      {
         invitations.remove(name);
         // TODO also remove invitation of member from DB in tbInvitations
         res = true;
      }

      return res;
   }

   /**
    * Add a join request for a team 
    *
    * @param teamName The name of the team to send the join request to
    * @return res If the creation of the request was successful
    * */
   public boolean addJoinTeamRequest(String teamName)
   {
      boolean res = false;

      if((null != teamName)
            && (!teamName.equals(""))
            && (!requests.contains(teamName))
            && (TeamAdvantage.teams.contains(teamName)))
      {
         requests.add(teamName);
         // TODO also add request to DB in tbRequests
         res = true;
      }

      return res;
   }

   /**
    * Delete an active request for a team
    *
    * @param teamName The name of the team to delete the request for
    * @return res If the deletion was successful
    * */
   public boolean deleteJoinTeamRequest(String teamName)
   {
      boolean res = false;

      if((null != teamName)
            && (requests.contains(teamName)))
      {
         requests.remove(teamName);
         // TODO also remove request of member from DB in tbRequests
         res = true;
      }

      return res;
   }

   /**
    * Get active requests for this team
    *    
    * @return requests The active requests of this team FROM other players
    * */
   public ArrayList<String> getRequests()
   {
      return requests;
   }

   /**
    * Get active invitations from this team 
    *    
    * @return invitations The invitations of this team TO other players
    * */
   public ArrayList<String> getInvitations()
   {
      return invitations;
   }
}