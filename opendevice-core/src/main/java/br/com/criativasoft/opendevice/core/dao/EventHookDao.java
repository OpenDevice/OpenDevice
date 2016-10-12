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

package br.com.criativasoft.opendevice.core.dao;

import br.com.criativasoft.opendevice.core.event.EventHook;

import java.util.List;

/**
 * Dao for  {@link EventHook}
 *
 * @author Ricardo JL Rufino
 * @date 28/08/15.
 */
public interface EventHookDao extends Dao<EventHook> {

    List<EventHook> listByDeviceUID(int uid);
}
