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

import br.com.criativasoft.opendevice.core.model.*;
import br.com.criativasoft.opendevice.core.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class GetDevicesResponse extends ResponseCommand implements ExtendedCommand {

	public static final CommandType TYPE = CommandType.GET_DEVICES_RESPONSE;

    private int index;

    private int length;

	private Collection<Device> devices = new LinkedList<Device>();

	private static final long serialVersionUID = -1023397181880070237L;

    public GetDevicesResponse() {
        super(TYPE, CommandStatus.CREATED);
    }

	public GetDevicesResponse(Collection<Device> devices, String connectionUUID) {
		super(TYPE, CommandStatus.CREATED, connectionUUID);
		this.devices = devices;
	}

	public Collection<Device> getDevices() {
		return devices;
	}

	@Override
	public void deserializeExtraData(String extradata) {

        String[] split = extradata.split(Command.DELIMITER);

        Board board = null;
        List<Device> deviceList = new LinkedList<Device>();

        index = Integer.parseInt(split[0]);
        length = Integer.parseInt(split[1]);

        for (int i = 2; i < split.length; i++) {
            String deviceStr = split[i].substring(1, split[i].length()-1);
            String[] deviceSplit = deviceStr.split(",");
            String name = deviceSplit[0];
            int uid = Integer.parseInt(deviceSplit[1]);
            long value = Long.parseLong(deviceSplit[3]);
            boolean isSensor = Integer.parseInt(deviceSplit[5]) > 0;

            DeviceType deviceType =  DeviceType.getByCode(Integer.parseInt(deviceSplit[6]));

            if(deviceType == DeviceType.BOARD) {
                if (StringUtils.isEmpty(name)) name = "Board " + uid;
                board = new Board(uid, name, deviceType, DeviceCategory.GENERIC, value);
            } else if(isSensor){
                if(StringUtils.isEmpty(name)) name = "Sensor " + uid;
                Sensor sensor = new Sensor(uid, name, deviceType, DeviceCategory.GENERIC, value);
                deviceList.add(sensor);
            }else{
                if(StringUtils.isEmpty(name)) name = "Device " + uid;
                deviceList.add(new PhysicalDevice(uid, name, deviceType, DeviceCategory.GENERIC, value));
            }
        }

        if(board != null){
            board.setDevices(deviceList);
            for (Device device : deviceList) {
                if(device instanceof PhysicalDevice){
                    ((PhysicalDevice) device).setBoard(board);
                }
            }
        }

        if(board != null) devices.add(board);
        devices.addAll(deviceList);

	}

	@Override
	public String serializeExtraData() {
		StringBuffer sb = new StringBuffer();
        // [ID, PIN, VALUE, TARGET, SENSOR?, TYPE]

        sb.append(devices.size());
        sb.append(Command.DELIMITER);

        for (Device device : devices) {
            sb.append("[");
            sb.append(device.getName()).append(",");
            sb.append(device.getUid()).append(",");

            if(device instanceof PhysicalDevice){
                GpioInfo gpio = ((PhysicalDevice) device).getGpio();
                if(gpio != null){
                    sb.append(gpio.getPin()).append(",");
                }else {
                    sb.append(0).append(",");
                }
            }else {
                sb.append(0).append(",");
            }

            sb.append(device.getValue()).append(",");
            sb.append(-1).append(",");
            sb.append((device instanceof Sensor ? 1 : 0)).append(",");
            sb.append(device.getType().getCode());
            sb.append("]");
            sb.append(";");
        }

        if(! devices.isEmpty()){
            sb.deleteCharAt(sb.length()-1);
        }

		return sb.toString();
	}

    public int getLength() {
        return length;
    }

    public int getIndex() {
        return index;
    }

    public boolean isLast(){
        return index == length;
    }

    public static void resolveParents(Collection<Device> devices){

        Board board = null;

        for (Device device : devices) {
            if(device instanceof Board){
                board = (Board) device;
            }
        }

        if(board != null){
            for (Device device : devices) {
                if(device != board){
                    board.addDevice(device);
                }
            }
        }

    }
}
