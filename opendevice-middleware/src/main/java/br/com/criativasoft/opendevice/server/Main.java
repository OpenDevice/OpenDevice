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

package br.com.criativasoft.opendevice.server;

import br.com.criativasoft.opendevice.atemospherews.WSServerConnection;
import br.com.criativasoft.opendevice.connection.StreamConnectionFactory;
import br.com.criativasoft.opendevice.connection.UsbConnection;
import br.com.criativasoft.opendevice.connection.discovery.DiscoveryServer;
import br.com.criativasoft.opendevice.core.BaseDeviceManager;
import br.com.criativasoft.opendevice.core.dao.memory.DeviceMemoryDao;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.util.Scanner;

//
// Run on firebug: app.deviceList.create({name:'Tomada DB2', value:0, category: app.DeviceCategory.POWER_SOURCE});
// URLs REST:
// - http://localhost:9191/device/1/setvalue/1

public class Main extends BaseDeviceManager {
	
	private static final String MODE_REMOTE = "remote";
	private static final String MODE_LOCAL = "local";
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public void init() throws Exception {

        setDeviceDao(new DeviceMemoryDao());

		addDevice(new Device(1, "Luz DB1", DeviceType.DIGITAL, DeviceCategory.LAMP, 0));
        addDevice(new Device(2, "Luz DB2", DeviceType.DIGITAL, DeviceCategory.LAMP, 0));
        addDevice(new Device(3, "Tomada DB1", DeviceType.DIGITAL, DeviceCategory.POWER_SOURCE, 0));
        addDevice(new Device(4, "Tomada DB2", DeviceType.DIGITAL, DeviceCategory.POWER_SOURCE, 0));
		
		// Ativar serviço de descoberta desse servidor via UDP.
		Thread discoveryThread = new Thread(DiscoveryServer.getInstance());
		discoveryThread.start();

		String webport = System.getProperty("app.port");
		String webapp = System.getProperty("app.dir");
		String mode = System.getProperty("app.mode");
		String remoteServer = "http://openhouse.criativasoft.com.br/";
		
		if(mode == null){
			mode = MODE_LOCAL;
		}
		
		int port = 8181;
		int restPort = 9191;
		
		if(webport != null && webport.trim().length() != 0) port= Integer.parseInt(webport);

		if(! (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik"))){
			String current = getWebAppDir();
			// StaticResourceHandler.setDefaultTempDir(new File(current)); FIXME: implementar algo similar, para funcionar no android !!
		}

        // Setup WebSocket (Socket.IO) with suport for simple htttpServer
        WSServerConnection wsServerConnection = new WSServerConnection(port);
        wsServerConnection.addWebResource("/media/Dados/Codigos/Java/Projetos/OpenDevice/opendevice-web-view/src/main/webapp");

        this.addInput(wsServerConnection);
		// this.addConnectionIN(new RestServerConnection(restPort)); // Servidor REST
		
//		// No modo local ele se conecta com o servidor remoto.
//		if(MODE_LOCAL.equalsIgnoreCase(mode)){
//			this.addConnectionIN(new WSClientConnection(remoteServer));
//		}

        // Procurar dispositivos USB
        if(MODE_LOCAL.equalsIgnoreCase(mode)){
            String usbPort = UsbConnection.getFirstAvailable();

            log.debug("Connection to USB port: " + usbPort);

            // setup connection with arduino/hardware
           if(usbPort != null) {
               addOutput(StreamConnectionFactory.createUsb(usbPort)); // Connect to first USB port available
           }
        }

		// Não é o andoid e possue suporte a ambiente visual (! isHeadless)
		// TODO: Testar em anbiente onde não tem o bluetooth....
		if(! (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) &&
		   ! GraphicsEnvironment.isHeadless()){  
//			this.addConnectionOut(new BluetoothDesktopConnection("00:13:03:14:19:07")); // stellaris 
//			this.addConnectionOut(new BluetoothDesktopConnection("00:11:06:14:04:57")); // stellaris old

//            StreamConnection bluetoothConnection = StreamConnectionFactory.createBluetooth("20:13:01:24:01:93");
//            bluetoothConnection.setSerializer(new CommandStreamSerializer()); // data conversion..
//            bluetoothConnection.setStreamReader(new CommandStreamReader()); // data protocol..
//			this.addConnectionOut(bluetoothConnection);
		}


		this.connectAll();

	}
	
	
	private String getWebAppDir(){
		String current = System.getProperty("user.dir");
		
		File webapp = new File(current + "/static-webapp" );
		
		// Default app..
		if(webapp.exists()){
			return webapp.getPath();
		}
		
		// Find project in same directory...
		File currentDir = new File(current);
		String parent = currentDir.getParent();
		
		webapp = new File(parent + "/opendevice-web-view/src/main/webapp" );
		if(webapp.exists()){
			return webapp.getPath();
		}
		
		return null;
	}
	

	public static void main(String[] args) throws Exception {

        final Main main = new Main();
        main.init();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                main.disconnectAll();
            }
        });

        System.out.print("Type Enter to Shutdown ....");
        Scanner in = new Scanner(System.in);
        String data= in.nextLine();
        in.close();

        System.out.println("Shutdown...");
        main.disconnectAll();
        Thread.sleep(2000);
        System.out.println("Shutdown [OK]");
    }
	

}
