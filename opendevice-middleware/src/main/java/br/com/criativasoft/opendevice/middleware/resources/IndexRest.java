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

import br.com.criativasoft.opendevice.connection.ServerConnection;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.wsrest.WSServerConnection;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Index Controller
 *
 * @author Ricardo JL Rufino
 * @date 09/09/16
 */
@Path("/")
public class IndexRest {

    @Inject
    OpenDeviceConfig config;

    @Inject
    private ServerConnection connection;

    @GET
    @Produces({MediaType.TEXT_HTML})
    public InputStream index(@Context AtmosphereResource res) throws Exception {

        AtmosphereRequest request = res.getRequest();
        Subject subject = (Subject) request.getAttribute(FrameworkConfig.SECURITY_SUBJECT);

        String location;
        File path = null;
        if(!config.isAuthRequired() || subject.isAuthenticated()){
            location = "admin.html";
        }else{
            location = "login.html";
        }

        // Find base path
        if(connection instanceof WSServerConnection){
            List<String> webresources = ((WSServerConnection) connection).getWebresources();
            path = findInWebPath(webresources, location);
        }

//        if(!config.isAuthRequired() || subject.isAuthenticated()){
//            location = new java.net.URI("admin.html");
//        }else{
//            location = new java.net.URI("login.html");
//        }
//        return Response.temporaryRedirect(location).build();

        if(path != null){
            return new FileInputStream(path);
        }else{
            throw new IllegalStateException(location + " not found in webapp path");
        }


    }

    @GET
    @Path("/admin")
    @Produces({MediaType.TEXT_HTML})
    public InputStream admin( @Context AtmosphereResource res) throws Exception {
        return index(res);
    }

    @GET
    @Path("admin/invitation/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response invitation(@Context AtmosphereResource res, @PathParam("key") String key) throws Exception {

        // admin/invitation/asdasdasdasd

        return Response.ok().build();
    }


    private File findInWebPath(List<String> webresources, String file){
        for (String webresource : webresources) {
            File path = new File(webresource, file);
            if(path.exists()){
                return path;
            }
        }

        return null;
    }





}
