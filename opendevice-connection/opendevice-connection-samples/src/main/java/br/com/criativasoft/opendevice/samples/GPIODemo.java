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

package br.com.criativasoft.opendevice.samples;

import br.com.criativasoft.opendevice.connection.DeviceConnection;
import br.com.criativasoft.opendevice.connection.StreamConnectionFactory;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;

/**
 *
 * @author Ricardo JL Rufino
 * @date 19/06/2014
 */
public class GPIODemo {

    public static void main(String[] args) throws ConnectionException {
        DeviceConnection connection = StreamConnectionFactory.createUsb("/dev/ttyACM0");
        connection.connect();
    }
}
