package com.github.CubieX.TeamAdvantage;

import com.github.CubieX.TeamAdvantage.CmdExecutors.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TACommandHandler implements CommandExecutor
{
   private TeamAdvantage plugin = null;
   private TAConfigHandler cHandler = null;   
   private ArrayList<String> helpList = new ArrayList<String>();   
   private HashMap<String, ISubCmdExecutor> subCommands = new HashMap<String, ISubCmdExecutor>();

   public TACommandHandler(TeamAdvantage plugin, TAConfigHandler cHandler) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;     

      loadSubCommands();
      loadHelpList();
   }

   private void loadSubCommands() // TODO buy und shop hinzufuegen. /team buy [Kategorie] -> liste der Effekte mit Preis und Dauer (nur Leader)  /team buy KÜRZEL DAUER -> Effekt kaufen für angegebene Zeit und direkt aktivieren (nur Leader)
   {
      subCommands.put("accept", new AcceptCmd());
      subCommands.put("buy", new BuyCmd());
      subCommands.put("chat", new ChatCmd());
      subCommands.put("clear", new ClearCmd());
      subCommands.put("create", new CreateCmd());
      subCommands.put("delete", new DeleteCmd());
      subCommands.put("deletehome", new DeleteHomeCmd());
      subCommands.put("deposit", new DepositCmd());
      subCommands.put("deny", new DenyCmd());
      subCommands.put("fee", new FeeCmd());      
      subCommands.put("home", new HomeCmd());
      subCommands.put("home-force-to", new HomeForceToCmd());
      subCommands.put("info", new InfoCmd()); // TODO Info ueber aktive Effekte des Teams hinzufuegen
      subCommands.put("invite", new InviteCmd());
      subCommands.put("leave", new LeaveCmd());
      subCommands.put("list", new ListCmd());      
      subCommands.put("me", new MeCmd()); // TODO Info ueber aktive Effekte des Spielers hinzufuegen
      subCommands.put("pay", new PayCmd());
      subCommands.put("pvp", new PvpCmd());
      subCommands.put("remove", new RemoveCmd());
      subCommands.put("request", new RequestCmd());
      subCommands.put("sethome", new SetHomeCmd());
      subCommands.put("setleader", new SetLeaderCmd());
      subCommands.put("setname", new SetNameCmd());
      subCommands.put("settag", new TagCmd());
      subCommands.put("uninvite", new UninviteCmd());
      subCommands.put("unrequest", new UnrequestCmd());
   }

   private void loadHelpList()
   {
      // add all available commands here      
      helpList.add("§a" + "Farben: §fJeder - §6Teamleiter - §cAdmins");
      helpList.add("§a" + "=============== Befehle ===============");
      helpList.add("§f" + "help - Dieses Hilfemenue");
      helpList.add("§f" + "version - Version des Plugins anzeigen");
      helpList.add("§f" + "me - Infos ueber sich selbst");
      helpList.add("§f" + "list [seite] - Liste aller Teams und Teamleiter");
      helpList.add("§f" + "info [teamName] [seite] - Infos ueber das Team");
      helpList.add("§f" + "chat - Umschalten zwischen Team- und normalem Chat");
      helpList.add("§f" + "deposit <Betrag> - Einzahlen von Geld auf das Teamkonto");
      helpList.add("§f" + "create <Teamname> <Chat-Tag> - Team erstellen");
      helpList.add("§f" + "request <Teamname> - Aufnahme in ein Team beantragen");
      helpList.add("§f" + "unrequest <Teamname> - Aufnahmeantrag zurueckziehen");
      helpList.add("§f" + "accept <Teamname/" + "§6" + "Spielername" + "§f" + "> - Einladung/Antrag annehmen");
      helpList.add("§f" + "deny <Teamname/" + "§6" + "Spielername" + "§f" + "> - Einladung/Antrag ablehnen");      
      helpList.add("§f" + "leave <Teamname> - Team verlassen");
      helpList.add("§f" + "home - Team-Homepunkt anspringen");
      helpList.add("§f" + "home-force-to - Team-Homepunkt anspringen trotz unsicherem Warp");
      helpList.add("§f" + "pvp - Diplomatie-Status anzeigen");
      helpList.add("§f" + "buy [Kategorie] - Alle Effekte zum Kaufen anzeigen");
      helpList.add("§6" + "buy <Effektkuerzel> <Dauer> - Effekt fuer x Stunden kaufen");
      helpList.add("§6" + "pvp <Teamname> alliance - Allianz vorschlagen");
      helpList.add("§6" + "pvp <Teamname> neutral - Neutral setzen");
      helpList.add("§6" + "pvp <Teamname> hostile - Krieg vorschlagen");
      helpList.add("§6" + "pvp <Teamname> accept/deny - Allianz/Krieg akzeptieren/ablehnen");
      helpList.add("§6" + "sethome - Team-Homepunkt setzen");
      helpList.add("§6" + "delhome - Team-Homepunkt loeschen");
      helpList.add("§6" + "delete <Teamname> - Team loeschen");      
      helpList.add("§6" + "setname <Teamname> - Team umbenennen");
      helpList.add("§6" + "setleader <Mitgliedsname> - Mitglied zum neuen Leiter machen");
      helpList.add("§6" + "settag <tag> - Team-Chat-Tag setzen (max. " + TeamAdvantage.MAX_CHAT_TAG_LENGTH + " Zeichen)");
      helpList.add("§6" + "invite <Spielername> - Spieler ins Team enladen");
      helpList.add("§6" + "uninvite <Spielername> - Einladung zurueckziehen");
      helpList.add("§6" + "remove <Spielername> - Mitglied aus Team entfernen");
      helpList.add("§6" + "clear - Alle Mitglieder des eigenes Teams entfernen");
      helpList.add("§6" + "pay <Mitgliedsname> <Betrag> - Geld an Teammitglied auszahlen");
      helpList.add("§6" + "fee - Team-Gebuehr nachzahlen um Bonus-Effekte freizuschalten");
      helpList.add("§4" + "reload - Plugin und DB-Daten neu laden");
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
      if (cmd.getName().equalsIgnoreCase("ta")) // TODO arg count in den Command-Klassen checken und nicht hier
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }
         else if (args.length == 1)
         {
            if (args[0].equalsIgnoreCase("version"))
            {
               sender.sendMessage("§a" + "This server is running " + plugin.getDescription().getName() + " version " +
                     plugin.getDescription().getVersion());
               return true;
            }

            // ME Get information about own team if applicable ===================================
            if (args[0].equalsIgnoreCase("me"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // LIST all existing teams, their leaders and money (Page 1) ===========================
            if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("liste"))
            {
               subCommands.get("list").execute(plugin, sender, player, args);
               return true;
            }

            // CHAT Toggle team chat =====================================================
            if (args[0].equalsIgnoreCase("chat"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // CLEAR all members of own team except from the leader himself =======================
            if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("flush") || args[0].equalsIgnoreCase("empty"))
            {
               subCommands.get("clear").execute(plugin, sender, player, args);
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
               subCommands.get("deletehome").execute(plugin, sender, player, args);
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

            // FEE Pay overdue fee to reactivate suspended team bonus effects ===================================
            if (args[0].equalsIgnoreCase("fee") || args[0].equalsIgnoreCase("steuer"))
            {
               subCommands.get("fee").execute(plugin, sender, player, args);
               return true;
            }

            // PVP Show diplomacy state of team (allies and enemies) ===================================
            if (args[0].equalsIgnoreCase("pvp"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // List all available bonus Effects ====================================
            if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("kaufen") || args[0].equalsIgnoreCase("shop"))
            {
               subCommands.get("buy").execute(plugin, sender, player, args);
               return true;
            }

            // HELP will be displayed (Page 1) =================================
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("hilfe"))
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
                  plugin.getGlobSQLman().loadTeamsFromDB(player);
               }
               else
               {
                  sender.sendMessage("§4" + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
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

            // DEPOSIT money to team account ==================
            if ((args[0].equalsIgnoreCase("deposit")) || (args[0].equalsIgnoreCase("einzahlen")))
            {
               subCommands.get("deposit").execute(plugin, sender, player, args);
               return true;
            }

            // HELP will be displayed (Page 2 and following) =================================
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("hilfe"))
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
                     sender.sendMessage("§6" + "Das 2. Argument muss eine positive Zahl sein!");
                  }
               }

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
            if (args[0].equalsIgnoreCase("setname") || args[0].equalsIgnoreCase("rename"))
            {
               subCommands.get("setname").execute(plugin, sender, player, args);
               return true;
            }

            // SETTAG to set a new team tag =======================
            if (args[0].equalsIgnoreCase("settag") || args[0].equalsIgnoreCase("tag"))
            {
               subCommands.get("settag").execute(plugin, sender, player, args);
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

            // List available BONUS EFFECTS to buy of given category ====================================
            if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("kaufen") || args[0].equalsIgnoreCase("shop"))
            {
               subCommands.get("buy").execute(plugin, sender, player, args);
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

            // CREATE new team =======================
            if (args[0].equalsIgnoreCase("create"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // PAY money from team account to a team members account =================
            if ((args[0].equalsIgnoreCase("pay")) || (args[0].equalsIgnoreCase("auszahlen")))
            {
               subCommands.get("pay").execute(plugin, sender, player, args);
               return true;
            }

            // PVP manage PvP actions ==================================================
            if (args[0].equalsIgnoreCase("pvp"))
            {
               subCommands.get(args[0].toLowerCase()).execute(plugin, sender, player, args);
               return true;
            }

            // BUY a bonus bonus effect for given duration ===============================
            if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("kaufen") || args[0].equalsIgnoreCase("shop"))
            {
               subCommands.get("buy").execute(plugin, sender, player, args);
               return true;
            }
         }
         else
         {
            sender.sendMessage("§6" + "Invalid argument count.");         
         }

         return false;
      }

      return false;
   }

   // ###################################################################################################

}
