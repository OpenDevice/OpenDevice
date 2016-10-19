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

package br.com.criativasoft.opendevice.core.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 *
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="uid")
public class PhysicalDevice extends Device {

    @Transient
    @JsonIgnore
    protected volatile GpioInfo gpio;

    @ManyToOne(fetch= FetchType.LAZY)
    @JsonIdentityReference(alwaysAsId = true)
    private Board board;

    public PhysicalDevice() {
    }

    public PhysicalDevice(int uid) {
        super(uid);
    }

    public PhysicalDevice(int uid, DeviceType type) {
        super(uid, type);
    }

    public PhysicalDevice(int uid, String name, DeviceType type) {
        super(uid, name, type);
    }

    public PhysicalDevice(int uid, String name, DeviceType type, DeviceCategory category) {
        super(uid, name, type, category);
    }

    public PhysicalDevice(int uid, String name, DeviceType type, DeviceCategory category, long value) {
        super(uid, name, type, category, value);
    }

    /**
     * Configure GPIO for this device. <br/>
     * This type of configuration is ideal for devices like the Raspberry.<br/>
     * Or when it is used to save the settings in the EPROM of low processing power devices
     * @param pin
     * @return
     */
    public Device gpio(int pin){
        this.gpio = new GpioInfo(pin);
        return this;
    }

    public GpioInfo getGpio() {
        return gpio;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "Physical[UID:"+getUid()+", Name:"+getName()+", Value:"+getValue()+", Type:" + getType()+"]";
    }
}
