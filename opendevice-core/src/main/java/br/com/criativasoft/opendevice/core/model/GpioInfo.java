/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.core.model;

import br.com.criativasoft.opendevice.core.command.GPIO;

/**
 * GPIO Config
 *
 * @author Ricardo JL Rufino on 22/10/14.
 */
public class GpioInfo {

    private int pin;
    private GPIO.InputMode inputMode = GPIO.InputMode.NORMAL;

    public GpioInfo(int pin) {
        this.pin = pin;
    }

    public GpioInfo(int pin, GPIO.InputMode inputMode) {
        this.pin = pin;
        this.inputMode = inputMode;
    }

    public GPIO.InputMode getInputMode() {
        return inputMode;
    }

    public int getPin() {
        return pin;
    }
}
