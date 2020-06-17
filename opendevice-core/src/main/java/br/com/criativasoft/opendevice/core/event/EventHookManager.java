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

package br.com.criativasoft.opendevice.core.event;

import br.com.criativasoft.opendevice.core.dao.EventHookDao;
import br.com.criativasoft.opendevice.core.dao.memory.EventHookMemoryDao;
import br.com.criativasoft.opendevice.core.event.impl.JavaDelegateEventHandler;
import br.com.criativasoft.opendevice.core.event.impl.SystemEventHandler;
import br.com.criativasoft.opendevice.core.filter.OnlyFilesWithExtension;
import br.com.criativasoft.opendevice.core.listener.DeviceListener;
import br.com.criativasoft.opendevice.core.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino
 * @date 28/08/15.
 */
public class EventHookManager implements DeviceListener {

    private static final Logger log = LoggerFactory.getLogger(EventHookManager.class);

    private static List<EventHandler> handlers;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private EventHookDao dao = new EventHookMemoryDao();


    public EventHookManager() {
        registerHandler(JavaDelegateEventHandler.class);
        registerHandler(SystemEventHandler.class);
    }

    public static void registerHandler(Class<? extends EventHandler> handler) {
        try {
            if (handlers == null) handlers = new LinkedList<EventHandler>();
            handlers.add(handler.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceChanged(Device device) {

        List<EventHook> hooks = getDao().listByDeviceUID(device.getUid());

        EventContext context = new EventContext();
        context.put("device", device);

        if (!hooks.isEmpty()) {

            for (EventHook hook : hooks) {
                try {
                    context.put("hook.id", hook.getDeviceIDs());
                    context.put("hook.name", hook.getName());
                    execute(hook, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    @Override
    public void onDeviceRegistred(Device device) {

    }

    protected void execute(final EventHook hook, final EventContext context) throws EventException {

        final String handler = hook.getHandler();

        final EventHandler eventHandler = getEventHandler(hook.getType());

        if (eventHandler != null) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.debug("Executing hook: " + hook.getName() + " using: " + eventHandler.getClass().getSimpleName());
                        eventHandler.execute(handler, context);
                    } catch (EventException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });


        } else {

            throw new EventException("Handler Implementation for type: " + hook.getType() + " not found ! Hook: " + hook.getName());

        }

    }

    public void scanHooks(File folder, String ext) throws EventException {

        List<EventHook> hooks = new LinkedList<EventHook>();

        if (folder.isDirectory()) {

            File[] list = folder.listFiles(new OnlyFilesWithExtension(ext));

            for (File file : list) {
                hooks.add(addHook(file));
            }

        }

    }

    public EventHook addHook(File file) throws EventException {

        EventHook hook = new FileHookScanner().parse(file);

        if (hook.getName() != null && hook.getType() != null) {
            getDao().persist(hook);
        } else {
            throw new EventException("The EventHook doesn't have name or type : " + file.getPath());
        }


        return hook;

    }


    protected EventHandler getEventHandler(String handlerType) {

        for (EventHandler handler : handlers) {
            if (handler.getHandlerType().equals(handlerType)) return handler;
        }

        return null;
    }

    public EventHookDao getDao() {
        return dao;
    }

    public void setDao(EventHookDao dao) {
        this.dao = dao;
    }


}
