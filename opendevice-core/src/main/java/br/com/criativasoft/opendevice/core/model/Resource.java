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

package br.com.criativasoft.opendevice.core.model;

import java.io.Serializable;

/**
 * Base class for things
 * @author Ricardo JL Rufino
 *         Date: 30/07/17
 */

public interface Resource extends Serializable {

    public long getId();

    public void setId(long id);

    public int getUid();

    public void setUid(int uid);

    public String getName();

    public void setName(String name);

    public String getTitle();

    public void setTitle(String title);

    public long getLastUpdate();

    public void setLastUpdate(long lastUpdate);

    public String getApplicationID();

    public void setApplicationID(String applicationID);
}
