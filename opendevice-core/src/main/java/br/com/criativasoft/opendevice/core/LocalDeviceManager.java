/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.connection.InputContections;
import br.com.criativasoft.opendevice.core.connection.OutputConnections;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.dao.memory.LocalDataManager;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Use this class to develop local applications, local servers or clients for OpenDevice.<br/><br/>
 *
 * <p>You should override the method {@link #start()} and create main method:</p>
 * <code>
 *    public static void main(String[] args) { launch(args); }
 * </code>
 *
 * @see <a href="https://opendevice.atlassian.net/wiki/display/DOC/Getting+started">Getting Started</a> for more info.
 * @author Ricardo JL Rufino
 * @date 24/08/14.
 */
public class LocalDeviceManager extends BaseDeviceManager {

    protected static final Logger log = LoggerFactory.getLogger(LocalDeviceManager.class);

    // ToDo: use only if dao is not memory
    private Set<Device> runtimeDevices = new LinkedHashSet<Device>();

    // Aliases to factory connections
    protected OutputConnections out = Connections.out;
    protected InputContections in = Connections.in;


    public LocalDeviceManager(){
        super();
        setDataManager(new LocalDataManager());
        TenantProvider.setProvider(new LocalTenantProvider());
    }



    public void setApiKey(String key) {
        TenantProvider.setCurrentID(key);
    }

    /**
     * @deprecated use setApiKey
     * @param applicationID
     */
    public void setApplicationID(String applicationID) {
        setApiKey(applicationID);
    }

    public String getApplicationID() {
        return TenantProvider.getCurrentID();
    }


    @Override
    public void addInput(DeviceConnection connection) {
        if(connection.getApplicationID() == null) {
            connection.setApplicationID(getApplicationID());
        }
        super.addInput(connection);
    }

    @Override
    public void addOutput(DeviceConnection connection) {
        if(connection.getApplicationID() == null) {
            connection.setApplicationID(getApplicationID());
        }
        super.addOutput(connection);
    }

    @Override
    public void send(Command command) throws IOException {
        if(command.getApplicationID() == null){
            command.setApplicationID(getApplicationID());
        }
        super.send(command);
    }

    @Override
    public Collection<Device> getDevices() {

        Set<Device> devices = new LinkedHashSet<Device>();
        Collection<Device> dblist = super.getDevices();

        devices.addAll(runtimeDevices); // TODO
        devices.addAll(dblist); // TODO: this must be enabled (comentado devido a duplicidade nos testes)

        return devices;
    }


    /**
     * This method is called from {@link Device} constructor, to auto-register devices if enabled
     * @see OpenDeviceConfig#setAutoRegisterLocalDevice(boolean)
     * @param device
     */
    public void autoRegisterDevice(Device device) {

        if(getConfig().isAutoRegisterLocalDevice()){

            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            int maxint = 10;

            boolean register = false;
            // Check if this method has called by a "New Device" in Constructor of a LocalDeviceManager
            // This avoids auto-register devices loaded from a Connections/Serializers

            for (int i = 0; i < maxint; i++) {
                StackTraceElement stackTraceElement = stack[i];
                String className = stackTraceElement.getClassName();
                String methodName = stackTraceElement.getMethodName();
                try {
                    Class<?> aClass = Class.forName(className);

                    if(LocalDeviceManager.class.isAssignableFrom(aClass) && ( methodName.equals("<init>") || methodName.equals("start"))){
                        register = true;
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    //
                }
            }

            if(register){
                log.info("Registring the device in context : " + device + " ! (may slow down device initialization )");
                addDevice(device);
            }
        }
    }


    @Override
    public void addDevice(Device device) {
        super.addDevice(device);

        if(findDeviceByUID(device.getUid()) == null) {
            runtimeDevices.add(device);
        }
    }

    /**
     * Alias to {@link #findDeviceByUID(int)}
     * @param deviceUID
     * @return Device
     */
    public Device findDevice(int deviceUID) {
        return findDeviceByUID(deviceUID);
    }

    @Override
    public Device findDeviceByUID(int deviceUID) {

        if(!runtimeDevices.isEmpty()){
            for (Device runtimeDevice : runtimeDevices) {
                if(runtimeDevice.getUid() == deviceUID){
                    return runtimeDevice;
                }
            }
        }

        return super.findDeviceByUID(deviceUID);
    }

    @Override
    public DeviceDao getValidDeviceDao() {

        if(TenantProvider.getCurrentID() == null){
            TenantProvider.setCurrentID(getApplicationID());
        }

        // Check if tenant is valid.
        if(TenantProvider.getCurrentID() != null && getConfig().isTenantsEnabled() && OpenDeviceConfig.LOCAL_APP_ID.equals(TenantProvider.getCurrentID())){
            throw new IllegalStateException("In Multi-Tenant support don't allow '*' in applicationID !");
        }

        return super.getValidDeviceDao();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public static void launch(String... args) {
        // Figure out the right class to call
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        boolean foundThisMethod = false;
        String callingClassName = null;
        for (StackTraceElement se : stack) {
            // Skip entries until we get to the entry for this class
            String className = se.getClassName();
            String methodName = se.getMethodName();
            // System.out.println("-className" + className + ", methodName: " + methodName);
            if (foundThisMethod) {
                callingClassName = className;
                break;
            } else if ("launch".equals(methodName)) {
                foundThisMethod = true;
            }
        }

        if (callingClassName == null) {
            throw new RuntimeException("Error: unable to determine Application class");
        }

        try {
            Class theClass = Class.forName(callingClassName, true,
                    Thread.currentThread().getContextClassLoader());
            if (LocalDeviceManager.class.isAssignableFrom(theClass)) {
                Class<? extends LocalDeviceManager> appClass = theClass;
                launchApplication(appClass, args);
            } else {
                throw new RuntimeException("Error: " + theClass + " is not a subclass of " + LocalDeviceManager.class.getName());
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void launchApplication(Class<? extends LocalDeviceManager> appClass, String... args) {
        try {
            final LocalDeviceManager main = appClass.newInstance();
            main.start();

            // Automatic shutdown
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    main.stop();
                }
            });

            // Manual shutdown
            log.info("========================================================");
            log.info("Application - started on port: " + OpenDeviceConfig.get().getPort());
            log.info("Type [CTRL+C] to stop the server");
            log.info("========================================================");

            while(true){
                Thread.sleep(60000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(-1);
    }


    /**
     * This method is called automatically when you start the application without using a main method.
     * @throws IOException
     */
    public void start() throws IOException {
        log.info("Method start not implemented");
    }

}
