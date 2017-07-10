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
import br.com.criativasoft.opendevice.core.command.CommandStatus;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.connection.EmbeddedGPIO;
import br.com.criativasoft.opendevice.core.model.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.impl.PinImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Wrapper to Raspberry GPIO (pi4j)
 * @author Ricardo JL Rufino
 * @date 06/10/14.
 */
public class RaspberryGPIO extends AbstractConnection implements EmbeddedGPIO {

    private final static Logger log = LoggerFactory.getLogger(RaspberryGPIO.class);

    private GpioController gpio;

    private GpioListenerIml listener = new GpioListenerIml();

    private Map<GpioPin, Integer> bindings = new HashMap<GpioPin, Integer>();

    private final Set<Device> devices = Collections.synchronizedSet(new LinkedHashSet<Device>());

    private Board board;

    public RaspberryGPIO(String name) {
        board = new Board(-1, name);
    }

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

            if(log.isTraceEnabled()) log.trace("Send GPIO - device = {}, pin = {}", deviceID, pin);

            if(pin != null){

                command.setStatus(CommandStatus.SUCCESS);

                if(command.getType() == CommandType.DIGITAL){

                    GpioPinDigitalOutput digital = (GpioPinDigitalOutput) pin;

                    if(command.getValue() == Device.VALUE_HIGH) digital.high();
                    if(command.getValue() == Device.VALUE_LOW) digital.low();

                }

                if(command.getType() == CommandType.ANALOG){

                    GpioPinAnalogOutput analog = (GpioPinAnalogOutput) pin;

                    analog.setValue(command.getValue());

                }

            }

        }

    }


    @Override
    public void attach(Device device){

        PhysicalDevice physicalDevice = (PhysicalDevice) device;

        if(findPinForDevice(device.getUid()) != null) return; // exist !

        if(physicalDevice.getGpio() ==  null) throw new IllegalStateException("Device doesn't have gpio config");

        GpioInfo info = physicalDevice.getGpio();

        Pin pin = new PinImpl(RaspiGpioProvider.NAME, info.getPin(), "GPIO " + info.getPin(),
                EnumSet.of(PinMode.DIGITAL_INPUT, PinMode.DIGITAL_OUTPUT),
                PinPullResistance.all());

        attach(device, pin);

    }

    public void attach(Device device, Pin pin){

        devices.add(device);
        board.addDevice(device);

        if(device instanceof Sensor){

            if(device.getType() == DeviceType.DIGITAL){
                attach(device.getUid(), gpio.provisionDigitalInputPin(pin));
            }else if(device.getType() == DeviceType.ANALOG){
                attach(device.getUid(), gpio.provisionAnalogInputPin(pin));
            }

        }else{
            if(device.getType() == DeviceType.DIGITAL){
                attach(device.getUid(), gpio.provisionDigitalOutputPin(pin));
            }else if(device.getType() == DeviceType.ANALOG){
                attach(device.getUid(), gpio.provisionAnalogOutputPin(pin));
            }
        }

    }

    public void attach(int deviceID, GpioPin pin){

        bindings.put(pin, deviceID);

        if(pin instanceof GpioPinInput){
            GpioPinInput input = (GpioPinInput) pin;
            input.addListener(listener);
        }

    }

    @Override
    public Board getBoardInfo() {
        return board;
    }

    /**
     *
     * @param gpio
     */
    protected void setup(GpioController gpio){

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

            System.out.println("Listener fired !");
            Integer deviceID = bindings.get(event.getPin());

            if(deviceID != null){
                DeviceCommand command = new DeviceCommand(CommandType.ANALOG, deviceID,(new Double(event.getValue())).longValue());
                notifyListeners(command);
            }


        }

        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            Integer deviceID = bindings.get(event.getPin());

            System.out.println("Listener fired !");

            if(deviceID != null){
                DeviceCommand command = new DeviceCommand(CommandType.DIGITAL, deviceID, event.getState().getValue());
                notifyListeners(command);
            }
        }
    }
}
