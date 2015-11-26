package com.timvisee.dungeonmaze.command.executable;

import com.timvisee.dungeonmaze.Core;
import com.timvisee.dungeonmaze.command.CommandParts;
import com.timvisee.dungeonmaze.command.ExecutableCommand;
import com.timvisee.dungeonmaze.update.UpdateChecker;
import com.timvisee.dungeonmaze.update.UpdateCheckerService;
import com.timvisee.dungeonmaze.update.bukkit.Updater;
import com.timvisee.dungeonmaze.util.Profiler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CheckUpdatesCommand extends ExecutableCommand {

    /**
     * Execute the command.
     *
     * @param sender           The command sender.
     * @param commandReference The command reference.
     * @param commandArguments The command arguments.
     *
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean executeCommand(CommandSender sender, CommandParts commandReference, CommandParts commandArguments) {
        // Profile the process
        Profiler p = new Profiler(true);

        // Show a status message
        sender.sendMessage(ChatColor.YELLOW + "Checking for Dungeon Maze updates...");

        // Get the update checker service, shut it down and start it again to force an update check
        UpdateCheckerService service = Core.getUpdateCheckerService();
        service.shutdownUpdateChecker();
        service.setupUpdateChecker();

        // Get the update checker instance
        UpdateChecker updateChecker = service.getUpdateChecker();

        // Show a status message
        sender.sendMessage(ChatColor.YELLOW + "Update checking succeed, took " + p.getTimeFormatted() + "!");

        // Get the version number and code of the new update if there is any
        String newVersionName = updateChecker.getUpdateVersionName();
        int newVersionCode = updateChecker.getUpdateVersionCode();

        // Make sure any update is available
        if(updateChecker.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE) {
            sender.sendMessage(ChatColor.GREEN + "New Dungeon Maze version available: " + String.valueOf(newVersionName));
            return true;

        } else if(updateChecker.getResult() == Updater.UpdateResult.NO_UPDATE) {
            sender.sendMessage(ChatColor.GREEN + "You are running the latest Dungeon Maze version!");
            return true;
        }

        // Make sure the new version is compatible with the current bukkit version
        if(updateChecker.getResult() == Updater.UpdateResult.FAIL_NOVERSION) {
            // Show a message
            sender.sendMessage(ChatColor.GREEN + "New Dungeon Maze version available: " + String.valueOf(newVersionName));
            sender.sendMessage(ChatColor.DARK_RED + "The new version is not compatible with your Bukkit version!");
            sender.sendMessage(ChatColor.DARK_RED + "Please update your Bukkit to " +  updateChecker.getLatestGameVersion() + " or higher!");
            return true;
        }

        // Check whether the update was installed or not
        if(updateChecker.getResult() == Updater.UpdateResult.SUCCESS)
            sender.sendMessage(ChatColor.GREEN + "New version installed (" + String.valueOf(newVersionName) + "). Server reboot required!");

        else {
            sender.sendMessage(ChatColor.GREEN + "New version found: " + String.valueOf(newVersionName));
            //noinspection SpellCheckingInspection
            sender.sendMessage(ChatColor.GREEN + "Use " + ChatColor.GOLD + "/dm installupdate" +
                    ChatColor.GREEN + " to automatically install the new version!");
        }

        // Return the result
        return true;
    }
}
