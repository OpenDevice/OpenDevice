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

import java.io.IOException;
import java.net.*;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author lycog
 */
public class SimpleUDPServer {
  public static void main(String[] args){
    DatagramSocket socket = null;
    DatagramPacket inPacket = null; //recieving packet
    DatagramPacket outPacket = null; //sending packet
    byte[] inBuf, outBuf;
    String msg;
    final int PORT = 5335;
 
    try{
      socket = new DatagramSocket(PORT);
      while(true){
        System.out.println("Waiting for client...");
 
        //Receiving datagram from client
        inBuf = new byte[256];
        inPacket = new DatagramPacket(inBuf, inBuf.length);
        socket.receive(inPacket);
 
        //Extract data, ip and port
        int source_port = inPacket.getPort();
        InetAddress source_address = inPacket.getAddress();
        msg = new String("Server READ: " + DateFormat.getDateTimeInstance().format(new Date()));
        System.out.println("Client " + source_address + ":" + msg);
 
        //Send back to client as an echo
        msg = reverseString(msg.trim());
        outBuf = msg.getBytes();
        outPacket = new DatagramPacket(outBuf, 0, outBuf.length,
                             source_address, source_port);
        socket.send(outPacket);
      }
    }catch(IOException ioe){
      ioe.printStackTrace();
    }
  }
 
  private static String reverseString(String input) {
    StringBuilder buf = new StringBuilder(input);
    return buf.reverse().toString();
  }
}