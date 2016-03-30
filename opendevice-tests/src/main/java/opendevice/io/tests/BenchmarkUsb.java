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

import br.com.criativasoft.opendevice.connection.UsbConnection;
import br.com.criativasoft.opendevice.core.command.CommandStreamSerializer;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;

import java.io.IOException;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 21/01/16
 */
public class BenchmarkUsb  {

    public static void main(String[] args) throws IOException {

        UsbConnection connection = new UsbConnection();
        connection.setSerializer(new CommandStreamSerializer());
        connection.connect();

        connection.send(DeviceCommand.ON(1));

    }
}
