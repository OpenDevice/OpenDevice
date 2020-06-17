/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.core.model;

import br.com.criativasoft.opendevice.core.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * OpenDevice configuration.
 *
 * @author Ricardo JL Rufino on 23/03/15.
 */
public class OpenDeviceConfig {

    private static final Logger log  = LoggerFactory.getLogger(OpenDeviceConfig.class);

    public static final String LOCAL_APP_ID = "*";

    // Profile
    public static final String PROFILE_KEY = "odev.profile";
    public static final String PROFILE_DEV = "dev";
    public static final String PROFILE_PROD = "production";

    public static final int DEFAULT_PORT = 8181;

    private static OpenDeviceConfig INSTANCE;

    private Properties properties;

    private Boolean bindLocalVariables; // as attribute for fast access.
    private Boolean remoteIDGeneration; // as attribute for fast access.


    public enum ConfigKey{
        web_port("web.port", null, DEFAULT_PORT),
        profile("profile", null, "dev"),
        log_config("log.config", null, "logback-dev.xml"),
        web_external_resources("web.external_resources", null, new ArrayList<String>()),
        startup_script("startup_script", null, null),
        tenants_enabled("tenants.enabled", null, false),
        broadcast_inputs("internal.broadcastInputs", null, true),
        database_enabled("database.enabled", null, false),
        database_path("database.path", null, null),
        database_engine("database.engine", null, null),
        mqtt_enabled("mqtt.enabled", null, false),
        auth_enabled("auth.enabled", null, false),
        internal_autoregister("internal.autoRegisterLocalDevice", null, true),
        remote_id_generation("internal.remote_id_generation", null, false),
        ssl_certificateFile("ssl.certificateFile", null, null),
        ssl_certificateKey("ssl.certificateKey", null, null),
        ssl_certificatePass("ssl.certificatePass", null, null),
        google_appid("google.appid", null, null),
        ;

        private String key, description;
        private Object defaultValue;
        ConfigKey(String key, String descriptions, Object defaultValue){
            this.key = "odev."+key;
            this.description = descriptions;
            this.defaultValue = defaultValue;
        }

        public String getKey() {return key;}

        public String getDescription() {return description;}

        public Object getDefaultValue() {
            return defaultValue;
        }
    }


    private OpenDeviceConfig(){
    }

    public static OpenDeviceConfig get(){
        if(INSTANCE == null){
            INSTANCE = new OpenDeviceConfig();

            String configDir = getConfigDirectory();

            // Get profile from system properties
            String profile = System.getProperty(PROFILE_KEY);

            File config = new File(configDir + File.separator + "odev-"+profile+".conf");

            if(!config.exists()) config = new File(configDir + File.separator + "odev.conf");

            if(config.exists()){
                try {
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(config));
                    INSTANCE.properties = properties;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }else{
                INSTANCE.properties = new Properties();
                log.info("Using default config");
            }

            if(!StringUtils.isEmpty(profile)){
                INSTANCE.set(ConfigKey.profile, profile);
            }

        }
        return INSTANCE;
    }

    /**
     * Get config directory from system properties or current directory
     * @return
     */
    public static String getConfigDirectory(){
        String configDir = System.getProperty("config-dir");
        if(configDir == null) configDir = getHomeDirectory()  + File.separator + "conf";
        return configDir;
    }

    /**
     * Get data directory from system properties or current directory
     * @return
     */
    public static String getDataDirectory(){
        return getHomeDirectory() + File.separator + "data";
    }

    public static String getHomeDirectory(){
        String dir = System.getProperty("odev-home");
        if(dir == null) dir = System.getProperty("user.dir");
        return dir;
    }

    /**
     * Checks whether the support for multi-tenant is active. This setting should only be enabled when using the platform as a service.
     * @return if multi-tenant is active
     */
    public boolean isTenantsEnabled() {
        return getBoolean(ConfigKey.tenants_enabled);
    }

    public void setTenantsEnabled(boolean value) {
        set(ConfigKey.tenants_enabled, value);
    }


    /**
     * Sets whether to make the broadcast of a command {@link br.com.criativasoft.opendevice.core.command.DeviceCommand} received by an incoming connection to the other incoming connections
     * @param value (Default = true)
     */
    public void setBroadcastInputs(boolean value) {
        set(ConfigKey.broadcast_inputs, value);
    }

    public boolean isBroadcastInputs() {return getBoolean(ConfigKey.broadcast_inputs);}

    public void setDatabaseEnabled(boolean value) {
        set(ConfigKey.database_enabled, value);
    }

    public boolean isDatabaseEnabled() {
        return getBoolean(ConfigKey.database_enabled);
    }

    /**
     * Set certificate file to enable SSL support over (MQTT, HTTP, REST, WebSocket)
     */
    public void setCertificateFile(String value) {
        set(ConfigKey.ssl_certificateFile, value);
    }

    public void setCertificateKey(String value) {
        set(ConfigKey.ssl_certificateKey, value);
    }

    public void setCertificatePass(String value) {
        set(ConfigKey.ssl_certificatePass, value);
    }

    public String getCertificateFile() {
        return getPath(ConfigKey.ssl_certificateFile);
    }

    public String getCertificateKey() {
        return getString(ConfigKey.ssl_certificateKey);
    }

    public String getCertificatePass() { return getString(ConfigKey.ssl_certificatePass); }

    public void setPort(int value) {
        set(ConfigKey.web_port, value);
    }

    public int getPort() {
        return getInt(ConfigKey.web_port);
    }

    public void setAuthRequired(boolean value) {
        set(ConfigKey.auth_enabled, value);
    }

    public boolean isAuthRequired() {
        return getBoolean(ConfigKey.auth_enabled);
    }

    public void setMqttEnabled(boolean value) {
        set(ConfigKey.mqtt_enabled, value);
    }

    public boolean isMqttEnabled() {
        return getBoolean(ConfigKey.mqtt_enabled);
    }

    public void setDatabasePath(String value) {set(ConfigKey.database_path, value); }

    public String getDatabasePath() {
        return getPath(ConfigKey.database_path);
    }

    public String getProfile() {
        return get(ConfigKey.profile);
    }

    public void setDatabaseEngine(String value) {set(ConfigKey.database_path, value);}

    public String getDatabaseEngine() { return getString(ConfigKey.database_engine);}


    /**
     * See {@link #getBindLocalVariables()}
     */
    public void setBindLocalVariables(boolean value) {
        this.bindLocalVariables = value;
        set(ConfigKey.internal_autoregister, value);
    }

    /**
     * This allow local application register/bind local (field/variable) devices without call addDevice.<br/>>
     * Note: This is a experimental feature and may slow down app initialization
     * @see br.com.criativasoft.opendevice.core.LocalDeviceManager#autoRegisterDevice(Device)
     */
    public boolean getBindLocalVariables() {
        if(this.bindLocalVariables == null ) bindLocalVariables = getBoolean(ConfigKey.internal_autoregister);
        return bindLocalVariables;
    }

    /**
     * Define if the UID device generation must be done by remote/cloud server.<br/>
     * This should be used when the devices are dynamically generated by the client application and
     * need to be synchronized with a remote server;
     * @param value
     */
    public void setRemoteIDGeneration(boolean value) {
        this.remoteIDGeneration = value;
        set(ConfigKey.internal_autoregister, value);
    }

    /**
     * @see #setRemoteIDGeneration(boolean)
     * @return
     */
    public boolean isRemoteIDGeneration() {
        if(this.remoteIDGeneration == null ) remoteIDGeneration = getBoolean(ConfigKey.remote_id_generation);
        return remoteIDGeneration;
    }

    public List<String> getExternalResources() { return getList(ConfigKey.web_external_resources);}

    public String getStartupScript() { return getString(ConfigKey.startup_script);}

    public String getLogConfig() { return getPath(ConfigKey.log_config);}

    //
    // Properties Utils
    //

    private String get(ConfigKey config) {
        String val = get(config.getKey());
        if(val == null){
            Object obj = config.getDefaultValue();
            if(obj != null) val = obj.toString();
        }
        return val;
    }

    private String get(String key) {

        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }

        return null;
    }

    private void set(ConfigKey config, Object value) {

        properties.setProperty(config.getKey(), value.toString());
    }


    public String getString(String key) {

        return get(key);
    }

    public String getString(ConfigKey key) {

        return get(key);
    }

    public int getInt(ConfigKey key) {

        String v = get(key);

        if (v == null) return (Integer) key.getDefaultValue();

        return Integer.parseInt(v);
    }

    public boolean getBoolean(ConfigKey key) {

        String v = get(key);

        if (v == null) return (Boolean) key.getDefaultValue();

        return Boolean.parseBoolean(v);
    }

    public List<String> getList(ConfigKey key) {

        String v = get(key);

        if (v == null) return (List) key.getDefaultValue();

        String[] temp = v.split("\\s*,\\s*");

        return new ArrayList<String>(Arrays.asList(temp));
    }

    public String getPath(ConfigKey key) {
        String s = getString(key);
        if (File.separatorChar == '/') {
            return s;
        }
        return s.replaceAll("\\/", File.separator);
    }

    /**
     * Get file in static path, or from home path
     */
    public File getFile(String key) {
        String path = get(key);

        if(path == null) return null;

        File file = new File(path);

        if(file.exists()) return file;

        file = new File(getHomeDirectory(), path);

        if(file.exists()) return file;

        file = new File(getConfigDirectory(), path);

        if(file.exists()) return file;

        return null;
    }

    public Class<? extends Object> getClass(ConfigKey key) {
        String className = getString(key);
        try {
            return Class.forName(className);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String[] makeArray(String v) {

        if (v.length() == 0) return new String[0];

        String[] array = v.split("\\s*,\\s*");

        for(int i = 0; i < array.length; i++) {
            String s = array[i];
            if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
                array[i] = s.substring(1, s.length() - 1);
            }
        }
        return array;
    }

    public String[] getArray(ConfigKey key) {
        String v = get(key).trim();

        if (!v.startsWith("[") || !v.endsWith("]")) throw new IllegalArgumentException("Not an array: " + v);

        v = v.substring(1, v.length() - 1).trim();

        return makeArray(v);
    }

}
