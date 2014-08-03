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

package br.com.criativasoft.opendevice.atemospherews;

import br.com.criativasoft.opendevice.atemospherews.guice.config.ConnectionGuiceProvider;
import br.com.criativasoft.opendevice.atemospherews.guice.config.GuiceModule;
import br.com.criativasoft.opendevice.atemospherews.io.CommandJacksonProvider;
import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionListener;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.restapi.DeviceRest;
import org.atmosphere.cpr.*;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.Collection;


/**
 * WebSocket Server Connection
 * @author Ricardo JL Rufino
 * @date 11/06/2013
 */
public class WSServerConnection extends AbstractConnection implements AtmosphereInterceptor, ConnectionListener {
	
	private static final Logger log = LoggerFactory.getLogger(WSServerConnection.class);
	
	public static final String PROP_WEBAPP_DIR = "webapp_dir";
	
	public static final String METHOD_UPDATE = "update";
	public static final String METHOD_CREATE = "create";
	public static final String METHOD_DELETE = "delete";
	public static final String METHOD_READ = "read";
	public static final String METHOD_SETVALUE = "setvalue";

    private static WSServerConnection INSTANCE;

    public static WSServerConnection getInstance() {
        return INSTANCE;
    }

    private int port;

	private Nettosphere server;

	public WSServerConnection(int port) {
		super();
		this.port = port;
        INSTANCE = this;
	}

    @Override
	public void connect() throws ConnectionException {
		try {
			log.debug("connecting...");
			if(!isConnected()){
				
				initConnection(); // Setup

                initServerEvents();

                server.start();
                log.debug("internal socket server [ok]");

				setStatus(ConnectionStatus.CONNECTED);
				
			}
			
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
		
	}



	private void initConnection() throws IOException{
		if(server == null){
            Config.Builder conf = new Config.Builder();
            conf.resource(DeviceRest.class);
            conf.resource(DeviceConnectionResource.class);
            conf.resource(GuiceModule.class);
            conf.resource(CommandJacksonProvider.class);
            conf.resource("./webapps");  // For *-distrubution
            conf.resource("./src/main/resources"); // For mvn exec:java
            conf.resource("./nettosphere-samples/jersey-chat/src/main/resources"); // For running inside an IDE
            conf.resource("/media/Dados/Codigos/Java/Projetos/OpenDevice/opendevice-web-view/src/main/webapp");
            conf.port(port);
            conf.host("127.0.0.1");
            conf.initParam("org.atmosphere.websocket.messageContentType", "application/json");
            conf.initParam("org.atmosphere.websocket.messageMethod", "POST");
            conf.initParam("com.sun.jersey.api.json.POJOMappingFeature", "true");

            conf.initParam("com.sun.jersey.spi.container.ResourceMethodDispatchProvider", "true");
                    //.initParam(ApplicationConfig.OBJECT_FACTORY, GuiceConfigFactory.class.getName())
            conf.interceptor(this);
            conf.build();

            server = new Nettosphere.Builder().config(conf.build()).build();
		}
	}

	
	@Override
	public void disconnect() throws ConnectionException {
		if(isConnected()){
			server.stop();
		}
	}


	@Override
	public boolean isConnected() {
		return server != null && ConnectionStatus.CONNECTED.equals(getStatus());
	}

    @Override
    public void send(Message message) throws IOException {
        Command command = (Command) message;
        System.out.println("WS.send -> " + message);
    }

    private void initServerEvents() {

        addListener(this);

		if(server != null){

		}
	}


    @Override
    public void configure(AtmosphereConfig atmosphereConfig) {

        System.out.println("WSServerConnection :: configure");

        ServletContext servletContext = atmosphereConfig.getServletConfig().getServletContext();
        System.out.println("servletContext = " + servletContext);
        System.out.println("atmosphereConfig.getServletConfig() = " +  atmosphereConfig.getServletConfig());

        servletContext.addListener("br.com.criativasoft.opendevice.atemospherews.GuiceConfig");
        servletContext.addServlet("GuiceServlet", "org.atmosphere.guice.AtmosphereGuiceServlet");

    }

    @Override
    public Action inspect(AtmosphereResource atmosphereResource) {

        ConnectionGuiceProvider.setConnection(WSServerConnection.this);

        // atmosphereResource.getRequest().getParameterMap().put("requestUID", new String[]{atmosphereResource.uuid()});

        System.out.println("WSServerConnection :: INSPECT :: uuid = " + atmosphereResource.uuid());

        // atmosphereResource.getRequest().setAttribute("requestUID", atmosphereResource.uuid());

//        AtmosphereResource atmosphereHandler = atmosphereResource.getAtmosphereHandler();
//        System.out.println("atmosphereHandler - " + atmosphereHandler);

        return Action.CONTINUE;
    }

    @Override
    public void postInspect(AtmosphereResource atmosphereResource) {
        System.out.println("WSServerConnection :: postInspect");
        ConnectionGuiceProvider.setConnection(null);
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

    }

    /**
     * Used to broadcast events/commands.</br>
     * Is fired by DeviceRest AND DeviceConnectionResource
     *
     * @param message
     * @param connection
     */
    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {
        broadcast(message);
    }


    private void broadcast(Message message){

        AtmosphereConfig atmosphereConfig = server.framework().getAtmosphereConfig();

        if(message instanceof  Command){

            Command cmd = (Command) message;

            // Get broadcast group for client.
            Broadcaster broadcaster = atmosphereConfig.getBroadcasterFactory().lookup(cmd.getClientID());


            Collection<AtmosphereResource> atmosphereResources = broadcaster.getAtmosphereResources();
            System.out.println("WSServer.clients = "+ atmosphereResources.size());
            for (AtmosphereResource atmosphereResource : atmosphereResources) {

                // Don't broadcast to yourself
                if(!atmosphereResource.uuid().equals(cmd.getConnectionUUID())){
                    System.out.println("BroadCast to -> " + atmosphereResource.uuid());
                    broadcaster.broadcast(message, atmosphereResource);
                }

            }

            Collection<Broadcaster> broadcasters = atmosphereConfig.getBroadcasterFactory().lookupAll();
            System.out.println("WSServer.broadcasters = "+ broadcasters.size());
            for (Broadcaster item : broadcasters) {
                System.out.println(" - " + item.getID());
            }

            // atmosphereConfig.getBroadcasterFactory().



            // Each client has your channel.
//            if(cmd.getClientID() != null && cmd.getClientID().trim().length() > 0){
//                atmosphereConfig.metaBroadcaster().broadcastTo(cmd.getClientID(), message);
//            }

        }
    }

}
