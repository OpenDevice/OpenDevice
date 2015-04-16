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
import br.com.criativasoft.opendevice.connection.discovery.DiscoveryService;
import br.com.criativasoft.opendevice.core.SimpleDeviceManager;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.core.dao.memory.DeviceMemoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//
// Run using Maven using: mvn compile exec:java -Dexec.mainClass=br.com.criativasoft.opendevice.middleware.Main
// Run on firebug: app.deviceList.create({name:'Tomada DB2', value:0, category: app.DeviceCategory.POWER_SOURCE});
// URLs REST:
// - http://localhost:8181/device/1/setvalue/1

public class Main extends SimpleDeviceManager {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

    protected int port = 8181;

	public void init() throws Exception {

        setApplicationID(OpenDeviceConfig.LOCAL_APP_ID);

        addDevice(new Device(1, "Luz 1", DeviceType.DIGITAL, DeviceCategory.LAMP, 0));
        addDevice(new Device(2, "Luz 2", DeviceType.DIGITAL, DeviceCategory.LAMP, 0));
        addDevice(new Device(3, "Luz 2", DeviceType.DIGITAL, DeviceCategory.LAMP, 0));

        // new FakeSensorSimulator(50, this, 6, 7).start(); // generate fake data
        // addFilter(new FixedReadIntervalFilter(1000, this));

		// Ativar serviço de descoberta desse servidor via UDP.
        DiscoveryService.listen(port);

        // Setup WebSocket (Socket.IO) with suport for simple htttpServer
        IWSServerConnection webscoket = Connections.in.websocket(port);
        String current = System.getProperty("user.dir");

        // Static WebResources
        String rootWebApp = getWebAppDir();
        webscoket.addWebResource(rootWebApp);
        log.debug("Current webresource: " + rootWebApp);
        webscoket.addWebResource( current + "/target/classes/webapp"); //  running exec:java

        webscoket.addWebResource("/media/Dados/Codigos/Java/Projetos/OpenDevice/opendevice-web-view/src/main/webapp");
        webscoket.addWebResource("/media/Dados/Codigos/Java/Projetos/OpenDevice/opendevice-clients/opendevice-js/dist");
        webscoket.addWebResource("/media/Dados/Codigos/Java/Projetos/OpenDevice/examples/opendevice-tutorial/src/main/resources");


        this.addInput(webscoket);


//		// No modo local ele se conecta com o servidor remoto.
//		if(MODE_LOCAL.equalsIgnoreCase(mode)){
//			this.addConnectionIN(new WSClientConnection(remoteServer));
//		}

        // OutputConnections
        // ===============================
        addOutput(Connections.out.usb()); // Connect to first USB port available
//        addOutput(Connections.out.bluetooth("00:13:03:14:19:07"));

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

        this.connectAll();

	}
	
	
	private String getWebAppDir(){

        extractResources();

		String current = System.getProperty("user.dir");

		File webapp = new File(current + File.separator + "webapp" );
		// Default app..
		if(webapp.exists()){
			return webapp.getPath();
		}

        webapp = new File(current + File.separator + "target"+ File.separator + "webapp" );
        // Default app..
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

    public static boolean extractResources() {
        try {

            String destPath = System.getProperty("user.dir") +  File.separator + "target" + File.separator;

            if(new File(destPath + "webapp").exists()){
                return false;
            }

            String jarSource = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            log.debug("Extracting contents to: " + destPath);
            log.debug("Source: " + jarSource);

            if(!jarSource.endsWith(".jar") && !jarSource.endsWith(".war")) return false;

            JarFile jarFile = new JarFile(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
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
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        return true;
    }
	

	public static void main(String[] args) throws Exception {

        final Main main = new Main();
        main.init();

        // Automatic shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                main.disconnectAll();
            }
        });

        // Manual shutdown
        log.info("OpenDevice Middleware - Server started on port {}", main.port);
        log.info("Type quit to stop the server");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String a = "";
        while (!("quit".equals(a))) {
            a = br.readLine();
        }

        System.out.println("Disconnecting all...");
        main.disconnectAll();
        Thread.sleep(800);
        System.exit(-1);
    }
	

}
