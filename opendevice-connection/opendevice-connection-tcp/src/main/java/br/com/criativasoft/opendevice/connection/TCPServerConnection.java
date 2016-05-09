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

package br.com.criativasoft.opendevice.connection;

import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.message.Request;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

/**
 * TCPServerConnection
 * @author Ricardo JL Rufino
 * @date 05/09/15
 */
public class TCPServerConnection extends AbstractStreamConnection implements ITcpServerConnection{

    private static final Logger log = LoggerFactory.getLogger(TCPServerConnection.class);

    private ServerSocket server;
    private List<TCPClientConnection> connections = new LinkedList<TCPClientConnection>();
    private int port;
    private boolean broadcast;

    public TCPServerConnection() {
        super();
    }

    /**
     * @param port
     */
    public TCPServerConnection(int port) {
        super();
        setPort(port);
        setConnectionURI(deviceURI);
    }

    @Override
    public void connect() throws ConnectionException {

        try {

            if(!isConnected()){

                initConnection(); // Setup

                setStatus(ConnectionStatus.CONNECTED);

                // Listen for Clients
                new ListenThread().start();

            }

        } catch (IOException e) {
            setStatus(ConnectionStatus.FAIL);
            throw new ConnectionException(e.getMessage(), e);
        }

    }

    private void initConnection() throws IOException{
        if(server == null){

            log.debug("Starting ServerSocket(TCP) on port:" + port);

            server = new ServerSocket(port);

            log.debug("Connected !");
        }
    }

    private void addConnection(TCPClientConnection connection) {
        this.connections.add(connection);
        connection.addListener(clientConnectionListener);
    }

    private void removeConnection(TCPClientConnection connection) {
        this.connections.remove(connection);
    }


    @Override
    public void disconnect() throws ConnectionException {
        if(isConnected()){
            try {
                server.close();
                super.disconnect();
                server = null;
            } catch (IOException e) {
                throw new ConnectionException(e);
            }

            log.debug("Disconnected !");
        }else{
            log.info("disconnect :: not connected !");
        }
    }


    @Override
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Enable broadcast between client connections.
     */
    public void enableBroadcast(){
        broadcast = true;
    }

    @Override
    public Message notifyAndWait(Request message) {
        log.error("notifyAndWait not implemented on this class");
        return null;
    }

    @Override
    public void send(Message message) throws IOException {
        if(!isConnected()){
            return;
        }

        broadcast(message, null);
    }

    private void broadcast(Message message, DeviceConnection from) throws IOException {
        // FIXME: Implement Multi-tenant - https://github.com/OpenDevice/OpenDevice/issues/38

//        // Get broadcast group for client.
//        Broadcaster broadcaster;
//        if(getConfig().isSupportTenants()){
//            broadcaster = atmosphereConfig.getBroadcasterFactory().lookup(cmd.getApplicationID());
//        }else{
//            broadcaster = atmosphereConfig.getBroadcasterFactory().lookup(OpenDeviceConfig.LOCAL_APP_ID);
//        }

        for (TCPClientConnection connection : connections) {
            if(connection != from && connection.isConnected()) {
                try{
                    connection.send(message);
                }catch (SocketException e){
                    connection.disconnect();
                }
            }
        }
    }

    private ConnectionListener clientConnectionListener = new ConnectionListener() {
        @Override
        public void connectionStateChanged(DeviceConnection connection, ConnectionStatus status) {

        }

        @Override
        public void onMessageReceived(Message message, DeviceConnection connection) {

            notifyListeners(message);

            if(broadcast) try {
                broadcast(message, connection);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    };



    private class ListenThread extends Thread{

        @Override
        public void run() {
            while(isConnected()) {
                try {

                    Socket clientSocket = server.accept();
                    TCPClientConnection connection = new TCPClientConnection(clientSocket, TCPServerConnection.this);
                    log.debug("New client connected : " + clientSocket.getInetAddress());
                    connection.connect();
                } catch (IOException e) {
                    if(!isConnected()) {
                        log.info("Server Stopped.") ;
                        return;
                    }
                    throw new RuntimeException("Error accepting client server", e);
                }
            }
        }
    }

    private class TCPClientConnection extends AbstractStreamConnection{

        private final Socket socket;
        private final TCPServerConnection server;

        private TCPClientConnection(Socket socket, TCPServerConnection server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void connect() throws ConnectionException {

            try{
                // open the streams
                setInput(socket.getInputStream());
                setOutput(socket.getOutputStream());

                setSerializer(server.getSerializer());
                setStreamReader(server.getStreamReader().getClass().newInstance());

                getStreamReader().setInput(input);

                if(reader instanceof DefaultSteamReader){
                    ((DefaultSteamReader)reader).startReading();
                }

                setStatus(ConnectionStatus.CONNECTED);

                server.addConnection(this);

            }catch (Exception e){
                setStatus(ConnectionStatus.FAIL);
                throw new ConnectionException(e.getMessage(), e);
            }

        }

        @Override
        public void disconnect() throws ConnectionException {
            if(isConnected()){
                try {
                    socket.close();
                    super.disconnect();
                    server.removeConnection(this);
                } catch (IOException e) {
                    throw new ConnectionException(e);
                }

                AbstractStreamConnection.log.debug("Disconnected !");
            }else{
                AbstractStreamConnection.log.info("not connected !");
            }
        }
    }




}