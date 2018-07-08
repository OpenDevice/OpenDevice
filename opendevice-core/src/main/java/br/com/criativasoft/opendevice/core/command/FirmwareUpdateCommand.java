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

/**
 * Command to notify start firmware update.
 * The path is relative to configured server IP on device.
 */
public class FirmwareUpdateCommand extends Command implements ExtendedCommand{

    private String path;

    public FirmwareUpdateCommand(String path) {
        super(CommandType.FIRMWARE_UPDATE);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public void deserializeExtraData(String extradata) {
        throw new IllegalStateException("not implemented !");
    }

    @Override
    public String serializeExtraData() {
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        return sb.toString();
    }

    @Override
    public int getTimeout() {
        return 30 * 1000; // 30sec
    }
}
