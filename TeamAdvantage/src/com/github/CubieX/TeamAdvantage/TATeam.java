package com.github.CubieX.TeamAdvantage;

import java.util.ArrayList;

public class TATeam
{
   private String teamName = "";
   private String leader = "";
   private ArrayList<String> members = new ArrayList<String>();

   public TATeam(String teamName, String leader, ArrayList<String> members)
   {
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

   public boolean setName(String name)
   {
      boolean res = false;

      if((null != name) && (!name.equals("")))
      {
         this.teamName = name;
         // TODO also set team name in DB in tbTeams
         res = true;
      }

      return res;
   }

   public boolean setLeader(String name)
   {
      boolean res = false;

      if((null != name) && (!name.equals("")))
      {
         this.leader = name;
         // TODO also set leader in DB in tbTeams
         res = true;
      }

      return res;
   }

   public boolean addMember(String name)
   {
      boolean res = false;

      if((null != name) && (!name.equals("")))
      {
         this.leader = name;
         // TODO also add member to DB in tbMemberships
         res = true;
      }

      return res;
   }

   public boolean deleteMember(String name)
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

   public void clearMembers()
   {
      members.clear();
      // TODO also remove all members of this team from DB in tbMemberships
   }
}