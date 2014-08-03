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

package br.com.criativasoft.opendevice.samples.tests;// File Name GreetingClient.java

import java.net.*;
import java.io.*;

public class TCPClient {

    private static final String serverName="192.168.0.204";
    private static final int port=8081;

    public static void main(String[] args) {

        ;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out =new DataOutputStream(outToServer);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in =new DataInputStream(inFromServer);

            out.write("Hello from PC".getBytes());

/*            while(true){
                int available = inFromServer.available();
                if(available == 0) continue;
                byte chunk[] = new byte[available];
                int count = inFromServer.read(chunk, 0, available);
                System.out.printf("read:count:"+count);
            }*/

//            System.out.println("Server says " + in.read);
//            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}