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

package br.com.criativasoft.opendevice.core.model.test;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 11/01/16
 */
public class CategoryIPCam extends GenericCategory {

    // Acions
    public static ActionDef snapshot;
    public static ActionDef setPosition;
    public static ActionDef gpio;

    // Properties
    public static PropertyDef alias;
    public static PropertyDef cameraID;
    public static PropertyDef alarmStatus;
    public static PropertyDef syswifiMode;
    public static PropertyDef mac;
    public static PropertyDef wifimac;
    public static PropertyDef dnsStatus;
    public static PropertyDef authuser;
    public static PropertyDef externWifi;
    public static PropertyDef sdSize;
    public static PropertyDef sdFree;

    public static PropertyDef videoStreamURL;
    public static PropertyDef contrast;
    public static PropertyDef brightness;
    public static PropertyDef resolution;
    public static PropertyDef mode;
    public static PropertyDef flip;
    public static PropertyDef framerate;
    public static PropertyDef speed;
    public static PropertyDef bitrate;
    public static PropertyDef infrared;


    public String getDescription(){
        return CategoryIPCam.class.getSimpleName();
    }

    public void loadProperties(){

        alias = add("alias").mode(PropertyDef.Mode.READ_WRITE);
        cameraID = add("cameraID").mode(PropertyDef.Mode.READ_ONLY);
        alarmStatus = add("alarmStatus").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.STRING);
        syswifiMode = add("syswifiMode").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.BOOLEAN);
        mac = add("mac").mode(PropertyDef.Mode.READ_ONLY);
        wifimac = add("wifimac").mode(PropertyDef.Mode.READ_ONLY);
        dnsStatus = add("dnsStatus").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.BOOLEAN);
        authuser = add("authuser").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.BOOLEAN);
        externWifi = add("externWifi").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.BOOLEAN);
        sdSize = add("sdSize").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.NUMBER);
        sdFree = add("sdFree").mode(PropertyDef.Mode.READ_ONLY).type(PropertyType.NUMBER);

        videoStreamURL = add("videoStreamURL").group("Image").mode(PropertyDef.Mode.READ_ONLY);
        brightness = add("brightness").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        contrast = add("contrast").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        resolution = add("resolution").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        mode = add("mode").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        flip = add("flip").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        framerate = add("framerate").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        speed = add("speed").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        bitrate = add("bitrate").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);
        infrared = add("infrared").group("Image").mode(PropertyDef.Mode.READ_WRITE).type(PropertyType.NUMBER);

        // Action
        // =========================================================
        snapshot = action("snapshot");
        setPosition = action("setPosition");
        gpio = action("gpio");

    }


}
