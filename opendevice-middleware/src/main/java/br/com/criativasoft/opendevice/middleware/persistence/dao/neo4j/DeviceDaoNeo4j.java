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

package br.com.criativasoft.opendevice.middleware.persistence.dao.neo4j;

import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.metamodel.PeriodType;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.middleware.persistence.dao.jpa.DeviceDaoJPA;

import javax.persistence.Query;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class DeviceDaoNeo4j extends DeviceDaoJPA {

    // FIXME: Use tentantIDs
    protected DeviceHistory getDeviceHistoryAggregate(DeviceHistoryQuery params) {

        Device device = getByUID(params.getDeviceID());

        String function = params.getAggregation().getFunction();

        StringBuilder sbquery = new StringBuilder("MATCH (h:DeviceHistory) where h.deviceID = {deviceID}");

        if (params.getPeriodType() != PeriodType.RECORDS) {
            sbquery.append(" and (h.timestamp >= {start} and h.timestamp <= {end})");
            sbquery.append(" RETURN " + function + "(h.value)");
        } else { // by: RECORDS
            sbquery.append(" WITH h ORDER BY h.timestamp DESC LIMIT " + params.getPeriodValue());
            sbquery.append(" MATCH h RETURN " + function + "(h.value)");
        }

        Query query = em().createNativeQuery(sbquery.toString());
        query.setParameter("deviceID", new Long(device.getId()));

        if (params.getPeriodType() != PeriodType.RECORDS) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(params.getPeriodType().getValue(), -params.getPeriodValue());
            query.setParameter("start", calendar.getTimeInMillis());
            query.setParameter("end", new Date().getTime());
        }

        List list = query.getResultList();

        Object value;

        if (list.isEmpty()) value = 0;
        else value = list.get(0);

        DeviceHistory history = new DeviceHistory();
        history.setDeviceID(params.getDeviceID());
        history.setTimestamp(Calendar.getInstance().getTimeInMillis());
        if (value instanceof Long) history.setValue((Long) value);
        if (value instanceof Double) history.setValue((Double) value);
        if (value instanceof String) history.setValue(Double.parseDouble((String) value));

        return history;
    }


}
