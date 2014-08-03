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

import br.com.criativasoft.opendevice.connection.StreamConnectionFactory;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.samples.ui.FormDevicesAPIController;

/**
 * TODO: PENDING DOC
 * USE: OpenDevice Middleware in Arduino...
 * @autor ricardo
 * @date 25/06/14.
 */
public class TCPDemo extends FormDevicesAPIController {

    public TCPDemo() throws ConnectionException {
        super(StreamConnectionFactory.createTcp("192.168.0.11:8282"));
    }

    public static void main(String[] args) throws ConnectionException {
        new TCPDemo().setVisible(true);
    }

}
