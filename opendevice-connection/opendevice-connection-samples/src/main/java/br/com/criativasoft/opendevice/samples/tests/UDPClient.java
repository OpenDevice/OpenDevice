/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.samples.tests;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class UDPClient {

//    private static final String serverIP="127.0.0.1"; //
    private static final String serverIP="192.168.0.204";
    private static final int serverPort=5335;

    public static void main(String args[]) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(serverIP);

        System.out.println("Sento Server on " + IPAddress);
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        for (int i = 0; i < 20; i++) {
            String sentence = "hello:"+i;
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
            clientSocket.send(sendPacket);
            System.out.println("Send:"+sentence);
            Thread.sleep(500);
        }

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        System.out.println("Aguardando...");
        clientSocket.receive(receivePacket);
        System.out.println("Recebido...");
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);
        clientSocket.close();
    }
}