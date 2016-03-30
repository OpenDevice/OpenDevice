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

package opendevice.io.tests.newconns;

import br.com.criativasoft.opendevice.connection.TCPConnection;
import br.com.criativasoft.opendevice.core.LocalDeviceManager;
import br.com.criativasoft.opendevice.core.command.*;
import br.com.criativasoft.opendevice.core.connection.ConnectionType;

import java.io.IOException;

/**
 * @author Ricardo JL Rufino
 * @date 22/08/15.
 */
public class Esp2688ConfigWIFI extends LocalDeviceManager {

    public static void main(String[] args) { launch(args); }

    public void start() throws IOException {

//        TCPConnection tcp = out.tcp("192.168.3.100:8182");
//        TCPConnection tcp = out.tcp("192.168.43.149:8182");
        TCPConnection tcp = out.tcp("192.168.4.1:8182");

        // List<TCPConnectionInfo> list = TCPConnection.listAvailable(5000, true /*return on frist*/);
        // NAME, IP, PORT  (getDeviceURI)

        try {
            connect(tcp);
        }catch (Exception ex){
            System.err.println("Falha na conexão, causa: " + ex.getCause().getMessage());
            ex.printStackTrace();
            return;
        }

//        AddConnection addCmd = new AddConnection(ConnectionType.WIFI, "EscritorioCriativa", "criativa22269");
//        AddConnection addCmd = new AddConnection(ConnectionType.WIFI, "ricardo-ap", "abcdefghij");
        AddConnection addCmd = new AddConnection(ConnectionType.WIFI, "ricardoandroid", "87654321");

        send(addCmd, true);

        delay(1500); // delay to send..

        // Connection will be lost.
        tcp.disconnect();

        tcp.tryReconnect(5, 5000);

        System.err.println(">>> Connected ? [1]" + tcp.isConnected());

        if(tcp.isConnected()){

            // force ative connection
            delay(1000);
            send(new SimpleCommand(CommandType.PING_REQUEST, 0));

            AddConnectionResponse response = addCmd.getResponse();

            if(response == null) System.err.println("NO RESPONSE....");

            if(response != null) {
                System.err.println("--- PLEASE CHANGE WIFI---");
                System.err.println("response : " + response);
                System.err.println("IP : " + response.getIP());

                if(response.getStatus() != CommandStatus.SUCCESS){

                    System.err.println("REMOTE CONNECTION FAIL");
                    return;
                }

                tcp.disconnect();

                // TODO: verifica se é o mesmo IP.

                tcp.setConnectionURI(response.getIP() + ":8182");

                tcp.tryReconnect(5, 1000);

                System.err.println(">>> Connected ? [2] : " + tcp.isConnected());

                // force ative connection
                send(new SimpleCommand(CommandType.PING_REQUEST, 0));
            }

        }else{
            System.err.println("NOT CONNECT");
        }


    }
}

