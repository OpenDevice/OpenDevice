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

package br.com.criativasoft.opendevice.core.dao.memory;

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.dao.EventHookDao;
import br.com.criativasoft.opendevice.core.event.EventHook;

import java.util.*;

/**
 * EventHookMemoryDao
 * @author Ricardo JL Rufino
 * @date 29/08/15.
 */
public class EventHookMemoryDao implements EventHookDao {

    private static Map<String, List<EventHook>> EventHookMap = new HashMap<String, List<EventHook>>();

    public static List<EventHook> getCurrentEventHooks(){

        String currentID = TenantProvider.getCurrentID();

        if(currentID == null) throw new IllegalStateException("TenantProvider.getCurrentID() is NULL");

        List<EventHook> hooks = EventHookMap.get(TenantProvider.getCurrentID());

        if(hooks == null){
            hooks = new LinkedList<EventHook>();
            EventHookMap.put(currentID, hooks);
        }

        return hooks;
    }

    @Override
    public EventHook getById(long id) {

        List<EventHook> list = getCurrentEventHooks();

        for (EventHook current : list){
            if(current.getId() == id){
                return current;
            }
        }

        return null;
    }

    private boolean exist(EventHook entity){
        if(entity.getId() > 0){
            EventHook find = getById(entity.getId());
            return find != null;
        }else{
            List<EventHook> list = getCurrentEventHooks();

            for (EventHook find : list){
                if(entity == find || entity.equals(find)){ // check if same instance.
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public void persist(EventHook entity) {
        List<EventHook> list = getCurrentEventHooks();

        if(!exist(entity)){
            list.add(entity);
        }

    }

    @Override
    public EventHook update(EventHook entity) {
        // nothing
        return null;
    }

    @Override
    public void delete(EventHook entity) {

        List<EventHook> list = getCurrentEventHooks();

        if(list.isEmpty()) return;

        boolean removed = list.remove(entity); // remove by instance

        // remove by ID.
        if(!removed && entity.getId() > 0){
            EventHook find = getById(entity.getId());
            if(find != null){
                list.remove(find);
            }
        }

    }

    @Override
    public void refresh(EventHook entity) {
        // nothing
    }

    @Override
    public List<EventHook> listAll() {
        return getCurrentEventHooks();
    }

    @Override
    public List<EventHook> listByDeviceUID(int id) {

        List<EventHook> foundList = new LinkedList<EventHook>();

        List<EventHook> list = getCurrentEventHooks();
        if(list.isEmpty()) return foundList;

        for (EventHook current : list){

            if(current.getDeviceIDs().contains(id)){
                foundList.add(current);
            }

        }

        return foundList;
    }
}
