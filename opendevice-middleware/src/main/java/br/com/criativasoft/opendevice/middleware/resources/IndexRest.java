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
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
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
import java.io.FileNotFoundException;
import java.net.URI;
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
    @Path("index.html") /* request send from ServiceWorker/cache*/
    @Produces({MediaType.TEXT_HTML})
    public Response index2(@Context AtmosphereResource res) throws Exception {
        return index(res);
    }

    @GET
    @Produces({MediaType.TEXT_HTML})
    public Response index(@Context AtmosphereResource res) throws Exception {

        AtmosphereRequest request = res.getRequest();
        Subject subject = (Subject) request.getAttribute(FrameworkConfig.SECURITY_SUBJECT);

        String location;

        if(!config.isAuthRequired() || subject.isAuthenticated()){

            SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
            if(savedRequest != null){
//                AtmosphereResponse response = res.getResponse();
//                WebUtils.redirectToSavedRequest(request, response, "admin.html");
                return Response.temporaryRedirect(new URI(savedRequest.getRequestURI()+"?"+savedRequest.getQueryString())).build();
            }

            location = "dist/index.html";
        }else{
            location = "login.html";
        }

        return resource(location);
    }

    @GET
    @Path("/login")
    @Produces({MediaType.TEXT_HTML})
    public Response login( @Context AtmosphereResource res) throws Exception {
        return resource("login.html");
    }

    @GET
    @Path("admin/invitation/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response invitation(@Context AtmosphereResource res, @PathParam("key") String key) throws Exception {

        // admin/invitation/asdasdasdasd

        return Response.ok().build();
    }

    private Response resource(String location) throws FileNotFoundException {
        // Find base path
        File path = null;

        if(connection instanceof WSServerConnection){
            List<String> webresources = ((WSServerConnection) connection).getWebresources();
            path = findInWebPath(webresources, location);
        }

        if(path != null){
            return Response.ok(new FileInputStream(path)).build();
        }else{
            throw new IllegalStateException(location + " not found in webapp path");
        }
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
