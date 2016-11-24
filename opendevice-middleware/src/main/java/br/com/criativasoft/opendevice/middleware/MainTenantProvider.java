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

import br.com.criativasoft.opendevice.core.DataManager;
import br.com.criativasoft.opendevice.core.TenantContext;
import br.com.criativasoft.opendevice.core.ThreadLocalTenantProvider;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.model.Device;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create TenantContext using MapDB, with a expiration cache system. <br/>
 * Calls to getDeviceByUID/getDevices use this cache to improve performance.
 *
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
public class MainTenantProvider extends ThreadLocalTenantProvider {

    private DataManager dataManager;

    public MainTenantProvider(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    protected TenantContext createContext(String id) {
        return new MainTenantContext(id);
    }

    @Override
    public void setTenantID(String appID) {
        if(appID != null && !exist(appID)) throw new IllegalArgumentException("AppID/TenantID does not exist !");
        super.setTenantID(appID);
    }

    private class MainTenantContext implements TenantContext{

        private final Logger log = LoggerFactory.getLogger(MainTenantContext.class);

        private String id;

        HTreeMap<Integer, Device> map;

        private Set<Integer> expired = new HashSet();

        private boolean initialized = false;

        public MainTenantContext(String id) {
            this.id = id;

            DB dbMemory = DBMaker
                    .newMemoryDB()
                    .make();

            map = dbMemory.createHashMap("devices-" + id)
                    .counterEnable()
                    .expireAfterAccess(5, TimeUnit.HOURS)
                    .valueSerializer(Serializer.JAVA)
                    .keySerializer(Serializer.INTEGER)

                    .valueCreator(new Fun.Function1<Device, Integer>() {
                        @Override
                        public Device run(Integer integer) {
                            if(expired.contains(integer)){
                                DeviceDao deviceDao = dataManager.getDeviceDao();
                                expired.remove(integer);
                                return deviceDao.getByUID(integer);
                            }else{
                                // FIXME: Bug in MapDB 1.x, always calling this function (even if value has in map)
                                // SEE: https://github.com/andsel/moquette/issues/140#issuecomment-253308776
                                // This must be return NULL (in fact it will not be called)
                                DeviceDao deviceDao = dataManager.getDeviceDao();
                                return deviceDao.getByUID(integer);
                            }
                        }
                    }).make();

            map.modificationListenerAdd(new Bind.MapListener<Integer, Device>() {
                @Override
                public void update(Integer key, Device oldVal, Device newVal) {
                    if(newVal == null){
                        expired.add(key);
                        log.debug("Expiring Device with ID: " + key);
                    }
                }
            });

        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void addDevice(Device device) {
            if(!initialized) syncronizeData(); // force load
            map.put(device.getUid(), device);
        }

        @Override
        public void removeDevice(Device device) {
            if(initialized) map.remove(device.getUid());
        }

        @Override
        public Device getDeviceByUID(int uid) {
            if(!initialized) syncronizeData(); // force load

            try {
                return map.get(uid);
            }catch (Exception ex){
                return null;
            }

        }
        @Override
        public Device getDeviceByName(String name) {
            if(!initialized) syncronizeData(); // force load

            try {

                Collection<Device> devices = getDevices();

                for (Device device : devices) {
                    if(device.getName().equals(name)) return device;
                }

            }catch (Exception ex){
                log.error(ex.getMessage(), ex);
            }

            return null;
        }

        @Override
        public Collection<Device> getDevices() {

            if(!initialized || expired.size() > 0){
                syncronizeData();
            }

            return map.values();

        }

        private void syncronizeData(){
            // Load from database
            List<Device> devices = dataManager.getDeviceDao().listAll();
            for (Device device : devices) {
                map.put(device.getUid(), device);
            }

            expired.clear();
            initialized = true;
        }
    }


}
