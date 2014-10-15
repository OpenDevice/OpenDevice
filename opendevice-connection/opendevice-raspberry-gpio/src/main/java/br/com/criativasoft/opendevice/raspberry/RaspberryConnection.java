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

package br.com.criativasoft.opendevice.raspberry;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.ConnectionStatus;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceType;
import br.com.criativasoft.opendevice.core.model.Sensor;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper to Raspberry GPIO
 * @author Ricardo JL Rufino
 * @date 06/10/14.
 */
public class RaspberryConnection extends AbstractConnection {


    private GpioController gpio;

    private GpioListenerIml listener = new GpioListenerIml();

    private Map<GpioPin, Integer> bindings = new HashMap<GpioPin, Integer>();

    @Override
    public void connect() throws ConnectionException {

        gpio = getGPIO();

        setStatus(ConnectionStatus.CONNECTED);

        setup(gpio);

    }

    @Override
    public void disconnect() throws ConnectionException {
        gpio.shutdown();
        gpio = null;
    }



    @Override
    public void send(Message message) throws IOException {

        if(message instanceof  DeviceCommand){

            DeviceCommand command = (DeviceCommand) message;

            int deviceID = command.getDeviceID();

            GpioPin pin = findPinForDevice(deviceID);

            if(pin != null){

                if(command.getType() == CommandType.DIGITAL){

                    GpioPinDigitalOutput digital = (GpioPinDigitalOutput) pin;

                    digital.setState(command.getValue() == Device.ON);

                }

                if(command.getType() == CommandType.ANALOG){

                    GpioPinAnalogOutput analog = (GpioPinAnalogOutput) pin;

                    analog.setValue(command.getValue());

                }

            }

        }

    }


    public void bind(Device device, Pin pin){

        if(device instanceof Sensor){

            if(device.getType() == DeviceType.DIGITAL){
                bind(1, gpio.provisionDigitalInputPin(pin));
            }else if(device.getType() == DeviceType.ANALOG){
                bind(1, gpio.provisionAnalogInputPin(pin));
            }

        }else{
            if(device.getType() == DeviceType.DIGITAL){
                bind(1, gpio.provisionDigitalOutputPin(pin));
            }else if(device.getType() == DeviceType.ANALOG){
                bind(1, gpio.provisionAnalogOutputPin(pin));
            }
        }

    }

    public void bind(int deviceID, GpioPin pin){

        bindings.put(pin, deviceID);

        if(pin instanceof GpioPinInput){
            GpioPinInput input = (GpioPinInput) pin;
            input.addListener(listener);
        }

    }

    public void setup(GpioController gpio){

        bind(new Device(1, Device.DIGITAL), RaspiPin.GPIO_01);
        bind(5, this.gpio.provisionDigitalInputPin(RaspiPin.GPIO_02));

        //GpioPinDigitalInput gpioPinDigitalInput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02);
    }

    public GpioController getGPIO(){
        if(gpio == null) gpio = GpioFactory.getInstance();
        return gpio;
    }


    public GpioPin findPinForDevice(int deviceID){

        Set<GpioPin> pins = bindings.keySet();
        for (GpioPin pin : pins) {
            Integer id = bindings.get(pin);
            if(id == deviceID){
                return pin;
            }
        }

        return null;

    }

    private class GpioListenerIml implements GpioPinListenerAnalog, GpioPinListenerDigital{

        @Override
        public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {

            Integer deviceID = bindings.get(event.getPin());

            if(deviceID != null){
                DeviceCommand command = new DeviceCommand(CommandType.ANALOG, deviceID,(new Double(event.getValue())).longValue());
                notifyListeners(command);
            }


        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            Integer deviceID = bindings.get(event.getPin());

            if(deviceID != null){
                DeviceCommand command = new DeviceCommand(CommandType.DIGITAL, deviceID, event.getState().getValue());
                notifyListeners(command);
            }
        }
    }
}
