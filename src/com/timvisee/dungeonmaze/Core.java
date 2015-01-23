package com.timvisee.dungeonmaze;

import com.timvisee.dungeonmaze.api.ApiController;
import com.timvisee.dungeonmaze.api.OldApiController;
import com.timvisee.dungeonmaze.api.OldApiControllerService;
import com.timvisee.dungeonmaze.command.CommandHandler;
import com.timvisee.dungeonmaze.command.CommandHandlerService;
import com.timvisee.dungeonmaze.config.ConfigHandler;
import com.timvisee.dungeonmaze.listener.EventListenerManager;
import com.timvisee.dungeonmaze.logger.LoggerManager;
import com.timvisee.dungeonmaze.permission.PermissionsManager;
import com.timvisee.dungeonmaze.service.*;
import com.timvisee.dungeonmaze.api.ApiControllerService;
import com.timvisee.dungeonmaze.config.ConfigHandlerService;
import com.timvisee.dungeonmaze.plugin.metrics.MetricsController;
import com.timvisee.dungeonmaze.plugin.metrics.MetricsControllerService;
import com.timvisee.dungeonmaze.structure.CustomStructureManagerService;
import com.timvisee.dungeonmaze.listener.EventListenerManagerService;
import com.timvisee.dungeonmaze.logger.LoggerService;
import com.timvisee.dungeonmaze.plugin.multiverse.MultiverseHandler;
import com.timvisee.dungeonmaze.plugin.multiverse.MultiverseHandlerService;
import com.timvisee.dungeonmaze.permission.PermissionsManagerService;
import com.timvisee.dungeonmaze.update.UpdateCheckerService;
import com.timvisee.dungeonmaze.world.WorldManagerService;
import com.timvisee.dungeonmaze.structure.CustomStructureManager;
import com.timvisee.dungeonmaze.update.Updater;
import com.timvisee.dungeonmaze.world.WorldManager;

import java.util.logging.Logger;

@SuppressWarnings("UnusedDeclaration")
public class Core {

    // TODO: Do we need to keep a non-static version of each method available?

    /** Static Core instance. */
    public static Core instance;

    /** Defines whether the core has been initialized. */
    private boolean init = false;

    /** Service manager instance. */
    private ServiceManager serviceManager;

    /** Logger service instance. */
    private LoggerService loggerService = new LoggerService();
    /** Config handler service instance. */
    private ConfigHandlerService configHandlerService = new ConfigHandlerService();
    /** Command handler service instance. */
    private CommandHandlerService commandHandlerService = new CommandHandlerService();
    /** World manager service instance. */
    private WorldManagerService worldManagerService = new WorldManagerService();
    /** Update checker service instance. */
    private UpdateCheckerService updateCheckerService = new UpdateCheckerService();
    /** Permissions manager service instance. */
    private PermissionsManagerService permissionsManagerService = new PermissionsManagerService();
    /** Multiverse handler service instance. */
    private MultiverseHandlerService multiverseHandlerService = new MultiverseHandlerService();
    /** Custom structure manager service instance. */
    private CustomStructureManagerService customStructureManagerService = new CustomStructureManagerService();
    /** API Controller service instance. */
    private ApiControllerService apiControllerService = new ApiControllerService();
    /** Old API Controller service instance. */
    private OldApiControllerService oldApiControllerService = new OldApiControllerService();
    /** Event listener manager service instance. */
    private EventListenerManagerService eventListenerManagerService = new EventListenerManagerService();
    /** Metrics controller service instance. */
    private MetricsControllerService metricsControllerService = new MetricsControllerService();

    /**
     * Constructor.
     *
     * @param init True to immediately initialize.
     */
    public Core(boolean init) {
        // Set the static instance
        Core.instance = this;

        // Initialize
        if(init)
            this.init();
    }

    /**
     * Initialize the core.
     * This method will initialize and set up all handlers and managers used by the core.
     *
     * @return True on success, false on failure.
     */
    public boolean init() {
        // Make sure the Core hasn't been instantiated already
        if(this.isInit())
            return true;

        // Initialize the service manager
        this.serviceManager = new ServiceManager();

        // Register all services
        this.serviceManager.unregisterAllServices();
        this.serviceManager.registerService(this.loggerService);
        this.serviceManager.registerService(this.configHandlerService);
        this.serviceManager.registerService(this.commandHandlerService);
        this.serviceManager.registerService(this.multiverseHandlerService);
        this.serviceManager.registerService(this.worldManagerService);
        this.serviceManager.registerService(this.updateCheckerService);
        this.serviceManager.registerService(this.permissionsManagerService);
        this.serviceManager.registerService(this.customStructureManagerService);
        this.serviceManager.registerService(this.apiControllerService);
        this.serviceManager.registerService(this.oldApiControllerService);
        this.serviceManager.registerService(this.eventListenerManagerService);
        this.serviceManager.registerService(this.metricsControllerService);

        // Initialize all services
        if(!this.serviceManager.initServices())
            return false;

        // Everything has been set up successfully, return the result
        this.init = true;
        return true;
    }

    /**
     * Destroy all services.
     *
     * @param force True to force to destroy all services, even if one service couldn't be destroyed. If force mode is
     *              used all service states will be set to destroyed even if the destruction failed.
     *
     * @return True on success, false on failure.
     */
    public boolean destroy(boolean force) {
        // Make sure the core is initialized, or the method must be forced
        if(!this.isInit() && !force)
            return true;

        // Destroy all services
        if(!this.serviceManager.destroyServices(force))
            if(!force)
                return false;

        // Return the result
        this.init = false;
        return true;
    }

    /**
     * Check whether the Core has been initialized.
     *
     * @return True if the core has been initialized, false otherwise.
     */
    public boolean isInit() {
        return this.init;
    }

    /**
     * Get the service manager.
     *
     * @return Service manager instance.
     */
    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    /**
     * Get the logger manager.
     *
     * @return Logger manager instance.
     */
    public static LoggerManager getLoggerManager() {
        return Core.instance._getLoggerManager();
    }

    /**
     * Get the logger manager.
     *
     * @return Logger manager instance.
     */
    public LoggerManager _getLoggerManager() {
        return this.loggerService.getLoggerManager();
    }

    /**
     * Get the logger.
     *
     * @return Logger instance.
     */
    public static Logger getLogger() {
        return Core.instance._getLogger();
    }
    
    /**
     * Get the logger.
     *
     * @return Logger instance.
     */
    public Logger _getLogger() {
        /// Make sure the logger service is initialized
        if(this.loggerService == null)
            return DungeonMaze.instance.getLogger();

        // Get and return the logger
        return this.loggerService.getLogger();
    }

    /**
     * Get the config handler.
     *
     * @return Config handler instance.
     */
    public static ConfigHandler getConfigHandler() {
        return Core.instance._getConfigHandler();
    }

    /**
     * Get the config handler.
     *
     * @return Config handler instance.
     */
    public ConfigHandler _getConfigHandler() {
        return this.configHandlerService.getConfigHandler();
    }

    /**
     * Get the command handler.
     *
     * @return Command handler instance.
     */
    public static CommandHandler getCommandHandler() {
        return Core.instance._getCommandHandler();
    }

    /**
     * Get the command handler.
     *
     * @return Command handler instance.
     */
    public CommandHandler _getCommandHandler() {
        return this.commandHandlerService.getCommandHandler();
    }

    /**
     * Get the multiverse handler.
     *
     * @return Multiverse handler instance.
     */
    public static MultiverseHandler getMultiverseHandler() {
        return Core.instance._getMultiverseHandler();
    }

    /**
     * Get the multiverse handler.
     *
     * @return Multiverse handler instance.
     */
    public MultiverseHandler _getMultiverseHandler() {
        return this.multiverseHandlerService.getMultiverseHandler();
    }

    /**
     * Get the world manager.
     *
     * @return World manager instance.
     */
    public static WorldManager getWorldManager() {
        return Core.instance._getWorldManager();
    }

    /**
     * Get the world manager.
     *
     * @return World manager instance.
     */
    public WorldManager _getWorldManager() {
        return this.worldManagerService.getWorldManager();
    }

    /**
     * Get the update checker.
     *
     * @return Update checker instance.
     */
    public static Updater getUpdateChecker() {
        return Core.instance._getUpdateChecker();
    }

    /**
     * Get the update checker.
     *
     * @return Update checker instance.
     */
    public Updater _getUpdateChecker() {
        return this.updateCheckerService.getUpdateChecker();
    }

    /**
     * Get the permissions manager.
     *
     * @return Permissions manager instance.
     */
    public static PermissionsManager getPermissionsManager() {
        return Core.instance._getPermissionsManager();
    }

    /**
     * Get the permissions manager.
     *
     * @return Permissions manager instance.
     */
    public PermissionsManager _getPermissionsManager() {
        return this.permissionsManagerService.getPermissionsManager();
    }

    /**
     * Get the custom structure manager.
     *
     * @return Custom structure manager instance.
     */
    public static CustomStructureManager getCustomStructureManager() {
        return Core.instance._getCustomStructureManager();
    }

    /**
     * Get the custom structure manager.
     *
     * @return Custom structure manager instance.
     */
    public CustomStructureManager _getCustomStructureManager() {
        return this.customStructureManagerService.getCustomStructureManager();
    }

    /**
     * Get the API Controller.
     *
     * @return API Controller instance.
     */
    public static ApiController getApiController() {
        return Core.instance._getApiController();
    }

    /**
     * Get the API Controller.
     *
     * @return API Controller instance.
     */
    public ApiController _getApiController() {
        return this.apiControllerService.getApiController();
    }

    /**
     * Get the old API Controller.
     *
     * @return Old API Controller instance.
     */
    public static OldApiController getOldApiController() {
        return Core.instance._getOldApiController();
    }

    /**
     * Get the old API Controller.
     *
     * @return Old API Controller instance.
     */
    public OldApiController _getOldApiController() {
        return this.oldApiControllerService.getApiController();
    }

    /**
     * Get the event listener manager.
     *
     * @return Event listener manager instance.
     */
    public static EventListenerManager getEventListenerManager() {
        return Core.instance._getEventListenerManager();
    }

    /**
     * Get the event listener manager.
     *
     * @return Event listener manager instance.
     */
    public EventListenerManager _getEventListenerManager() {
        return this.eventListenerManagerService.getEventListenerManager();
    }

    /**
     * Get the metrics controller.
     *
     * @return Metrics controller instance.
     */
    public static MetricsController getMetricsController() {
        return Core.instance._getMetricsController();
    }

    /**
     * Get the metrics controller.
     *
     * @return Metrics controller instance.
     */
    public MetricsController _getMetricsController() {
        return this.metricsControllerService.getMetricsController();
    }
}
