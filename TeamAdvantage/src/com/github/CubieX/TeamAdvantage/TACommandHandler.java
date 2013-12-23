package com.github.CubieX.TeamAdvantage;

import com.github.CubieX.TeamAdvantage.CmdExecutors.*;

import java.util.ArrayList;
import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TACommandHandler implements CommandExecutor
{
   private TeamAdvantage plugin = null;
   private TAConfigHandler cHandler = null;
   private TASQLManager sqlMan = null;
   private Economy econ = null;
   private ArrayList<String> helpList = new ArrayList<String>();   
   private HashMap<String, ISubCmdExecutor> subCommands = new HashMap<String, ISubCmdExecutor>();

   public TACommandHandler(TeamAdvantage plugin, TAConfigHandler cHandler, TASQLManager sqlMan, Economy econ) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
      this.sqlMan = sqlMan;
      this.econ = econ;

      loadSubCommands();
      loadHelpList();
   }
   
   private void loadSubCommands()
   {
      subCommands.put("accept", new AcceptCmd());
      subCommands.put("clear", new ClearCmd());
      subCommands.put("create", new CreateCmd());
      subCommands.put("delete", new DeleteCmd());
      subCommands.put("deletehome", new DeleteHomeCmd());
      subCommands.put("deny", new DenyCmd());
      subCommands.put("home", new HomeCmd());
      subCommands.put("home-force-to", new HomeForceToCmd());
      subCommands.put("info", new InfoCmd());
      subCommands.put("invite", new InviteCmd());
      subCommands.put("leave", new LeaveCmd());
      subCommands.put("list", new ListCmd());
      subCommands.put("remove", new RemoveCmd());
      subCommands.put("request", new RequestCmd());
      subCommands.put("sethome", new SetHomeCmd());
      subCommands.put("setleader", new SetLeaderCmd());
      subCommands.put("setname", new SetNameCmd());
      subCommands.put("uninvite", new UninviteCmd());
      subCommands.put("unrequest", new UnrequestCmd());
   }

   private void loadHelpList()
   {
      // add all available commands here      
      helpList.add("" + ChatColor.GREEN + "Farben: "+ ChatColor.WHITE + "Jeder" + ChatColor.WHITE + " - " + ChatColor.YELLOW + "Teamleiter" + ChatColor.WHITE + " - " + ChatColor.RED + "Admins");
      helpList.add("" + ChatColor.GREEN + "=============== Befehle ===============");
      helpList.add("" + ChatColor.WHITE + "help - Dieses Hilfemenue");
      helpList.add("" + ChatColor.WHITE + "version - Version des Plugins anzeigen");
      helpList.add("" + ChatColor.WHITE + "list [seite] - Liste aller Teams und Teamleiter");
      helpList.add("" + ChatColor.WHITE + "info [teamName][seite] - Infos ueber das Team");
      helpList.add("" + ChatColor.WHITE + "create <Teamname> - Team erstellen");
      helpList.add("" + ChatColor.WHITE + "request <Teamname> - Aufnahme in ein Team beantragen");
      helpList.add("" + ChatColor.WHITE + "unrequest <Teamname> - Aufnahmeantrag zurueckziehen");
      helpList.add("" + ChatColor.WHITE + "accept <Teamname/"+ ChatColor.YELLOW + "Spielername" + ChatColor.WHITE + "> - Einladung/Antrag annehmen");
      helpList.add("" + ChatColor.WHITE + "deny <Teamname/" + ChatColor.YELLOW + "Spielername" + ChatColor.WHITE + "> - Einladung/Antrag ablehnen");
      helpList.add("" + ChatColor.WHITE + "leave <Teamname> - Team verlassen");
      helpList.add("" + ChatColor.YELLOW + "delete <Teamname> - Team loeschen");
      helpList.add("" + ChatColor.YELLOW + "setname <Teamname> - Team umbenennen");
      helpList.add("" + ChatColor.YELLOW + "setleader <Mitgliedsname> - Mitglied zum neuen Leiter machen");
      helpList.add("" + ChatColor.YELLOW + "invite <Spielername> - Spieler ins Team enladen");
      helpList.add("" + ChatColor.YELLOW + "uninvite <Spielername> - Einladung zurueckziehen");
      helpList.add("" + ChatColor.YELLOW + "remove <Spielername> - Mitglied aus Team entfernen");
      helpList.add("" + ChatColor.YELLOW + "clear - Alle Mitglieder des eigenes Teams entfernen");
      helpList.add("" + ChatColor.RED + "reload - Plugin und DB-Daten neu laden");
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player player = null;
      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      // Befehls-Ideen siehe: http://www.northkingdom.eu/board/index.php?/topic/1125-server-30-team-plugin
      if (cmd.getName().equalsIgnoreCase("ta"))
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }
         else if (args.length == 1)
         {
            if (args[0].equalsIgnoreCase("version"))
            {            
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " version " +
                     plugin.getDescription().getVersion());
               return true;
            }

            // LIST all existing teams, their leaders and money (Page 1) ==================
            if (args[0].equalsIgnoreCase("list"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // CLEAR all members of own team except from the leader himself =======================
            if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("flush") || args[0].equalsIgnoreCase("empty"))
            {
               subCommands.get("clear".toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // LEAVE a team =================================
            if (args[0].equalsIgnoreCase("leave"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // SETHOME of a team =================================
            if (args[0].equalsIgnoreCase("sethome"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // DELETEHOME of a team =================================
            if ((args[0].equalsIgnoreCase("deletehome")) || (args[0].equalsIgnoreCase("delhome")))
            {
               subCommands.get("deletehome".toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // HOME Teleport to the home point of the team of issuing player =======================
            if (args[0].equalsIgnoreCase("home"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // HOME-FORCE-TO Teleport override to the potentially not save home point of the team of issuing player =======================
            if (args[0].equalsIgnoreCase("home-force-to"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // HELP will be displayed (Page 1) =================================
            if (args[0].equalsIgnoreCase("help"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  // send list paginated
                  plugin.paginateHelpList(sender, helpList, 1, helpList.size());
               }

               return true;
            }

            // RELOAD the plugin ================================
            if (args[0].equalsIgnoreCase("reload"))
            {
               if(sender.hasPermission("teamadvantage.admin"))
               {
                  cHandler.reloadConfig(sender);
                  sqlMan.loadTeamsFromDB(player);
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
               }

               return true;
            }
         }
         else if (args.length == 2)
         {
            // LIST all existing teams, their leaders and money (Page 2 and following) ==================
            if (args[0].equalsIgnoreCase("list"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // INFO about all members of a team and the leader (Page 1) =================
            if (args[0].equalsIgnoreCase("info"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // HELP will be displayed (Page 2 and following) =================================
            if (args[0].equalsIgnoreCase("help"))
            {
               if(sender.hasPermission("teamadvantage.use"))
               {
                  if(plugin.isValidInteger(args[1]))
                  {
                     int currPage = Integer.parseInt(args[1]);

                     // send list paginated
                     plugin.paginateHelpList(sender, helpList, currPage, helpList.size());
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.YELLOW + "Das 2. Argument muss eine positive Zahl sein!");
                  }
               }

               return true;
            }

            // CREATE new team =======================
            if (args[0].equalsIgnoreCase("create"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // DELETE existing team =======================
            if (args[0].equalsIgnoreCase("delete"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // SETLEADER to set a new leader for the team - old leader will become a normal member =======================
            if (args[0].equalsIgnoreCase("setleader"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // SETNAME to set a new team name =======================
            if (args[0].equalsIgnoreCase("setname"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }        

            // INVITE player into team =======================
            if (args[0].equalsIgnoreCase("invite"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // UNINVITE player =======================
            if (args[0].equalsIgnoreCase("uninvite"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // REQUEST membership in a team =======================
            if (args[0].equalsIgnoreCase("request"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // UNREQUEST deletes a request for membership of a player for a team =======================
            if (args[0].equalsIgnoreCase("unrequest"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // ACCEPT membership invitation of a team / ACCEPT membership request of a player =======================
            if (args[0].equalsIgnoreCase("accept"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // DENY membership invitation of a team / DENY membership request of a player =======================
            if (args[0].equalsIgnoreCase("deny"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // REMOVE player from team =======================
            if (args[0].equalsIgnoreCase("remove"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }
         }
         else if (args.length == 3)
         {
            // INFO about all members of a team and the leader (Page 2 and following) =================
            if (args[0].equalsIgnoreCase("info"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }
         }
         else
         {
            sender.sendMessage(ChatColor.YELLOW + "Invalid argument count.");         
         }

         return false;
      }

      return false;
   }

   // ###################################################################################################
   
}
