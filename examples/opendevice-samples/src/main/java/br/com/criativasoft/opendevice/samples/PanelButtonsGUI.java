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

import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.core.connection.Connections;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;
import br.com.criativasoft.opendevice.samples.ui.FormDevicesAPIController;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Tutorial: https://opendevice.atlassian.net/wiki/display/DOC/A.+First+Steps+with+OpenDevice
 * For arduino/energia use: opendevice-hardware-libraries/arduino/OpenDevice/examples/UsbConnection
 * For arduino(with bluetooth): opendevice-hardware-libraries/arduino/OpenDevice/examples/BluetoothConnection
 * @author ricardo
 * @date 25/06/14.
 */
public class PanelButtonsGUI extends FormDevicesAPIController {

    public PanelButtonsGUI() throws ConnectionException {
//        super(Connections.out.bluetooth("001303141907"));
        super(Connections.out.usb());

        Collection<Device> devices = new LinkedList<Device>();
        devices.add(new Device(1,"RED", DeviceType.DIGITAL));
        devices.add(new Device(2,"GREEN", DeviceType.DIGITAL));
        devices.add(new Device(3,"BLUE", DeviceType.DIGITAL));
        devices.add(new Sensor(4,"Sensor 1", DeviceType.DIGITAL));
        devices.add(new Sensor(5,"Sensor 2", DeviceType.DIGITAL));

        addDevices(devices);
        connect();
    }

    public static void main(String[] args) throws ConnectionException {
        new PanelButtonsGUI().setVisible(true);
    }

}
