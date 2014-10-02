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

package br.com.criativasoft.opendevice.wsrest;

import br.com.criativasoft.opendevice.connection.*;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.restapi.WaitResponseListener;
import br.com.criativasoft.opendevice.wsrest.guice.config.ConnectionGuiceProvider;
import br.com.criativasoft.opendevice.wsrest.guice.config.GuiceModule;
import br.com.criativasoft.opendevice.wsrest.io.CommandJacksonProvider;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.Command;
import org.atmosphere.cpr.*;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * Base WebSocket/REST/HTTP Server Connection
 * @author Ricardo JL Rufino
 * @date 11/06/2013
 */
public abstract class AbstractAtmosphereConnection extends AbstractConnection implements AtmosphereInterceptor, ConnectionListener, ServerConnection {

    private static final Logger log = LoggerFactory.getLogger(AbstractAtmosphereConnection.class);

    private int port;

    private Nettosphere server;

    private List<String> resources = new ArrayList<String>();

    private List<WaitResponseListener> waitListeners = new LinkedList<WaitResponseListener>();

    public AbstractAtmosphereConnection() {
        super();
    }

    public AbstractAtmosphereConnection(int port) {
        super();
        this.port = port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void connect() throws ConnectionException {
        try {
            log.debug("connecting...");
            if(!isConnected()){

                initConnection(); // Setup

                initServerEvents();

                log.debug("Starting server on port: " + port);
                server.start();

                setStatus(ConnectionStatus.CONNECTED);

            }

        } catch (IOException e) {
            throw new ConnectionException(e);
        }

    }

    private void initConnection() throws IOException{
        if(server == null){
            Config.Builder conf = new Config.Builder();
            conf.port(port);
            conf.host("127.0.0.1");
            configure(conf);

            conf.resource(GuiceModule.class);
            conf.resource(CommandJacksonProvider.class);

            //conf.resource("./webapp");  // For *-distrubution
            //conf.resource("./src/main/webapp"); // For mvn exec:java

            for(String resource : resources){
                conf.resource(resource);
            }

            conf.initParam("com.sun.jersey.api.json.POJOMappingFeature", "true");
            conf.initParam(ApplicationConfig.SCAN_CLASSPATH, "false");

            // conf.initParam("com.sun.jersey.spi.container.ResourceMethodDispatchProvider", "true");
            //.initParam(ApplicationConfig.OBJECT_FACTORY, GuiceConfigFactory.class.getName())
            conf.interceptor(this);
            conf.build();

            server = new Nettosphere.Builder().config(conf.build()).build();

        }
    }

    protected abstract void configure(Config.Builder conf);

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

        log.debug("waitListeners = " + waitListeners.size());
        log.debug("threads = " + Thread.activeCount());

        // Notify REST clients that are waiting for a response
        Iterator<WaitResponseListener> iterator = waitListeners.iterator();
        while(iterator.hasNext()){
            WaitResponseListener waitListener = iterator.next();
            boolean accept = waitListener.accept(message);
            if(accept) iterator.remove();
        }

        broadcast(message);
    }

    private void initServerEvents() {
        addListener(this);
    }


    public void addWebResource(String path){
        resources.add(path);
    }

    @Override
    public void configure(AtmosphereConfig atmosphereConfig) {
        //System.out.println("WSServerConnection :: configure");
        //ServletContext servletContext = atmosphereConfig.getServletConfig().getServletContext();
    }

    @Override
    public Action inspect(AtmosphereResource atmosphereResource) {

        ConnectionGuiceProvider.setConnection(this);

        // atmosphereResource.getRequest().getParameterMap().put("requestUID", new String[]{atmosphereResource.uuid()});
        // atmosphereResource.getRequest().setAttribute("requestUID", atmosphereResource.uuid());
        // AtmosphereResource atmosphereHandler = atmosphereResource.getAtmosphereHandler();

        return Action.CONTINUE;
    }

    @Override
    public void postInspect(AtmosphereResource atmosphereResource) {
        ConnectionGuiceProvider.setConnection(null);
    }

    @Override
    public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

    }


    @Override
    public Message notifyAndWait(Request request) {

        WaitResponseListener waitResponse =  new WaitResponseListener(request, this);
        waitListeners.add(waitResponse);

        try {
            return waitResponse.getResponse(1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            return null;
        }

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
            Broadcaster broadcaster = atmosphereConfig.getBroadcasterFactory().lookup(cmd.getApplicationID());

            Collection<Broadcaster> broadcasters = atmosphereConfig.getBroadcasterFactory().lookupAll();
            System.out.println("WSServer.broadcasters = "+ broadcasters.size());
            for (Broadcaster item : broadcasters) {
                System.out.println(" - " + item.getID());
            }

            if(broadcaster != null){
                Collection<AtmosphereResource> atmosphereResources = broadcaster.getAtmosphereResources();
//                System.out.println("WSServer.clients = "+ atmosphereResources.size());

                for (AtmosphereResource atmosphereResource : atmosphereResources) {

                    // Don't broadcast to yourself
                    if(!atmosphereResource.uuid().equals(cmd.getConnectionUUID())){
                        System.out.println("Broadcast ["+message+"] to -> " + atmosphereResource.uuid());
                        broadcaster.broadcast(message, atmosphereResource);
                    }

                }

            }





            // atmosphereConfig.getBroadcasterFactory().



            // Each client has your channel.
//            if(cmd.getApplicationID() != null && cmd.getApplicationID().trim().length() > 0){
//                atmosphereConfig.metaBroadcaster().broadcastTo(cmd.getApplicationID(), message);
//            }

        }
    }

}
