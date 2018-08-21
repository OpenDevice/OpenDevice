/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.middleware;

import br.com.criativasoft.opendevice.core.*;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.middleware.model.IAccountEntity;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Create TenantContext using MapDB, with a expiration cache system. <br/>
 * Calls to getDeviceByUID/getDevices use this cache to improve performance.
 *
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
public class MainTenantProvider extends ThreadLocalTenantProvider {

    private static final Logger log = LoggerFactory.getLogger(MainTenantContext.class);

    private DataManager dataManager;

    private LoadingCache<String, TenantContext> cache;   // Used to track active tenants

    public MainTenantProvider(DataManager dataManager) {
        this.dataManager = dataManager;

        CacheLoader<String, TenantContext> loader = new CacheLoader<String, TenantContext>() {
            @Override
            public TenantContext load(String key) {
                log.info("Re-loading Tenant: " + key);
                MainTenantContext tenantContext = (MainTenantContext) getTenantContext(key);
                tenantContext.syncronizeData();
                return tenantContext;
            }
        };

        // Unload cached data
        RemovalListener<String, TenantContext> listener = n -> {
            if (n.wasEvicted()) {
                TenantContext tenant = n.getValue();
                log.info("Expiring Tenant: " + n.getKey());
                tenant.cleanUp();
            }
        };


        // Used to track active tenants
        // https://github.com/google/guava/wiki/CachesExplained
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .weakValues()
                .removalListener(listener)
                .build(loader);
    }

    @Override
    protected TenantContext createContext(String id) {
        MainTenantContext tenantContext = new MainTenantContext(id);
        cache.put(id, tenantContext);
        return tenantContext;
    }

    @Override
    public TenantContext getTenantContext() {

        TenantContext tenantContext = super.getTenantContext();

        if(tenantContext != null){
            cache.getUnchecked(tenantContext.getId()); // Force KeepAlive cache...
        }

        return tenantContext;
    }

    @Override
    public void setTenantID(String appID) {
        if(appID != null && !exist(appID)) throw new IllegalArgumentException("AppID/TenantID does not exist !");
        super.setTenantID(appID);
    }

    @Override
    public void cleanUp() {
        cache.cleanUp();
    }

    private class MainTenantContext implements TenantContext{

        private String id;

        private DeviceManager manager;

        private Map<Integer, Device> cache = new ConcurrentHashMap<>();

        private boolean loaded = false;

        public MainTenantContext(String id) {
            this.id = id;
            this.manager = ODev.getDeviceManager();
        }

        @Override
        public void cleanUp() {
            cache.clear();
            loaded = false;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void addDevice(Device device) {
            if(!loaded) syncronizeData(); // force load/reload
            cache.put(device.getUid(), device);
        }

        @Override
        public void updateDevice(Device device) {
            cache.put(device.getUid(), device);
        }

        @Override
        public void removeDevice(Device device) {
            if(loaded) cache.remove(device.getUid());
        }

        @Override
        public Device getDeviceByUID(int uid) {
            if(!loaded) syncronizeData(); // force load

            try {
                return cache.get(uid);
            }catch (Exception ex){
                return null;
            }

        }
        @Override
        public Device getDeviceByName(String name) {
            if(!loaded) syncronizeData(); // force load

            try {

                Collection<Device> devices = getDevices();

                for (Device device : devices) {
                    if(device.getName() != null && device.getName().equals(name)) return device;
                }

            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }

            return null;
        }

        @Override
        public Collection<Device> getDevices() {

            if(!loaded){
                syncronizeData();
            }

            return cache.values();

        }

        private void syncronizeData(){
            // Load from database
            List<Device> devices = dataManager.getDeviceDao().listAll();
            for (Device device : devices) {
                manager.bindDevice(device);
                cache.put(device.getUid(), device);
            }

            loaded = true;
        }
    }

    public static boolean validadeEntity(IAccountEntity entity){
        return entity != null && !entity.getAccount().getUuid().equals( TenantProvider.getCurrentID());
    }
}
