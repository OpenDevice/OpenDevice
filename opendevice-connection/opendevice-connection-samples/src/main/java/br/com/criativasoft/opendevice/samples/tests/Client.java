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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class Client
{
    //Ip Adress and Port, where the Arduino Server is running on
    private static final String serverIP="192.168.0.203";
    private static final int serverPort=80;
 
     public static void main(String argv[]) throws Exception
     {
          String msgToServer;//Message that will be sent to Arduino

          String msgFromServer;//recieved message will be stored here
 
          Socket clientSocket = new Socket(serverIP, serverPort);//making the socket connection
          System.out.println("Connected to:"+serverIP+" on port:"+serverPort);//debug
          //OutputStream to Arduino-Server
          DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
          //BufferedReader from Arduino-Server
          BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));//
 
          msgToServer = "Hello Arduino Server";//Message tha will be sent
          outToServer.writeBytes("Mensagem1"+'\n');//sending the message
          outToServer.flush();
             outToServer.writeBytes("Mensagem2"+'\n');
            outToServer.flush();
         outToServer.writeBytes("Mensagem2"+'\n');
         outToServer.flush();
         clientSocket.close();

     }
}