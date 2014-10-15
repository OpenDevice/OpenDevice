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

package br.com.criativasoft.opendevice.middleware.test;

import br.com.criativasoft.opendevice.core.DeviceManager;
import br.com.criativasoft.opendevice.core.command.CommandType;
import br.com.criativasoft.opendevice.core.command.DeviceCommand;
import br.com.criativasoft.opendevice.core.model.Sensor;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Generate fake data
 */
public class FakeSensorSimulator extends Thread {

    private DeviceManager manager;

    private Random random = new Random();
    private List<Integer> list = new LinkedList<Integer>();
    private long delay = 500;

    public FakeSensorSimulator(long delay, DeviceManager manager, Integer... ids) {
        this.delay = delay;
        this.manager = manager;
        list.addAll(Arrays.asList(ids));
    }


    @Override
    public void run() {

        while(true){

            try {

                Thread.sleep(delay);

                if(manager.isConnected()){
                    for (Integer id : list) {
                        manager.send(new DeviceCommand(CommandType.ANALOG, id, nextIntInRange(0, 255, random)));
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    int nextIntInRange(int min, int max, Random rng) {
        if (min > max) {
            throw new IllegalArgumentException("Cannot draw random int from invalid range [" + min + ", " + max + "].");
        }
        int diff = max - min;
        if (diff >= 0 && diff != Integer.MAX_VALUE) {
            return (min + rng.nextInt(diff + 1));
        }
        int i;
        do {
            i = rng.nextInt();
        } while (i < min || i > max);
        return i;
    }


}
