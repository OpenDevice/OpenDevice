/*
 * *****************************************************************************
 * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Ricardo JL Rufino - Initial API and Implementation
 * *****************************************************************************
 */

package br.com.criativasoft.opendevice.core.dao;

import br.com.criativasoft.opendevice.core.metamodel.DeviceHistoryQuery;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.core.model.DeviceCategory;
import br.com.criativasoft.opendevice.core.model.DeviceHistory;

import java.util.List;

/**
 * @author Ricardo JL Rufino
 * @date 27/08/14.
 */
public interface DeviceDao extends Dao<Device> {

    Device getByUID(int uid);

    DeviceCategory getCategoryByCode(int code);

    List<DeviceHistory> getDeviceHistory(DeviceHistoryQuery query);

    void persistHistory(DeviceHistory history);
}
