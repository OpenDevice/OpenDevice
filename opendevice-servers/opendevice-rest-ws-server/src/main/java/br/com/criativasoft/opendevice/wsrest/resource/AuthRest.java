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

import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import com.sun.jersey.core.util.Base64;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.FrameworkConfig;
import org.secnod.shiro.jaxrs.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.UUID;


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
public class AuthRest {

    private static final Logger LOG = LoggerFactory.getLogger(AuthRest.class);

    public static final String TOKEN_HEADER = "AuthToken";
    public static final String TOKEN_CACHE = "AuthTokenCache";
    public static final String SESSION_ID = "JSESSIONID";

    @Inject
    private AccountDao dao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context AtmosphereResource res,
                          @Context HttpHeaders headers,
                          @QueryParam("username") String username, @QueryParam("password") String password) {

        AtmosphereRequest request = res.getRequest();
        Subject currentUser = (Subject) request.getAttribute(FrameworkConfig.SECURITY_SUBJECT);

        // Basic Auth (Token)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            // Decode Authorization header user/pass
            byte[] decode = Base64.decode(authHeader.replace("Basic ", "").getBytes());
            String auth = new String(decode);
            username = auth.split(":")[0]; // ApiKey
            //password = auth.split(":")[1]; // ingored

            return doLogin(currentUser, username, null, true);

        }else{ // Query param AUTH (user, pass)

            return doLogin(currentUser, username, password, true);
        }

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginForm(@Context AtmosphereResource res,
                              @Auth Subject currentUser,
                              @FormParam("username") String username,
                              @FormParam("password") String password) {


        Response response = doLogin(currentUser, username, password, false);

        if(currentUser.isAuthenticated()){

            // Generate Cookie to indentify user on Shiro (NewShiroInterceptor)
            Session session = currentUser.getSession(true); // this will force session creation
            javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(AuthRest.SESSION_ID, (String) session.getId());
            cookie.setPath("/");
            res.getResponse().addCookie(cookie);

        }

        return response;

    }

    private Response doLogin(Subject currentUser, String username, String password, boolean isToken){

        LOG.debug("Using token ("+isToken+"), username : " + username);

        Account account = null;
        String authtoken = null;

//        List<Account> list = dao.listAll();
//        for (Account account1 : list) {
//
//            Set<ApiKey> keys = account1.getKeys();
//
//            System.out.println(" -- Acount: " + account1.getUsername() + " > " +account1.getUuid());
//            for (ApiKey key : keys) {
//                System.out.println(" - " + key.getKey());
//            }
//
//        }

        if(isToken){

            account = dao.getAccountByApiKey(username);

            // Generate and cache the 'AuthToken', this will be used in AuthenticationFilter
            // TODO: Need configure expire using EhCache
            if(account != null){
                authtoken = UUID.randomUUID().toString();
                DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
                Cache<Object, Object> cache = securityManager.getCacheManager().getCache(TOKEN_CACHE);
                cache.put(authtoken, username); // username == Api_Key
            }

        // Form auth
        }else if (!currentUser.isAuthenticated()){

            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            token.setRememberMe(false); // to be remembered across sessions

            try {

                currentUser.login(token);

                // currentUser.getSession(true).setTimeout(xxxxx);

                account = new Account(currentUser.getPrincipal().toString(), "x"); // fake, only to send response

            } catch (UnknownAccountException e) {
                return noCache(Response.status(Status.UNAUTHORIZED).entity("Unknown Account"));
            } catch (IncorrectCredentialsException e) {
                return noCache(Response.status(Status.FORBIDDEN).entity("Incorrect Credentials"));
            } catch (AuthenticationException e) {
                return noCache(Response.status(Status.UNAUTHORIZED).entity("Authentication failed"));
            }


        }


        if (account != null) {
            return noCache(Response.status(Status.OK).entity(authtoken));
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