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

import com.sun.jersey.core.util.Base64;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.secnod.shiro.jaxrs.Auth;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;


/**
 * TODO: Add docs.
 *
 * curl -v -u admin:pass http://localhost:8181/api/auth
 * curl -H "Authorization: Bearer 1234" http://localhost:8181/device/list
 * http://localhost:8181/api/auth?username=admin&password=pass
 *
 * @author Ricardo JL Rufino
 * @date 08/09/16
 */
@Path("/api/auth")
public class AuthResource {

    public static final String TOKEN_HEADER = "AuthToken";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context AtmosphereResource res,
                          @Context HttpHeaders headers,
                          @QueryParam("username") String username, @QueryParam("password") String password) {

        AtmosphereRequest request = res.getRequest();
        Subject currentUser = (Subject) request.getAttribute(FrameworkConfig.SECURITY_SUBJECT);

        // Basic Auth
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            // Decode Authorization header user/pass
            byte[] decode = Base64.decode(authHeader.replace("Basic ", "").getBytes());
            String auth = new String(decode);
            username = auth.split(":")[0];
            password = auth.split(":")[1];
        }

        return doLogin(currentUser, username, password);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginForm(@Context AtmosphereResource res,
                              @Auth Subject currentUser,
                              @FormParam("username") String username,
                              @FormParam("password") String password) {

        return doLogin(currentUser, username, password);

    }

    private Response doLogin(Subject currentUser, String username, String password){

        System.out.println("isAuthenticated >>>  " + currentUser.isAuthenticated() + " || " + username + ":" + password);

        // TODO: validar usuario e senha - required !!

        if (!currentUser.isAuthenticated()) {
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            token.setRememberMe(false); //

            try {
                currentUser.login(token);

                // UUID.randomUUID().toString()

                // TODO: return token ?
                currentUser.getSession().setAttribute(TOKEN_HEADER, "");

            } catch (UnknownAccountException e) {
                return noCache(Response.status(Status.UNAUTHORIZED).entity("Unknown Account"));
            } catch (IncorrectCredentialsException e) {
                return noCache(Response.status(Status.FORBIDDEN).entity("Incorrect Credentials"));
            } catch (AuthenticationException e) {
                return noCache(Response.status(Status.UNAUTHORIZED).entity("Authentication failed"));
            }

        }

        if (currentUser.isAuthenticated()) {
            return noCache(Response.status(Status.OK).entity(currentUser.getSession().getAttribute(TOKEN_HEADER)));
        } else {
            return noCache(Response.status(Status.UNAUTHORIZED).entity("Authentication Fail"));
        }
    }

    /**
     * Avoid cache login request
     */
    private Response noCache(Response.ResponseBuilder resp) {
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setMaxAge(-1);
        cc.setMustRevalidate(true);
        return resp.cacheControl(cc).build();
    }


    @GET
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context AtmosphereResource res, @Auth Subject currentUser) {

        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            return noCache(Response.status(Status.OK).entity("Logout OK"));
        } else {
            return noCache(Response.status(Status.INTERNAL_SERVER_ERROR).entity("Not Logged"));
        }

    }


}