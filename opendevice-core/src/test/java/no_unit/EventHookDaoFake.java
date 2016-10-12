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

package no_unit;

import br.com.criativasoft.opendevice.core.dao.EventHookDao;
import br.com.criativasoft.opendevice.core.event.EventHook;

import java.util.Arrays;
import java.util.List;


public class EventHookDaoFake implements EventHookDao {

    private EventHook hook;

    public EventHookDaoFake(EventHook hook) {
        this.hook = hook;
    }

    @Override
    public List<EventHook> listByDeviceUID(int id) {
        return Arrays.asList(hook);
    }

    @Override
    public EventHook getById(long id) {
        return null;
    }

    @Override
    public void persist(EventHook entity) {

    }

    @Override
    public void update(EventHook entity) {

    }

    @Override
    public void delete(EventHook entity) {

    }

    @Override
    public void refresh(EventHook entity) {

    }

    @Override
    public List<EventHook> listAll() {
        return null;
    }
}
