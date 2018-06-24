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

package br.com.criativasoft.opendevice.middleware.rules.action;

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.model.Device;
import br.com.criativasoft.opendevice.middleware.jobs.AbstractAction;
import br.com.criativasoft.opendevice.middleware.jobs.ActionException;
import br.com.criativasoft.opendevice.middleware.model.actions.ControlActionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ricardo JL Rufino
 * @date 04/11/16
 */
public class ControlAction extends AbstractAction<ControlActionSpec> {

    private static final Logger log = LoggerFactory.getLogger(ControlAction.class);

    @Override
    public void execute() throws ActionException {

        if(log.isDebugEnabled()) log.debug("Executing [Device "+ spec.getResourceID()+" -> "+spec.getValue()+"]");

        long resourceID = spec.getResourceID();

        Device device = ODev.findDevice((int) resourceID);

        if(device == null) throw new ActionException("Resource nor found !");

        device.setValue(spec.getValue());
    }
}
