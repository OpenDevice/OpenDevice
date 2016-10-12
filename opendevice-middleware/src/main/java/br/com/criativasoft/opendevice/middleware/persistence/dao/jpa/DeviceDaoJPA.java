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

package br.com.criativasoft.opendevice.middleware.persistence.dao.jpa;

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.dao.DeviceDao;
import br.com.criativasoft.opendevice.core.metamodel.AggregationType;
import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.metamodel.PeriodType;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;

import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
public abstract class DeviceDaoJPA extends GenericJpa<Device> implements DeviceDao{

    public DeviceDaoJPA() {
        super(Device.class);
    }

    @Override
    public Device getByUID(int uid) {
        TypedQuery<Device> query = em().createQuery("select x from Device x where x.uid = :p1 and x.applicationID = :TENANT", Device.class);
        query.setParameter("p1", uid);
        query.setParameter("TENANT", TenantProvider.getCurrentID());

        try {
            return query.getSingleResult();
        }catch (NoResultException ex){
            return null;
        }
    }

    @Override
    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery params) {

        Device device = getByUID(params.getDeviceID());

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

            TypedQuery<DeviceHistory> query = em().createQuery(sbquery.toString(), DeviceHistory.class);
            query.setParameter("deviceID",  new Long(device.getId()));

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

    protected abstract DeviceHistory getDeviceHistoryAggregate(DeviceHistoryQuery params);

    @Override
    public List<Device> listAll() {
        TypedQuery<Device> query = em().createQuery("select x from Device x where x.applicationID = :TENANT", Device.class);

        query.setParameter("TENANT", TenantProvider.getCurrentID());

        return query.getResultList();
    }

    @Override
    public DeviceCategory getCategoryByCode(int code) {
        return em().find(DeviceCategory.class, code);
    }

    @Override
    public void persist(Device entity) {

        // Start transaction if not active, this is necessary because the persist can be called in a unmanaged context
        EntityTransaction tx = em().getTransaction();

        boolean active = tx.isActive();

        if(!active) tx.begin();

        super.persist(entity);

        if(!active) tx.commit();

    }
}

