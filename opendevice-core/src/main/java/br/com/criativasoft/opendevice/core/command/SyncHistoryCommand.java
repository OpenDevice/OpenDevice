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

package br.com.criativasoft.opendevice.core.command;

import br.com.criativasoft.opendevice.core.model.DeviceHistory;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Ricardo JL Rufino
 *         Date: 24/06/17
 */
public class SyncHistoryCommand extends Command {

    public static final CommandType TYPE = CommandType.SYNC_HISTORY;

    private Collection<DeviceHistory> list = new LinkedList<DeviceHistory>();

    public SyncHistoryCommand() {
        super(TYPE);
    }

    public SyncHistoryCommand(Collection<DeviceHistory> list) {
        super(TYPE);
        this.list.addAll(list);
    }

    public Collection<DeviceHistory> getList() {
        return list;
    }
}
