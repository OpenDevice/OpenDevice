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
import br.com.criativasoft.opendevice.core.metamodel.OrderType;
import br.com.criativasoft.opendevice.core.metamodel.PeriodType;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.middleware.persistence.HibernateProvider;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * @author Ricardo JL Rufino
 * @date 12/10/16
 */
public abstract class DeviceJPA extends GenericJpa<Device> implements DeviceDao{

    public DeviceJPA() {
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
    public Device getByName(String name) {
        TypedQuery<Device> query = em().createQuery("select x from Device x where x.name = :p1 and x.applicationID = :TENANT", Device.class);
        query.setParameter("p1", name);
        query.setParameter("TENANT", TenantProvider.getCurrentID());

        try {
            return query.getSingleResult();
        }catch (NoResultException ex){
            return null;
        }
    }

    @Override
    public int getNextUID() {

        TypedQuery<Integer> query = em().createQuery("select MAX(x.uid) from Device x where x.applicationID = :TENANT", Integer.class);
        query.setParameter("TENANT", TenantProvider.getCurrentID());
        Integer val = query.getSingleResult();
        if(val == null) val = 0;

        try {
            return val + 1;
        }catch (NoResultException ex){
            return 1;
        }

    }

    @Override
    public void delete(Device entity) {
        deleteHistory(entity);
        super.delete(entity);
    }

    @Override
    public void deleteHistory(Device device) {
        int deletedCount = em().createQuery("DELETE FROM DeviceHistory where deviceID = :uid").setParameter("uid", device.getId()).executeUpdate();
    }

    @Override
    public List<DeviceHistory> getOfflineHistory() {
        TypedQuery<DeviceHistory> query = em().createQuery("select x from DeviceHistory x where x.needSync = :p1 and x.applicationID = :TENANT", DeviceHistory.class);
        query.setParameter("p1", true);
        query.setParameter("TENANT", TenantProvider.getCurrentID());
        return query.getResultList();
    }

    @Override
    public List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery params) {

        // FIXME: Use tentantIDs

        AggregationType aggregation = params.getAggregation();

        if(aggregation != null && aggregation != AggregationType.NONE){
            List<DeviceHistory> list = new LinkedList<DeviceHistory>();
            list.add(getDeviceHistoryAggregate(params));
            return list;
        }else{

            StringBuilder jpql = new StringBuilder("from DeviceHistory where deviceID = :deviceID");

            if(params.getPeriodType() != PeriodType.RECORDS){
                jpql.append(" and timestamp between :start and :end");
            }

            OrderType order = params.getOrder();

            // Default order
            if(order == null){
                order = OrderType.ASC;
            }

            if(order == OrderType.ASC){
                jpql.append(" ORDER BY timestamp ASC");
            }else{
                jpql.append(" ORDER BY timestamp DESC");
            }

            TypedQuery<DeviceHistory> query = em().createQuery(jpql.toString(), DeviceHistory.class);
            query.setParameter("deviceID",  params.getDeviceID());

            if(params.getPeriodType() == PeriodType.RECORDS){
                params.setMaxResults(params.getPeriodValue());
            }else{
                Calendar calendar = Calendar.getInstance();
                Date end;
                if(params.getPeriodEnd() != null){
                    end = params.getPeriodEnd();
                }else{
                    end = new Date();
                    params.setPeriodEnd(end);
                }

                calendar.setTime(end);

                // Calculate new time from 'end' to 'PeriodType/PeriodValue'
                calendar.add(params.getPeriodType().getValue(), -params.getPeriodValue());
                query.setParameter("start", calendar.getTime().getTime());
                query.setParameter("end" , end.getTime());

//                System.err.println("query: "+new Date(calendar.getTimeInMillis())+" - "+end);
            }

            int maxForAnalisys = params.getMaxResults(10000); //get default

            query.setMaxResults(maxForAnalisys);

            System.out.println("maxForAnalisys :" + maxForAnalisys);
            System.out.println("maxForAnalisys :" + params.getPeriodType());

            // Paginated query
            if(params.getPageNumber() > 0){
                query.setFirstResult((params.getPageNumber()-1) * maxForAnalisys);
            }

            List<DeviceHistory> list = query.getResultList();

            // Data must be sorted to show in chart's
            // NOTE: Show last data frist !!
//            Collections.sort(list, new Comparator<DeviceHistory>() {
//                @Override
//                public int compare(DeviceHistory o1, DeviceHistory o2) {
//                    return (int) (o1.getTimestamp() - o2.getTimestamp());
//                }
//            });

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

        if(entity.getId() <= 0) entity.setDateCreated(new Date());
        else entity.setLastUpdate(System.currentTimeMillis());

        super.persist(entity);

    }

    @Override
    public void persistHistory(DeviceHistory history) {
        em().persist(history);
    }

    @Override
    public EntityManager em() {
        return HibernateProvider.getInstance();
    }
}

