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

import br.com.criativasoft.opendevice.middleware.jobs.AbstractAction;
import br.com.criativasoft.opendevice.middleware.jobs.ActionException;
import br.com.criativasoft.opendevice.middleware.model.actions.WebHookActionSpec;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Ricardo JL Rufino
 *         Date: 14/01/18
 */
public class WebHookAction extends AbstractAction<WebHookActionSpec> {

    private static final Logger log = LoggerFactory.getLogger(ControlAction.class);

    @Override
    public void execute() throws ActionException {

        log.debug("Executing ...");

        String url = replaceDeviceVariables(getSpec().getUrl());

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        request.addHeader("User-Agent", "OpenDevie");
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            throw new ActionException(e.getMessage(), e);
        }

        log.debug("Response Code ..." + response.getStatusLine().getStatusCode());

    }

}
