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

package br.com.criativasoft.opendevice.wsrest.resource;

import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino (09/09/16)
 */
@Path("/api")
public class ApiInfoResource {

    @Inject
    OpenDeviceConfig config;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String index(@Auth Subject subject) {
        return "<html> " + "<title>" + "OpenDevice" + "</title>"
                +"<body><h2>" + "API is Working!</h2>" +
                "Auth Enabled  = " + config.isAuthRequired() +", Authenticated  = " + subject.isAuthenticated()
                +"</body>" + "</html> ";
    }

}
