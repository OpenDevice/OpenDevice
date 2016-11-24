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

package br.com.criativasoft.opendevice.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory for OpenDevice
 *
 * @author Ricardo JL Rufino
 * @date 16/10/16
 */
public class ODevThreadFactory implements ThreadFactory {

    private final AtomicInteger count = new AtomicInteger();
    private final String name;

    /**
     * @param name - Name of Group
     */
    public ODevThreadFactory(String name) {
        this.name = name;
//        this.group = threads.get(name);
//        if(this.group == null){
//            this.group = new ThreadGroup(name);
//            this.group.setDaemon(true);
//            threads.put(name, this.group);
//        }
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, ("ODev-"+name+"-" + count.getAndIncrement()));
        t.setDaemon(true);
        return t;
    }
}
