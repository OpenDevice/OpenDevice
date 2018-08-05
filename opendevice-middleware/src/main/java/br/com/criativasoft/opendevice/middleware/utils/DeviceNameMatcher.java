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

package br.com.criativasoft.opendevice.middleware.utils;

import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.util.StringUtils;
import org.apache.commons.codec.language.Metaphone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by ricardo on 06/05/17.
 */
public class DeviceNameMatcher {

    private Metaphone soundex = new Metaphone();

    private Collection<Device> devices;

    private boolean inputHasOnlyDevices;

    public DeviceNameMatcher(Collection<Device> devices, boolean inputHasOnlyDevices) {
        this.devices = devices;
        this.inputHasOnlyDevices = inputHasOnlyDevices;
    }

    private static final List<String> actions = new ArrayList<>();

    static{

        // ON (pt_BR)
        actions.add("ativar");
        actions.add("ligar");
        actions.add("acender");
        actions.add("abrir");

        // ON (EN)
        actions.add("turn on");
        actions.add("open");


        // OFF (pt_BR)
        actions.add("desativar");
        actions.add("ligar");
        actions.add("apagar");
        actions.add("fechar");

        // OFF (EN)
        actions.add("turn off");
        actions.add("close");
        actions.add("deactivate");

    }

    public Device process(String input){

        input = sanitize(input);

        if(!inputHasOnlyDevices) {
            input = removeActions(input);
        }

        List<String> inputs = Arrays.asList(input.split(" "));

        // Find device name (exact match)
        for (Device device : devices) {
            String name = sanitize(device.getTitle());

            if(input.contains(name)){
                return device;
            }

            name = sanitize(device.getName()); // TODO: Change this to international property

            if(input.contains(name)){
                return device;
            }
        }

        // Find device name (block match)
        Device device = findDeviceMatching(inputs, /*soundMatch=*/false);

        // Find device name (soundex match)
        if(device == null){
            List<String> inputsSDX = new ArrayList<>();
            for (String s : inputs) {
                inputsSDX.add(soundex.encode(s));
            }

            device = findDeviceMatching(inputsSDX, /*soundMatch=*/true);
        }

        return device;

    }

    private String removeActions(String input) {
        String articleA = " a ";
        String articleO = " o ";

        for (String special : actions) {

            int index = input.indexOf(special);

            if (index >= 0){

                if(input.contains(articleA)){ // Ligar a Luz da Sala
                    index = input.indexOf(articleA);
                } else if(input.contains(articleO)){ // Ligar o Ventilardor
                    index = input.indexOf(articleO);
                }

                input = input.substring(index);
                return input;
            }
        }

        return input;
    }

    protected Device findDeviceMatching(List<String> inputs, boolean soundMatch){
        for (Device device : devices) {
            String name = sanitize(device.getTitle());
            String[] blocks = name.split(" ");
            if(allMatch(inputs, blocks, soundMatch)){
                return device;
            }

            // Name 2
            name = sanitize(device.getName()); // TODO: Change this to international property
            blocks = name.split(" ");
            if(allMatch(inputs, blocks, soundMatch)){
                return device;
            }
        }

        return null;
    }

    protected String sanitize(String input){
        return StringUtils.removeSpecialChars(input.toLowerCase());
    }

    /**
     */
    protected boolean allMatch(List<String> input, String[] find, boolean soundMatch){

        int totalMatch = 0;

        for (String key : find) {
            if(soundMatch) key = soundex.encode(key);
            if(input.contains(key)) totalMatch++;
        }

        return (totalMatch == find.length);
    }

    public void setDevices(Collection<Device> devices) {
        this.devices = devices;
    }

}