/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.middleware;

import br.com.criativasoft.opendevice.connection.IWSServerConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.extension.ViewExtension;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.engine.js.OpenDeviceJSEngine;
import br.com.criativasoft.opendevice.middleware.config.DependencyConfig;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;
import br.com.criativasoft.opendevice.middleware.persistence.dao.DeviceDaoNeo4j;
import br.com.criativasoft.opendevice.mqtt.MQTTServerConnection;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceConfigRegistry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.SimpleBindings;
import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//
// Run using Maven using: mvn compile exec:java -Dexec.mainClass=br.com.criativasoft.opendevice.middleware.Main
// Run on firebug: app.deviceList.create({name:'Tomada DB2', value:0, category: app.DeviceCategory.POWER_SOURCE});
// URLs REST:
// - http://localhost:8181/device/1/setvalue/1

public class Main extends LocalDeviceManager {


	private static final Logger log = LoggerFactory.getLogger(Main.class);

    protected int port = 8181;

    private SimpleBindings jscontext;
    private IWSServerConnection webscoket;

	public void start() throws IOException  {

//        setApplicationID(OpenDeviceConfig.LOCAL_APP_ID);

        // Server with suport for HTTP,Rest,WebSocket
        webscoket = Connections.in.websocket(port);

        OpenDeviceConfig config = OpenDeviceConfig.get();

        jscontext = new SimpleBindings();
        jscontext.put("manager", this);
        jscontext.put("config", config);

        // Configuration Scripts.
        String userConfig = System.getProperty("config");
        if(!StringUtils.isBlank(userConfig)){
            log.info("Using config file (JS): " + userConfig);
            loadScript(userConfig);
        }

        // TODO: use EntityManager by injection
        if(config.isDatabaseEnabled()){
            setDeviceDao(new DeviceDaoNeo4j(LocalEntityManagerFactory.getInstance().createEntityManager()));
        }

        // new FakeSensorSimulator(50, this, 6, 7).start(); // generate fake data
        // addFilter(new FixedReadIntervalFilter(500, this));

		// Enable UDP discovery service.
        getDiscoveryService().listen();

        // Set IoC/DI Config
        GuiceConfigRegistry.setConfigClass(DependencyConfig.class);


        // Rest Resources
        // ================
        // webscoket.addResource(DashboardRest.class);

        // Static WebResources
        String rootWebApp = getWebAppDir();
        addWebResource(rootWebApp);
        log.debug("Current root-resource: " + rootWebApp);


        this.addInput(webscoket);
        this.addInput(new MQTTServerConnection());


        // Neo4j rasberry config: https://gist.github.com/widged/8329039
        //addOutput(Connections.out.tcp("192.168.0.204:8081"));
//        DeviceConnection conn = new RaspberryConnection() {
//            @Override
//            public void setup(GpioController gpio) {
//                attach(findDeviceByUID(1), RaspiPin.GPIO_01);
//                attach(findDeviceByUID(2), RaspiPin.GPIO_02);
//                attach(findDeviceByUID(3), RaspiPin.GPIO_03);
//                // attach(4, gpio.provisionDigitalInputPin(RaspiPin.GPIO_04));
//            }
//        };
//        addOutput(conn);

        this.connect();

	}
	
	
	private String getWebAppDir(){

        extractResources();

        String current = getClass().getResource("").getPath();

        // Current directory (of JAR)
        File webapp = new File(current + File.separator + "webapp" );
        if(webapp.exists()){
            return webapp.getPath();
        }

        // Current Directory
        current = System.getProperty("user.dir");
        webapp = new File(current + File.separator + "webapp" );
		if(webapp.exists()){
			return webapp.getPath();
		}

        // Using mvn exec
        webapp = new File(current + File.separator + "target"+ File.separator + "webapp" );
        if(webapp.exists()){
            return webapp.getPath();
        }

		// Find project in same directory...
		File currentDir = new File(current);
		String parent = currentDir.getParent();
		webapp = new File(parent + "/target/webapp" );
		if(webapp.exists()){
			return webapp.getPath();
		}
		
		return null;
	}

    protected boolean extractResources() {
        try {

            String destPath = System.getProperty("user.dir") +  File.separator + "target" + File.separator;

            extractResources(Main.class, destPath);

            // Load: User Interface Extensions
            // ======================================

            ServiceLoader<ViewExtension> service = ServiceLoader.load(ViewExtension.class);

            Iterator<ViewExtension> iterator = service.iterator();
            List<String> userExtensions = new ArrayList<String>();

            PrintWriter dynamic_plugins = new PrintWriter(new File(destPath, "webapp/ext/dynamic_plugins.js"));

            while (iterator.hasNext()) {
                ViewExtension extension = iterator.next();
                boolean extracted = extractResources(extension.getClass(), destPath);
                if (extracted){
                    dynamic_plugins.println("$.getScript('"+extension.loadScripts().get(0)+"', function(){\n" +
                            "\n" +
                            "   console.log('Additional user interface extension: "+extension.loadScripts().get(0)+"');\n" +
                            "\n" +
                            "});");
                }
                userExtensions.addAll(extension.loadScripts());
            }

            dynamic_plugins.close();

            log.info("Additional user interface extension: " + userExtensions);

        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    protected boolean extractResources(Class klass, String destPath) throws IOException {

        String jarSource = klass.getProtectionDomain().getCodeSource().getLocation().getPath();
        log.debug("Extracting contents to: " + destPath);
        log.debug("Source: " + jarSource);

        if(!jarSource.endsWith(".jar") && !jarSource.endsWith(".war")) return false;

        // Check if has extracted
        File extensionMarker = new File(destPath, "webapp/ext/." + klass.getSimpleName()+".properties");
        if(extensionMarker.exists()){
            log.debug("Ignoring webapp content for extension: " + klass);
            return false;
        }else{
            extensionMarker.getParentFile().mkdirs();
            PrintWriter dynamic_plugins = new PrintWriter(extensionMarker);
            dynamic_plugins.println("install.date = " + new Date());
            dynamic_plugins.close();
        }

        JarFile jarFile = new JarFile(klass.getProtectionDomain().getCodeSource().getLocation().getPath());
        Enumeration<JarEntry> enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry entry = enums.nextElement();
            if (entry.getName().startsWith("webapp")) {
                File toWrite = new File(destPath + entry.getName());
                if (entry.isDirectory()) {
                    toWrite.mkdirs();
                    continue;
                }
                InputStream in = new BufferedInputStream(jarFile.getInputStream(entry));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(toWrite));
                byte[] buffer = new byte[2048];
                for (;;) {
                    int nBytes = in.read(buffer);
                    if (nBytes <= 0) {
                        break;
                    }
                    out.write(buffer, 0, nBytes);
                }
                out.flush();
                out.close();
                in.close();
                log.debug("Extracting: {}", entry.getName());
            }
        }
        return true;
    }
	

	public static void main(String[] args) throws Exception {
        launchApplication(Main.class, args);
    }


    private void loadScript(String file){

        try {
            OpenDeviceJSEngine.run(new File(file), jscontext);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void addWebResource(String staticResourcePath){
        if(StringUtils.isBlank(staticResourcePath)) return;
        webscoket.addWebResource(staticResourcePath);
    }

}
