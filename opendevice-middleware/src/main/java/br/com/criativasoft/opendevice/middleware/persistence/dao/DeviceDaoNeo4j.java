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

package br.com.criativasoft.opendevice.middleware.persistence.dao;

import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.AggregationType;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.metamodel.PeriodType;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class DeviceDaoNeo4j implements DeviceDao {

    @PersistenceContext
    private EntityManager em;

    public DeviceDaoNeo4j(EntityManager em) {
        this.em = em;
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Override
    public Device getByUID(int uid) {
        return em.find(Device.class, uid);
    }

    @Override
    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery params) {

        AggregationType aggregation = params.getAggregation();

        if(aggregation != null && aggregation != AggregationType.NONE){
            List<DeviceHistory> list = new ArrayList<DeviceHistory>();
            list.add(getDeviceHistoryAggregate(params));
            return list;
        }else{

            StringBuilder sbquery = new StringBuilder("from DeviceHistory where deviceID = :deviceID");

            if(params.getPeriodType() != PeriodType.RECORDS){
                sbquery.append(" and timestamp between :start and :end");
                sbquery.append(" ORDER BY timestamp ASC");
            }else{
                sbquery.append(" ORDER BY timestamp DESC");
            }

            TypedQuery<DeviceHistory> query = em.createQuery(sbquery.toString(), DeviceHistory.class);
            query.setParameter("deviceID",  new Long(params.getDeviceID()));

            if(params.getPeriodType() == PeriodType.RECORDS){
                query.setMaxResults(params.getPeriodValue());
            }else{
                Calendar calendar = Calendar.getInstance();
                calendar.add(params.getPeriodType().getValue(), -params.getPeriodValue());
                query.setParameter("start", calendar.getTimeInMillis());
                query.setParameter("end", new Date().getTime());
            }

            List<DeviceHistory> list = query.getResultList();
            return list;
        }


    }

    private DeviceHistory getDeviceHistoryAggregate(DeviceHistoryQuery params) {

        String function = params.getAggregation().getFunction();

        StringBuilder sbquery = new StringBuilder("MATCH (h:DeviceHistory) where h.deviceID = {deviceID}");

        if(params.getPeriodType() != PeriodType.RECORDS){
            sbquery.append(" and (h.timestamp >= {start} and h.timestamp <= {end})");
            sbquery.append(" RETURN "+function+"(h.value)");
        }else{ // by: RECORDS
            sbquery.append(" WITH h ORDER BY h.timestamp DESC LIMIT " + params.getPeriodValue());
            sbquery.append(" MATCH h RETURN "+function+"(h.value)");
        }

        Query query = em.createNativeQuery(sbquery.toString());
        query.setParameter("deviceID",  new Long(params.getDeviceID()));

        if(params.getPeriodType() != PeriodType.RECORDS){
            Calendar calendar = Calendar.getInstance();
            calendar.add(params.getPeriodType().getValue(), -params.getPeriodValue());
            query.setParameter("start", calendar.getTimeInMillis());
            query.setParameter("end", new Date().getTime());
        }

        List list = query.getResultList();

        Object value;

        if(list.isEmpty()) value = 0;
        else value = list.get(0);

        DeviceHistory history = new DeviceHistory();
        history.setDeviceID(params.getDeviceID());
        history.setTimestamp(Calendar.getInstance().getTimeInMillis());
        if(value instanceof Long) history.setValue((Long) value);
        if(value instanceof Double) history.setValue((Double) value);
        if(value instanceof String) history.setValue(Double.parseDouble((String)value));

        return history;
    }

    @Override
    public Device getById(long id) {
        return em.find(Device.class, id);
    }

    @Override
    public void persist(Device entity) {
        em.persist(entity);
    }

    @Override
    public void update(Device entity) {
        em.merge(entity);
    }

    @Override
    public void delete(Device entity) {
        em.remove(entity);
    }

    @Override
    public void refresh(Device entity) {
        em.refresh(entity);
    }

    @Override
    public List<Device> listAll() {
        return em.createQuery("from Device", Device.class).getResultList();
    }
}
