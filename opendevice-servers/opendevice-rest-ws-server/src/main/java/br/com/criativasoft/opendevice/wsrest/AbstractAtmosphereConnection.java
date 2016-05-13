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
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.command.Command;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.ResponseCommand;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.restapi.WaitResponseListener;
import br.com.criativasoft.opendevice.wsrest.guice.config.ConnectionGuiceProvider;
import br.com.criativasoft.opendevice.wsrest.guice.config.DeviceManagerGuiceProvider;
import br.com.criativasoft.opendevice.wsrest.io.CommandJacksonProvider;
import br.com.criativasoft.opendevice.wsrest.io.CrossOriginInterceptor;
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

    private List<String> webresources = new ArrayList<String>();
    private List<Class<?>> resources = new ArrayList<Class<?>>();

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
            //conf.host("::0"); // bind all local IPs
            conf.host("0.0.0.0"); // bind all local IPs
            configure(conf);

            conf.resource(CommandJacksonProvider.class);

            //conf.resource("./webapp");  // For *-distrubution
            //conf.resource("./src/main/webapp"); // For mvn exec:java

            for(String resource : webresources){
                conf.resource(resource);
            }

            for(Class<?> resource : resources){
                conf.resource(resource);
            }

            conf.initParam("com.sun.jersey.api.json.POJOMappingFeature", "true");
            conf.initParam(ApplicationConfig.SCAN_CLASSPATH, "false");
            conf.initParam(ApplicationConfig.ANALYTICS, "false");
            // conf.initParam(ApplicationConfig.DROP_ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "false");

            // conf.initParam("com.sun.jersey.spi.container.ResourceMethodDispatchProvider", "true");
            //.initParam(ApplicationConfig.OBJECT_FACTORY, GuiceConfigFactory.class.getName())
            conf.interceptor(new CrossOriginInterceptor());
//            conf.interceptor(new JacksonFilterInterceptor());
            conf.interceptor(this);
            conf.build();

            server = new Nettosphere.Builder().config(conf.build()).build();

        }
    }

    protected OpenDeviceConfig getConfig(){
        return OpenDeviceConfig.get();
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

        // Notify clients that are waiting for a response
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
        if(path != null){
            webresources.add(path);
        }
    }
    public void addResource(Class<?> resource){
        if(resource != null){
            resources.add(resource);
        }
    }


    @Override
    public void configure(AtmosphereConfig atmosphereConfig) {
        //System.out.println("WSServerConnection :: configure");
        //ServletContext servletContext = atmosphereConfig.getServletConfig().getServletContext();
    }

    @Override
    public Action inspect(AtmosphereResource atmosphereResource) {

        String appID = atmosphereResource.getRequest().getHeader(TenantProvider.HTTP_HEADER_KEY);
        if(appID != null) TenantProvider.setCurrentID(appID);

        DeviceManager manager = (DeviceManager) this.getConnectionManager();
        ConnectionGuiceProvider.setConnection(this);
        DeviceManagerGuiceProvider.setInstance(manager);

        // atmosphereResource.getRequest().getParameterMap().put("requestUID", new String[]{atmosphereResource.uuid()});
        // atmosphereResource.getRequest().setAttribute("requestUID", atmosphereResource.uuid());
        // AtmosphereResource atmosphereHandler = atmosphereResource.getAtmosphereHandler();

        return Action.CONTINUE;
    }

    @Override
    public void postInspect(AtmosphereResource atmosphereResource) {
        ConnectionGuiceProvider.setConnection(null);
        DeviceManagerGuiceProvider.setInstance(null);
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
     * Is fired by {@linkplain br.com.criativasoft.opendevice.restapi.DeviceRest} AND {@linkplain WebSocketResource}
     *
     * @param message
     * @param connection
     */
    @Override
    public void onMessageReceived(Message message, DeviceConnection connection) {

        if(message instanceof  Command) {

            Command cmd = (Command) message;

            if(CommandType.allowBroadcast(cmd.getType())){
                broadcast(message);
            }

        }

    }



    private void broadcast(Message message){

        if(server == null) return;

        AtmosphereConfig atmosphereConfig = server.framework().getAtmosphereConfig();

        if(message instanceof  Command){

            Command cmd = (Command) message;

            // Get broadcast group for client.
            Broadcaster broadcaster;
            if(getConfig().isSupportTenants()){
                broadcaster = atmosphereConfig.getBroadcasterFactory().lookup(cmd.getApplicationID());
            }else{
                broadcaster = atmosphereConfig.getBroadcasterFactory().lookup(OpenDeviceConfig.LOCAL_APP_ID);
            }

            if(broadcaster != null){

                log.debug("To: " +cmd.getApplicationID()+ " ( clients: "+broadcaster.getAtmosphereResources().size()+" ) ");


                Collection<AtmosphereResource> atmosphereResources = broadcaster.getAtmosphereResources();
//                System.out.println("WSServer.clients = "+ atmosphereResources.size());

                for (AtmosphereResource atmosphereResource : atmosphereResources) {

                    if(cmd instanceof ResponseCommand){

                        if(atmosphereResource.uuid().equals(cmd.getConnectionUUID())){
                            broadcaster.broadcast(message, atmosphereResource);
                        }

                    }else if(!atmosphereResource.uuid().equals(cmd.getConnectionUUID())){
                        broadcaster.broadcast(message, atmosphereResource);
                    }

                }

            } else {
                log.warn("To: " + cmd.getApplicationID() + "( broadcast channel not found )");
            }


        }
    }

    public void destroy() {

    }

}
