/*
 * ******************************************************************************
 *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.command;

/**
 * TODO: PENDING DOC
 *
 * @author ricardo
 * @date 19/06/14.
 */
public class GPIO extends DeviceCommand {

    public GPIO(int deviceID, long value) {
        super(CommandType.GPIO_DIGITAL, deviceID, value);
    }
}
