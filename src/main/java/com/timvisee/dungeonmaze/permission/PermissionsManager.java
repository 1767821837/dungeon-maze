package com.timvisee.dungeonmaze.permission;

import com.nijiko.permissions.Group;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PermissionsManager.
 *
 * A permissions manager, to manage and use various permissions systems.
 * This manager supports dynamic plugin hooking and various other features.
 *
 * Written by Tim Visée.
 *
 * @author Tim Visée, http://timvisee.com
 * @version 0.2.2
 */
public class PermissionsManager {

    /**
     * Server instance.
     */
    private Server server;

    /**
     * Plugin instance.
     */
    private Plugin plugin;

    /**
     * Logger instance.
     */
    private Logger log;

    /**
     * Type of permissions system that is currently used.
     * Null if no permissions system is used.
     */
    private PermissionsSystemType permsType = null;

    /**
     * Essentials group manager instance.
     */
    private GroupManager groupManagerPerms;

    /**
     * Permissions manager instance for the legacy permissions system.
     */
    private PermissionHandler defaultPerms;

    /**
     * zPermissions service instance.
     */
    private ZPermissionsService zPermissionsService;

    /**
     * Vault instance.
     */
    public Permission vaultPerms = null;

    /**
     * Constructor.
     *
     * @param server Server instance
     * @param plugin Plugin instance
     * @param log    Logger
     */
    public PermissionsManager(Server server, Plugin plugin, Logger log) {
        this.server = server;
        this.plugin = plugin;
        this.log = log;
    }

    /**
     * Check if the permissions manager is currently hooked into any of the supported permissions systems.
     *
     * @return False if there isn't any permissions system used.
     */
    public boolean isEnabled() {
        return !permsType.equals(null);
    }

    /**
     * Return the permissions system where the permissions manager is currently hooked into.
     *
     * @return Permissions system type.
     */
    public PermissionsSystemType getUsedPermissionsSystemType() {
        return this.permsType;
    }

    /**
     * Setup and hook into the permissions systems.
     *
     * @return The detected permissions system.
     */
    public PermissionsSystemType setup() {
        // Define the plugin manager
        final PluginManager pluginManager = this.server.getPluginManager();

        // Reset used permissions system type flag
        permsType = null;

        // Create a variable to store the permissions system that is currently being checked
        PermissionsSystemType currentCheck;

        // PermissionsEx, check if it's available
        currentCheck = PermissionsSystemType.PERMISSIONS_EX;
        try {
            Plugin pex = pluginManager.getPlugin(currentCheck.getPluginName());
            if (pex != null) {
                PermissionManager pexPerms = PermissionsEx.getPermissionManager();
                if (pexPerms != null) {
                    permsType = currentCheck;
                    System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                    return currentCheck;
                }
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // PermissionsBukkit, check if it's available
        currentCheck = PermissionsSystemType.PERMISSIONS_BUKKIT;
        try {
            Plugin bukkitPerms = pluginManager.getPlugin(currentCheck.getPluginName());
            if (bukkitPerms != null) {
                permsType = currentCheck;
                System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                return currentCheck;
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // bPermissions, check if it's available
        currentCheck = PermissionsSystemType.B_PERMISSIONS;
        try {
            Plugin bPerms = pluginManager.getPlugin(currentCheck.getPluginName());
            if (bPerms != null) {
                permsType = currentCheck;
                System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                return currentCheck;
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // Essentials Group Manager, check if it's available
        currentCheck = PermissionsSystemType.ESSENTIALS_GROUP_MANAGER;
        try {
            final Plugin groupManagerPlugin = pluginManager.getPlugin(currentCheck.getPluginName());
            if (groupManagerPlugin != null && groupManagerPlugin.isEnabled()) {
                permsType = currentCheck;
                groupManagerPerms = (GroupManager) groupManagerPlugin;
                System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                return currentCheck;
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // zPermissions, check if it's available
        currentCheck = PermissionsSystemType.Z_PERMISSIONS;
        try {
            Plugin zPerms = pluginManager.getPlugin(currentCheck.getPluginName());
            if (zPerms != null) {
                zPermissionsService = Bukkit.getServicesManager().load(ZPermissionsService.class);
                if (zPermissionsService != null) {
                    permsType = currentCheck;
                    System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                    return currentCheck;
                }
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // Vault, check if it's available
        currentCheck = PermissionsSystemType.VAULT;
        try {
            final Plugin vaultPlugin = pluginManager.getPlugin(currentCheck.getPluginName());
            if (vaultPlugin != null && vaultPlugin.isEnabled()) {
                RegisteredServiceProvider<Permission> permissionProvider = this.server.getServicesManager().getRegistration(Permission.class);
                if (permissionProvider != null) {
                    vaultPerms = permissionProvider.getProvider();
                    if (vaultPerms.isEnabled()) {
                        permsType = currentCheck;
                        System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                        return currentCheck;
                    } else
                        System.out.println("[" + plugin.getName() + "] Not using " + currentCheck.getName() + ", " + currentCheck.getName() + " is disabled!");
                }
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // Permissions, check if it's available
        currentCheck = PermissionsSystemType.PERMISSIONS;
        try {
            Plugin testPerms = pluginManager.getPlugin(currentCheck.getPluginName());
            if (testPerms != null) {
                permsType = currentCheck;
                this.defaultPerms = ((Permissions) testPerms).getHandler();
                System.out.println("[" + plugin.getName() + "] Hooked into " + currentCheck.getName() + "!");
                return currentCheck;
            }
        } catch (Exception ex) {
            // An error occurred, show a warning message
            System.out.println("[" + plugin.getName() + "] Error while hooking into " + currentCheck.getName() + "!");
        }

        // No recognized permissions system found
        permsType = null;
        System.out.println("[" + plugin.getName() + "] No supported permissions system found! " + currentCheck.getName() + " disabled!");
        return permsType;
    }

    /**
     * Break the hook with all permission systems.
     */
    public void unhook() {
        // Store the permissions system that was hooked
        PermissionsSystemType hookedSystem = getUsedPermissionsSystemType();

        // Reset the current used permissions system
        this.permsType = null;

        // Print a status message to the console
        if (hookedSystem != null)
            this.log.info("Unhooked from " + hookedSystem + "!");
    }

    /**
     * Reload the permissions manager, and re-hook all permission plugins.
     *
     * @return True on success, false on failure.
     */
    public boolean reload() {
        // Unhook all permission plugins
        unhook();

        // Set up the permissions manager again, return the result
        setup();
        return true;
    }

    /**
     * Check whether a plugin is a permissions system plugin that is supported by the permissions manager.
     *
     * @param plugin The plugin to check.
     * @return True if the plugin is a supported permissions system, false if not.
     */
    public boolean isSupportedPlugin(Plugin plugin) {
        // Make sure the plugin isn't null
        if (plugin == null)
            return false;

        // Check whether this plugin is supported by it's name
        return isSupportedPlugin(plugin.getName());
    }

    /**
     * Check whether a plugin is supported by the permissions manager by it's plugin name.
     * The name of the plugin is case sensitive.
     *
     * @param pluginName The name of the plugin.
     * @return True if the plugin is supported, false if not.
     */
    public boolean isSupportedPlugin(String pluginName) {
        // Make sure the name isn't empty
        if (pluginName.trim().length() == 0)
            return false;

        // Loop through the list with permissions systems, and compare it's plugin name to the given name
        for (PermissionsSystemType type : PermissionsSystemType.values())
            if (type.getPluginName().equals(pluginName))
                return true;

        // This doesn't seem to be a supported permissions system plugin, return false
        return false;
    }

    /**
     * Method called when a plugin is being enabled.
     *
     * @param event Event instance.
     */
    public void onPluginEnable(PluginEnableEvent event) {
        // Get the plugin and it's name
        Plugin plugin = event.getPlugin();
        String pluginName = plugin.getName();

        // Check if any known permissions system is enabling
        if (isSupportedPlugin(plugin)) {
            this.log.info(pluginName + " plugin enabled, dynamically updating permissions hooks!");
            setup();
        }
    }

    /**
     * Method called when a plugin is being disabled.
     *
     * @param event Event instance.
     */
    public void onPluginDisable(PluginDisableEvent event) {
        // Get the plugin instance and name
        Plugin plugin = event.getPlugin();
        String pluginName = plugin.getName();

        // Is the WorldGuard plugin disabled
        if (isSupportedPlugin(plugin)) {
            this.log.info(pluginName + " plugin disabled, updating hooks!");
            setup();
        }
    }

    /**
     * Get the logger instance.
     *
     * @return Logger instance.
     */
    public Logger getLogger() {
        return this.log;
    }

    /**
     * Set the logger instance.
     *
     * @param log Logger instance.
     */
    public void setLogger(Logger log) {
        this.log = log;
    }

    /**
     * Check if the player has permission. If no permissions system is used, the player has to be OP.
     *
     * @param player    The player.
     * @param permsNode Permissions node.
     * @return True if the player has permission.
     */
    public boolean hasPermission(Player player, String permsNode) {
        return hasPermission(player, permsNode, player.isOp());
    }

    /**
     * Check if a player has permission.
     *
     * @param player    The player.
     * @param permsNode The permission node.
     * @param def       Default returned if no permissions system is used.
     * @return True if the player has permission.
     */
    public boolean hasPermission(Player player, String permsNode, boolean def) {
        // If no permissions system is used, return the default value
        if (!isEnabled())
            return def;

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return def;

        // Use the proper API
        switch (this.permsType) {
            case PERMISSIONS_EX:
                // Permissions Ex
                PermissionUser user = PermissionsEx.getUser(player);
                return user.has(permsNode);

            case PERMISSIONS_BUKKIT:
                // Permissions Bukkit
                return player.hasPermission(permsNode);

            case B_PERMISSIONS:
                // bPermissions
                return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), permsNode);

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                final AnjoPermissionsHandler handler = groupManagerPerms.getWorldsHolder().getWorldPermissions(player);
                return handler != null && handler.has(player, permsNode);

            case Z_PERMISSIONS:
                // zPermissions
                @SuppressWarnings("deprecation")
                Map<String, Boolean> perms = zPermissionsService.getPlayerPermissions(player.getWorld().getName(), null, player.getName());
                return perms.containsKey(permsNode) ? perms.get(permsNode) : def;

            case VAULT:
                // Vault
                return vaultPerms.has(player, permsNode);

            case PERMISSIONS:
                // Permissions
                return this.defaultPerms.has(player, permsNode);
        }

        // Failed, return the default
        return def;
    }

    /**
     * Check whether the current permissions system has group support.
     * If no permissions system is hooked, false will be returned.
     *
     * @return True if the current permissions system supports groups, false otherwise.
     */
    public boolean hasGroupSupport() {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return false;

        // Use the proper API
        switch (this.permsType) {
            case PERMISSIONS_EX:
            case PERMISSIONS_BUKKIT:
            case B_PERMISSIONS:
            case ESSENTIALS_GROUP_MANAGER:
            case Z_PERMISSIONS:
                return true;

            case VAULT:
                // Vault
                return vaultPerms.hasGroupSupport();

            case PERMISSIONS:
                // Legacy permissions
                // FIXME: Supported by plugin, but addGroup and removeGroup haven't been implemented correctly yet!
                return false;
        }

        // Failed return false
        return false;
    }

    /**
     * Get the permission groups of a player, if available.
     *
     * @param player The player.
     * @return Permission groups, or an empty list if this feature is not supported.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
    public List<String> getGroups(Player player) {
        // If no permissions system is used, return an empty list
        if (!isEnabled())
            return new ArrayList<>();

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return new ArrayList<>();

        // Use the proper API
        switch (this.permsType) {
            case PERMISSIONS_EX:
                // Permissions Ex
                PermissionUser user = PermissionsEx.getUser(player);
                return user.getParentIdentifiers(null);

            case PERMISSIONS_BUKKIT:
                // Permissions Bukkit
                // FIXME: Add support for this!
                return new ArrayList<>();

            case B_PERMISSIONS:
                // bPermissions
                return Arrays.asList(ApiLayer.getGroups(player.getWorld().getName(), CalculableType.USER, player.getName()));

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                final AnjoPermissionsHandler handler = groupManagerPerms.getWorldsHolder().getWorldPermissions(player);
                if (handler == null)
                    return new ArrayList<>();
                return Arrays.asList(handler.getGroups(player.getName()));

            case Z_PERMISSIONS:
                //zPermissions
                return new ArrayList(zPermissionsService.getPlayerGroups(player.getName()));

            case VAULT:
                // Vault
                return Arrays.asList(vaultPerms.getPlayerGroups(player));

            case PERMISSIONS:
                // Permissions
                // Create a list to put the groups in
                List<String> groups = new ArrayList<>();

                // Get the groups and add each to the list
                groups.addAll(this.defaultPerms.getGroups(player.getName()).stream().map(Group::getName).collect(Collectors.toList()));

                // Return the groups
                return groups;
        }

        // Failed, return an empty list
        return new ArrayList<>();
    }

    /**
     * Get the primary group of a player, if available.
     *
     * @param player The player.
     * @return The name of the primary permission group. Or null.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
    public String getPrimaryGroup(Player player) {
        // If no permissions system is used, return an empty list
        if (!isEnabled())
            return null;

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return null;

        // Use the proper API
        switch (this.permsType) {
            case PERMISSIONS_EX:
            case PERMISSIONS_BUKKIT:
            case B_PERMISSIONS:
            case PERMISSIONS: // FIXME: Is this correct for PERMISSIONS?
                // Get the groups of the player
                List<String> groups = getGroups(player);

                // Make sure there is any group available, or return null
                if (groups.size() == 0)
                    return null;

                // Return the first group
                return groups.get(0);

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                final AnjoPermissionsHandler handler = groupManagerPerms.getWorldsHolder().getWorldPermissions(player);
                if (handler == null)
                    return null;
                return handler.getGroup(player.getName());

            case Z_PERMISSIONS:
                //zPermissions
                return zPermissionsService.getPlayerPrimaryGroup(player.getName());

            case VAULT:
                // Vault
                return vaultPerms.getPrimaryGroup(player);
        }

        // Failed, return null
        return null;
    }

    /**
     * Check whether the player is in the specified group.
     *
     * @param player    The player.
     * @param groupName The group name.
     * @return True if the player is in the specified group, false otherwise.
     * False is also returned if groups aren't supported by the used permissions system.
     */
    public boolean inGroup(Player player, String groupName) {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return false;

        // Use the proper API
        switch (this.permsType) {
            case PERMISSIONS_EX:
                // Permissions Ex
                PermissionUser user = PermissionsEx.getUser(player);
                return user.inGroup(groupName);

            case PERMISSIONS_BUKKIT:
            case Z_PERMISSIONS:
                // Get the current list of groups
                List<String> groupNames = getGroups(player);

                // Check whether the list contains the group name, return the result
                for (String entry : groupNames)
                    if (entry.equals(groupName))
                        return true;
                return false;

            case B_PERMISSIONS:
                // bPermissions
                return ApiLayer.hasGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groupName);

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                final AnjoPermissionsHandler handler = groupManagerPerms.getWorldsHolder().getWorldPermissions(player);
                return handler != null && handler.inGroup(player.getName(), groupName);

            case VAULT:
                // Vault
                return vaultPerms.playerInGroup(player, groupName);

            case PERMISSIONS:
                // Permissions
                return this.defaultPerms.inGroup(player.getWorld().getName(), player.getName(), groupName);
        }

        // Failed, return false
        return false;
    }

    /**
     * Add the permission group of a player, if supported.
     *
     * @param player    The player
     * @param groupName The name of the group.
     * @return True if succeed, false otherwise.
     * False is also returned if this feature isn't supported for the current permissions system.
     */
    public boolean addGroup(Player player, String groupName) {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return false;

        // Set the group the proper way
        switch (this.permsType) {
            case PERMISSIONS_EX:
                // Permissions Ex
                PermissionUser user = PermissionsEx.getUser(player);
                user.addGroup(groupName);
                return true;

            case PERMISSIONS_BUKKIT:
                // Permissions Bukkit
                // Add the group to the user using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player addgroup " + player.getName() + " " + groupName);

            case B_PERMISSIONS:
                // bPermissions
                ApiLayer.addGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groupName);
                return true;

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                // Add the group to the user using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manuaddsub " + player.getName() + " " + groupName);

            case Z_PERMISSIONS:
                // zPermissions
                // Add the group to the user using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player " + player.getName() + " addgroup " + groupName);

            case VAULT:
                // Vault
                vaultPerms.playerAddGroup(player, groupName);
                return true;

            case PERMISSIONS:
                // Permissions
                // FIXME: Add this method!
                //return this.defaultPerms.group
                return false;
        }

        // Failed, return false
        return false;
    }

    /**
     * Add the permission groups of a player, if supported.
     *
     * @param player     The player
     * @param groupNames The name of the groups to add.
     * @return True if succeed, false otherwise.
     * False is also returned if this feature isn't supported for the current permissions system.
     */
    public boolean addGroups(Player player, List<String> groupNames) {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Add each group to the user
        boolean result = true;
        for (String groupName : groupNames)
            if (!addGroup(player, groupName))
                result = false;

        // Return the result
        return result;
    }

    /**
     * Remove the permission group of a player, if supported.
     *
     * @param player    The player
     * @param groupName The name of the group.
     * @return True if succeed, false otherwise.
     * False is also returned if this feature isn't supported for the current permissions system.
     */
    public boolean removeGroup(Player player, String groupName) {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return false;

        // Set the group the proper way
        switch (this.permsType) {
            case PERMISSIONS_EX:
                // Permissions Ex
                PermissionUser user = PermissionsEx.getUser(player);
                user.removeGroup(groupName);
                return true;

            case PERMISSIONS_BUKKIT:
                // Permissions Bukkit
                // Remove the group to the user using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player removegroup " + player.getName() + " " + groupName);

            case B_PERMISSIONS:
                // bPermissions
                ApiLayer.removeGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groupName);
                return true;

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                // Remove the group to the user using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manudelsub " + player.getName() + " " + groupName);

            case Z_PERMISSIONS:
                // zPermissions
                // Remove the group to the user using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player " + player.getName() + " removegroup " + groupName);

            case VAULT:
                // Vault
                vaultPerms.playerRemoveGroup(player, groupName);
                return true;

            case PERMISSIONS:
                // Permissions
                // FIXME: Add this method!
                //return this.defaultPerms.group
                return false;
        }

        // Failed, return false
        return false;
    }

    /**
     * Remove the permission groups of a player, if supported.
     *
     * @param player     The player
     * @param groupNames The name of the groups to add.
     * @return True if succeed, false otherwise.
     * False is also returned if this feature isn't supported for the current permissions system.
     */
    public boolean removeGroups(Player player, List<String> groupNames) {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Add each group to the user
        boolean result = true;
        for(String groupName : groupNames)
            if(!removeGroup(player, groupName))
                result = false;

        // Return the result
        return result;
    }

    /**
     * Set the permission group of a player, if supported.
     * This clears the current groups of the player.
     *
     * @param player    The player
     * @param groupName The name of the group.
     * @return True if succeed, false otherwise.
     * False is also returned if this feature isn't supported for the current permissions system.
     */
    public boolean setGroup(Player player, String groupName) {
        // If no permissions system is used, return false
        if (!isEnabled())
            return false;

        // Create a list of group names
        List<String> groupNames = new ArrayList<>();
        groupNames.add(groupName);

        // Make sure we're hooked into a permissions system
        if(this.permsType == null)
            return false;

        // Set the group the proper way
        switch (this.permsType) {
            case PERMISSIONS_EX:
                // Permissions Ex
                PermissionUser user = PermissionsEx.getUser(player);
                user.setParentsIdentifier(groupNames);
                return true;

            case PERMISSIONS_BUKKIT:
                // Permissions Bukkit
                // Set the user's group using a command
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player setgroup " + player.getName() + " " + groupName);

            case B_PERMISSIONS:
                // bPermissions
                ApiLayer.setGroup(player.getWorld().getName(), CalculableType.USER, player.getName(), groupName);
                return true;

            case ESSENTIALS_GROUP_MANAGER:
                // Essentials Group Manager
                // Clear the list of groups, add the player to the specified group afterwards using a command
                removeAllGroups(player);
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manuadd " + player.getName() + " " + groupName);

            case Z_PERMISSIONS:
                //zPermissions
                // Set the players group through the plugin commands
                return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "permissions player " + player.getName() + " setgroup " + groupName);

            case VAULT:
                // Vault
                // Remove all current groups, add the player to the specified group afterwards
                removeAllGroups(player);
                vaultPerms.playerAddGroup(player, groupName);
                return true;

            case PERMISSIONS:
                // Permissions
                // FIXME: Add this method!
                //return this.defaultPerms.group
                return false;
        }

        // Failed, return false
        return false;
    }

    /**
     * Set the permission groups of a player, if supported.
     * This clears the current groups of the player.
     *
     * @param player     The player
     * @param groupNames The name of the groups to set.
     * @return True if succeed, false otherwise.
     * False is also returned if this feature isn't supported for the current permissions system.
     */
    public boolean setGroups(Player player, List<String> groupNames) {
        // If no permissions system is used or if there's no group supplied, return false
        if (!isEnabled() || groupNames.size() <= 0)
            return false;

        // Set the main group
        if (!setGroup(player, groupNames.get(0)))
            return false;

        // Add the rest of the groups
        boolean result = true;
        for (int i = 1; i < groupNames.size(); i++) {
            // Get the group name
            String groupName = groupNames.get(0);

            // Add this group
            if (!addGroup(player, groupName))
                result = false;
        }

        // Return the result
        return result;
    }

    /**
     * Remove all groups of the specified player, if supported.
     * Systems like Essentials GroupManager don't allow all groups to be removed from a player, thus the user will stay
     * in it's primary group. All the subgroups are removed just fine.
     *
     * @param player The player to remove all groups from.
     * @return True if succeed, false otherwise.
     * False will also be returned if this feature isn't supported for the used permissions system.
     */
    public boolean removeAllGroups(Player player) {
        // If no permissions system is used, return false
        if(!isEnabled())
            return false;

        // Get a list of current groups
        List<String> groupNames = getGroups(player);

        // Remove each group
        return removeGroups(player, groupNames);
    }

    /**
     * The various permission system types.
     * This is used to identify all the permission system types that are supported by the permissions manager.
     */
    public enum PermissionsSystemType {

        /**
         * Permissions Ex.
         */
        PERMISSIONS_EX("PermissionsEx", "PermissionsEx"),

        /**
         * Permissions Bukkit.
         */
        PERMISSIONS_BUKKIT("Permissions Bukkit", "PermissionsBukkit"),

        /**
         * bPermissions.
         */
        B_PERMISSIONS("bPermissions", "bPermissions"),

        /**
         * Essentials Group Manager.
         */
        ESSENTIALS_GROUP_MANAGER("Essentials Group Manager", "GroupManager"),

        /**
         * zPermissions.
         */
        Z_PERMISSIONS("zPermissions", "zPermissions"),

        /**
         * Vault.
         */
        VAULT("Vault", "Vault"),

        /**
         * Permissions.
         */
        PERMISSIONS("Permissions", "Permissions");

        /**
         * The display name of the permissions system.
         */
        public String name;

        /**
         * The name of the permissions system plugin.
         */
        public String pluginName;

        /**
         * Constructor for PermissionsSystemType.
         *
         * @param name       Display name of the permissions system.
         * @param pluginName Name of the plugin.
         */
        PermissionsSystemType(String name, String pluginName) {
            this.name = name;
            this.pluginName = pluginName;
        }

        /**
         * Get the display name of the permissions system.
         *
         * @return Display name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Return the plugin name.
         *
         * @return Plugin name.
         */
        public String getPluginName() {
            return this.pluginName;
        }
    }
}
