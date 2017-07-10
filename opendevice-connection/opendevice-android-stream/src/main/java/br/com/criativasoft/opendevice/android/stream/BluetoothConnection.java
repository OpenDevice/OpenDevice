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
import br.com.criativasoft.opendevice.android.AndroidContextSupport;
import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.IBluetoothConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


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

    private boolean secure = false;
    private boolean connectUsingThread = false;


    public BluetoothConnection(){
        this(null);
    }

    public BluetoothConnection(String deviceURI){
        super();
        setConnectionURI(deviceURI);
    }


    @Override
    public void connect() throws ConnectionException {

        bluetoothAdapter = getBluetoothAdapter();

        if (!isConnected()) {

            // try enable bluetooth.
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }

            // Check paired
//            if(isPaired()){

            ConnectTask connectTask = new ConnectTask(getBluetoothDevice());

            if(connectUsingThread){
                Thread connectThread = new Thread(connectTask);
                connectThread.setName("BluetoothConnection");
                connectThread.start();
                setStatus(ConnectionStatus.CONNECTING); // use this for backgroud connection
            }else{
                connectTask.run();
            }


//            }else{
//                setStatus(ConnectionStatus.DISCONNECTED);
//                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
        }

    }

    public boolean isPaired(){
        return getBluetoothDevice().getBondState() == BluetoothDevice.BOND_BONDED;
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

    public BluetoothAdapter getBluetoothAdapter(){

        if(bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        return bluetoothAdapter;
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
    private class ConnectTask implements Runnable {
        private BluetoothDevice device;

        public ConnectTask(BluetoothDevice device) {
            this.device = device;


            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                log.trace("Creating connection socket to: " + device);

                connection = device.createRfcommSocketToServiceRecord(UUID_SPP);

            } catch (Exception e) {
               log.error(e.getMessage(), e);
            }

        }

        public void run() {

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {

                // This is a blocking call and will only return on a
                // successful connection or an exception
                connection.connect();

            } catch (IOException e) {

                log.error(e.getMessage());

                try {
                    // FIX for erros in android >=4.2 (ref: https://stackoverflow.com/a/25647197)
                    log.info("trying fallback...");

                    connection = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                    connection.connect();

                    log.info("Connected using fallback");
                } catch (Exception e2) {
                    setStatus(ConnectionStatus.FAIL);

                    log.error(e.getMessage(), e);

                    // Close the socket
                    try {
                        connection.close();
                    } catch (IOException e3) {
                        log.error(e.getMessage(), e);
                    }

                    return;
                }

                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
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
