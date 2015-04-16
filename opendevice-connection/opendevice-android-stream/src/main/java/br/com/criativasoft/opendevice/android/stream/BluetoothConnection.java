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

package br.com.criativasoft.opendevice.android.stream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import br.com.criativasoft.opendevice.android.AndroidContextSupport;
import br.com.criativasoft.opendevice.connection.IBluetoothConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;


/**
 * Android Bluetooth Connection (Client)
 * Reference: http://developer.android.com/guide/topics/connectivity/bluetooth.html
 * @author Ricardo JL Rufino on 25/10/14.
 */
public class BluetoothConnection extends AbstractStreamConnection implements IBluetoothConnection, AndroidContextSupport {

    private static final Logger log = LoggerFactory.getLogger(BluetoothConnection.class);

    // Well known SPP UUID
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothSocket connection;
    private Context context;

    boolean secure = false;

    public BluetoothConnection(){
        this(null);
    }

    public BluetoothConnection(String deviceURI){
        super();
        setConnectionURI(deviceURI);
    }


    @Override
    public void connect() throws ConnectionException {

        if(bluetoothAdapter == null) bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!isConnected()) {
            ConnectThread connectThread = new ConnectThread(getBluetoothDevice());
            connectThread.start();
        }

    }

    public void connect2() throws ConnectionException {

        try {

            if(!isConnected()){

                if(bluetoothAdapter == null) bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                // try enable bluetooth.
                if (!bluetoothAdapter.isEnabled()) {

                    if(context != null){
//                        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//                        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
//                        if(!runningTasks.isEmpty()){
//                            ComponentName cn = runningTasks.get(0).topActivity;
//                             Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        }

                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        context.startActivity(enableBtIntent);
                        // startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }


                }

                log.info("Connecting to: " + getConnectionURI());

                initConnection(); // Setup

                // Make a connection to the BluetoothSocket
                try {

                    // Always cancel discovery because it will slow down a connection
                    bluetoothAdapter.cancelDiscovery();

                    // This is a blocking call and will only return on a successful connection or an exception
                    connection.connect();

                    log.debug("Conected !");

                } catch (IOException e) {

                    log.error("Connection Error: " + e.getMessage(), e);

                    // Close the socket
                    try {
                        connection.close();
                    } catch (IOException e2) {
                        log.error("unable to close() socket during connection failure", e2);
                    }
                    setStatus(ConnectionStatus.FAIL);

                    connection = null;
                    throw new ConnectionException(e);
                }

                // open the streams
                setInput(connection.getInputStream());
                setOutput(connection.getOutputStream());

                getStreamReader().setInput(input);

                if(reader instanceof DefaultSteamReader){
                    ((DefaultSteamReader)reader).startReading();
                }

                setStatus(ConnectionStatus.CONNECTED);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ConnectionException(e.getMessage());
        }

    }


    private void initConnection() throws IOException{

        if(connection == null){

            // If no URL specified , find first device available
            if(getConnectionURI() == null) setConnectionURI(getFirstAvailable());

            BluetoothDevice device = getBluetoothDevice();

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {

//                boolean withSdp = device.fetchUuidsWithSdp();
//                UUID uuid;
//                if(withSdp && device.getUuids() != null){
//                    uuid = device.getUuids()[0].getUuid();
//                }else{
//                    uuid = SPP_UUID;
//                }

                //connection = device.createRfcommSocketToServiceRecord(UUID_SPP);
                // connection = device.createRfcommSocketToServiceRecord(UUID_SPP);

                // for galaxy tab 2 with:bxabyyyyyyyxxxxb
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                connection = (BluetoothSocket) m.invoke(device, 1);

                log.info("Internal Socket (insecure) : " + connection);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new ConnectionException(e);
            }


        }
    }

    @Override
    public boolean isConnected() {

        return connection != null && super.isConnected();

    }

    @Override
    public void disconnect() throws ConnectionException {
        if(isConnected()){
            try {

                log.info("Disconnecting bluetooth device: " + this.getConnectionURI());

                connection.close();
                super.disconnect();
                connection = null;
            } catch (IOException e) {
                throw new ConnectionException(e);
            }

            log.debug("Disconnected !");
        }else{
            log.info("disconnect :: not connected !");
        }
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    public BluetoothDevice getBluetoothDevice(){
        return bluetoothAdapter.getRemoteDevice(getConnectionURI());
    }

    /**
     * Returns the first available bluetooth device
     * @return If none is available returns NULL
     */
    public static String getFirstAvailable() {
        List<String> portNames = listAvailable();
        if(portNames != null && portNames.size() > 0) return portNames.get(0);
        return null;
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                log.trace("Creating connection scoket to: " + device);

                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                tmp = (BluetoothSocket) m.invoke(device, 1);

                // tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (Exception e) {
               log.error(e.getMessage(), e);
            }
            mmSocket = tmp;
            connection = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

                setStatus(ConnectionStatus.CONNECTED);

            } catch (IOException e) {

                setStatus(ConnectionStatus.FAIL);

                log.error(e.getMessage(), e);

                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    log.error(e.getMessage(), e);
                }
                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
                return;
            }


            // open the streams
            try {

                setInput(connection.getInputStream());
                setOutput(connection.getOutputStream());

                getStreamReader().setInput(input);

                if(reader instanceof DefaultSteamReader){
                    ((DefaultSteamReader)reader).startReading();
                }

                setStatus(ConnectionStatus.CONNECTED);

            } catch (IOException e) {
                log.error(e.getMessage(), e);
                setStatus(ConnectionStatus.FAIL);
            }

        }


    }



    public static List<String> listAvailable() {
      throw new IllegalStateException("not implemented");
    }
}
