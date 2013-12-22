/**
 * 
 */
package com.github.CubieX.TeamAdvantage.CmdExecutors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.CubieX.TeamAdvantage.TeamAdvantage;

/**
 * @author CubieX
 *
 */
public interface ISubCmdExecutor
{
   public abstract void execute(TeamAdvantage plugin, CommandSender sender, Player player, String[] args);
}
