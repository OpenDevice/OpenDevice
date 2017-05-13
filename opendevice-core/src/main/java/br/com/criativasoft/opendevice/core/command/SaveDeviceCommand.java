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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.core.model.Device;

/**
 * TODO: Add docs.
 * @author Ricardo JL Rufino
 * Date: 10/05/17
 */
public class SaveDeviceCommand extends Command implements ExtendedCommand {

    /**
     * Need firmware sincronization ?
     */
    private boolean sync;

    private Device device;

    public SaveDeviceCommand() {
        this(null);
    }

    public SaveDeviceCommand(Device device) {
        super(CommandType.DEVICE_SAVE);
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public boolean isSync() {
        return sync;
    }

    public boolean getSync() {
        return sync;
    }

    @Override
    public void deserializeExtraData(String extradata) {
        // TODO: firmware version of this command is not implemented
        throw new IllegalStateException("firmware version of this command is not implemented ");
    }

    @Override
    public String serializeExtraData() {
        throw new IllegalStateException("firmware version of this command is not implemented ");
    }
}

