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
    * @param newName The name of the team.
    * @result res Whether or not the action was successful
    * */
   public boolean setName(String newName)
   {
      boolean res = false;

      if((null != newName) && (!newName.equals("")))
      {
         if(plugin.getSQLman().sqlSetTeamName(teamName, newName))
         {
            this.teamName = newName;
            res = true;
         }
      }

      return res;
   }

   /**
    * Set the leader of the team
    *
    * @param name The name of the team leader to set.
    * @result res Whether or not the action was successful
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

      return res;
   }

   /**
    * Remove a player from the team 
    *
    * @param name The name of the team member to remove.
    * @result res Whether or not the action was successful
    * */
   public boolean removeMember(String name)
   {
      boolean res = false;

      if((null != name) && (members.contains(name)))
      {
         if(plugin.getSQLman().sqlRemoveMemberFromTeam(teamName, name))
         {
            members.remove(name);         
            res = true;
         }
      }

      return res;
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
      
      return res;
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
         res = true;
      }

      return res;
   }

   /**
    * Get a list of active requests for this team
    *    
    * @return requests The active requests of this team FROM other players
    * */
   public ArrayList<String> getRequests()
   {
      return requests;
   }

   /**
    * Get a list of active invitations from this team 
    *    
    * @return invitations The invitations of this team TO other players
    * */
   public ArrayList<String> getInvitations()
   {
      return invitations;
   }
}