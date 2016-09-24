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

package br.com.criativasoft.opendevice.middleware.resources;

import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * TODO: Add docs.
 *
 * @author Ricardo JL Rufino
 * @date 09/09/16
 */
@Path("/")
public class IndexRest {

    @Inject
    OpenDeviceConfig config;

    @GET
    public Response index(@PathParam("id") long id, @Context AtmosphereResource res) throws Exception {

        AtmosphereRequest request = res.getRequest();
        Subject subject = (Subject) request.getAttribute(FrameworkConfig.SECURITY_SUBJECT);

        URI location;
        if(!config.isAuthRequired() || subject.isAuthenticated()){
            location = new java.net.URI("admin.html");
        }else{
            location = new java.net.URI("login.html");
        }

        System.out.println("Index: isAuthenticated = " + subject.isAuthenticated() + " -> " + location);

        return Response.temporaryRedirect(location).build();
    }

}
