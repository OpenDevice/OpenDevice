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

package br.com.criativasoft.opendevice.core.discovery;

import br.com.criativasoft.opendevice.connection.discovery.DiscoveryListener;
import br.com.criativasoft.opendevice.connection.discovery.NetworkDeviceInfo;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DiscoveryResponse;
import br.com.criativasoft.opendevice.core.command.SimpleCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class DiscoveryClientService implements Runnable {

    private static final int DISCOVERY_PORT = DiscoveryServiceImpl.DISCOVERY_PORT;

    private Logger log = LoggerFactory.getLogger(DiscoveryClientService.class);

    private long timeout;
    private boolean closeOnFinish = true;
    private String deviceName;
    private DatagramSocket socket;
    private DiscoveryListener listener;
    private Set<NetworkDeviceInfo> devices = new HashSet<NetworkDeviceInfo>();

    final CommandStreamSerializer serializer = new CommandStreamSerializer();

    public DiscoveryClientService(long timeout, String deviceName, DiscoveryListener listener , DatagramSocket socket) {
        this.timeout = timeout;
        this.deviceName = deviceName;
        this.listener = listener;
        this.socket = socket;
        this.closeOnFinish = false; // shared instance.
    }

    @Override
    public void run() {
        try{
            scan();
        } catch (IOException e) {
            log.error("Could not send discovery request", e);
        }
    }

    public void scan() throws IOException {
        if(socket == null) {
            socket = new DatagramSocket(DISCOVERY_PORT);
            socket.setBroadcast(true);
            socket.setSoTimeout((int) timeout);
        }
        sendDiscoveryRequest(socket);
        listenForResponses(socket);
    }

    /**
     * Send a broadcast UDP packet containing a request for boxee services to
     * announce themselves.
     *
     * @throws IOException
     */
    private void sendDiscoveryRequest(DatagramSocket socket) throws IOException {
        byte[] bytes = serializer.serialize(new SimpleCommand(CommandType.DISCOVERY_REQUEST, 0));
        log.debug("Send discover request... ");
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length ,getBroadcastAddress(), DISCOVERY_PORT);
        socket.send(packet);
    }

    /**
     * Listen on socket for responses, timing out after timeout
     *
     * @param socket
     *          socket on which the announcement request was sent
     * @throws IOException
     */
    private void listenForResponses(DatagramSocket socket) throws IOException {
        byte[] buf = new byte[64];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout((int) timeout);
                socket.receive(packet);

                log.debug("Received response from <<" + packet.getAddress().getHostAddress() + ">> " + new String(packet.getData()));

                if(!isLocalIP(packet.getAddress())){
                    final DiscoveryResponse response = (DiscoveryResponse) serializer.parse(packet.getData());
                    NetworkDeviceInfo deviceInfo = response.getDeviceInfo();
                    deviceInfo.setIp(packet.getAddress().getHostAddress());

                    log.info("Found Device: " + deviceInfo);

                    if(deviceName == null || "*".equals(deviceName) || deviceName.equals(deviceInfo.getName())) {
                        devices.add(deviceInfo);
                        notifyListeners(deviceInfo);
                    }

                    if (deviceName != null && deviceInfo.getName().equals(deviceName)){
                        break;
                    }
                }

            }
        } catch (SocketTimeoutException e) {
            log.debug("Scan timeout");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        if(this.closeOnFinish) {
            socket.close();
        }
    }


    /**
     * Calculate the broadcast IP we need to send the packet along. If we send it
     * to 255.255.255.255, it never gets sent. I guess this has something to do
     * with the mobile network not wanting to do broadcast.
     */
    private InetAddress getBroadcastAddress() throws IOException {

        System.setProperty("java.net.preferIPv4Stack", "true");

        InetAddress broadcast = null;

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();

            if (network.isLoopback() || ! network.isUp())
                continue;

            for (InterfaceAddress interfaceAddress : network.getInterfaceAddresses()) {
                broadcast = interfaceAddress.getBroadcast();
                if (broadcast == null)
                    continue;

                break;
            }
        }

        return broadcast;
    }


    private boolean isLocalIP(InetAddress address) throws IOException {

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface network = interfaces.nextElement();

            if (network.isLoopback() || ! network.isUp()) {
                continue;
            }

            Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress netAddress = inetAddresses.nextElement();

                if (address.getHostAddress().equals(netAddress.getHostAddress()))
                    return true;

            }

        }

        return false;
    }

    private void notifyListeners(NetworkDeviceInfo deviceInfo) {

        if(listener != null) listener.onDiscoveryDevice(deviceInfo);

    }

    public Set<NetworkDeviceInfo> getDevices() {
        return devices;
    }
}
