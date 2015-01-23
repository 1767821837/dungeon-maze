package com.timvisee.dungeonmaze.service;

import com.timvisee.dungeonmaze.Core;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class ServiceManager {

    /** The list of services. */
    private List<Service> services = new ArrayList<Service>();

    /**
     * Constructor.
     */
    public ServiceManager() { }

    /**
     * Get a service by it's index.
     * Alias of getService();
     *
     * @param i The index of the service to get.
     *
     * @return The service, or null if the index is out of bound.
     */
    public Service get(int i) {
        return this.getService(i);
    }

    /**
     * Get a service by it's index.
     *
     * @param i The index of the service to get.
     *
     * @return The service, or null if the index is out of bound.
     */
    public Service getService(int i) {
        // Make sure the index is valid
        if(i >= this.getServiceCount() || i < 0)
            return null;

        // Get and return the service
        return this.services.get(i);
    }

    /**
     * Get the list of available services.
     *
     * @return The list of available services.
     */
    public List<Service> getServices() {
        return this.services;
    }

    /**
     * Get the number of services.
     *
     * @return Number of services.
     */
    public int getServiceCount() {
        return this.services.size();
    }

    /**
     * Register a service without initializing it.
     *
     * @param service The service to register.
     *
     * @return True if succeed, false otherwise.
     */
    public boolean registerService(Service service) {
        return this.registerService(service, false);
    }

    /**
     * Register a service.
     *
     * @param service The service to register.
     * @param init True to immediately initialize the service.
     *
     * @return True if succeed, false otherwise.
     */
    public boolean registerService(Service service, boolean init) {
        // Register the service, return false on failure
        if(!this.services.add(service))
            return false;

        // Check whether we should initialize the service, return the result
        return !init || service.init();
    }

    /**
     * Unregister all services.
     */
    public void unregisterAllServices() {
        this.services.clear();
    }

    /**
     * Initialize all services.
     *
     * @return True on success, false on failure.
     */
    public boolean initServices() {
        // Initialize each service
        for(int i = 0; i < this.getServiceCount(); i++) {
            // Get the current service
            Service m = this.get(i);

            // Initialize the service
            if(!m.init()) {
                // TODO: Improve this error message!
                Core.getLogger().info("[Service] Load: " + m.getName() + " FAILED!");
                return false;
            }

            // Show a status message
            // TODO: Should we remove this message?
            Core.getLogger().info("[Service] Load: " + m.getName() + " SUCCESS!");
        }

        // Every service was initialized successfully, return the result
        return true;
    }

    /**
     * Destroy all services.
     *
     * @param force True to force to destroy every service, this will destroy all services even if one failed to destroy.
     *
     * @return True on success, false on failure. If force is set to true false might still be returned even though
     * all services are destroyed.
     */
    public boolean destroyServices(boolean force) {
        // Set whether the destruction failed
        boolean failed = false;

        // Destroy each service
        for(int i = this.getServiceCount() - 1; i >= 0; i--) {
            // Get the current service
            Service m = this.get(i);

            // Destroy the service
            if(!m.destroy(force)) {
                // Set the failed state
                failed = true;

                // TODO: Improve this error message!
                Core.getLogger().info("[Service] Unload: " + m.getName() + " FAILED!");

                // Return false if the force mode isn't used
                if(!force)
                    return false;
            }

            // Show a status message
            // TODO: Should we remove this message?
            Core.getLogger().info("[Service] Unload: " + m.getName() + " SUCCESS!");
        }

        // Every service was initialized successfully, return the result
        return !failed;
    }
}
