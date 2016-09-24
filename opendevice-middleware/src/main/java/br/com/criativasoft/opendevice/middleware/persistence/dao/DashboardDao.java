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

import br.com.criativasoft.opendevice.core.dao.Dao;
import br.com.criativasoft.opendevice.middleware.model.Dashboard;
import br.com.criativasoft.opendevice.middleware.model.DashboardItem;

import java.util.List;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 07/05/15.
 */
public interface DashboardDao extends Dao<Dashboard> {

    public List<DashboardItem> listItems(long id);

    public void persistItem(DashboardItem DashboardItem);

    public void deleteItem(DashboardItem DashboardItem);

    public DashboardItem getItemByID(long id);

    public void activate(Dashboard dashboard);

}
