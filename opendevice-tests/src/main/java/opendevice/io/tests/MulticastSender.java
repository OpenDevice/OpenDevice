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

package opendevice.io.tests;

import br.com.criativasoft.opendevice.core.discovery.DiscoveryServiceImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 *
 * @author uli
 */
public class MulticastSender {
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
 
        //Address
        String multiCastAddress = "224.1.0.1";
        final int multiCastPort = DiscoveryServiceImpl.DISCOVERY_PORT;
 
        //Create Socket
        System.out.println("Create socket on address " + multiCastAddress + " and port " + multiCastPort + ".");
        InetAddress group = InetAddress.getByName(multiCastAddress);
                                                    MulticastSocket s = new MulticastSocket(multiCastPort);
        s.joinGroup(group);
 
        //Prepare Data
        String message = "Hello there!\n\r";
        byte[] data = message.getBytes("utf-8");
 
        //Send data
        s.send(new DatagramPacket(data, data.length, group, multiCastPort));

        Thread.sleep(1000);

        s.leaveGroup(group);
        s.close();
    }
}