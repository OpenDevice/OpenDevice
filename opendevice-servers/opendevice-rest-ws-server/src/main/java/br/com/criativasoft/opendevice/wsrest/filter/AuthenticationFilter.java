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

package br.com.criativasoft.opendevice.wsrest.filter;

import br.com.criativasoft.opendevice.restapi.auth.BearerAuthRealm;
import br.com.criativasoft.opendevice.restapi.auth.BearerAuthToken;
import br.com.criativasoft.opendevice.restapi.auth.GoogleAuthToken;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import br.com.criativasoft.opendevice.wsrest.io.WebUtils;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

/**
 * Check the authentication token via the Bearer header, and performs validation using
 * {@link BearerAuthRealm BearerTokenRealm}. <br/>
 * Validation rules are enforced by class: {@link org.secnod.shiro.jersey.ShiroAnnotationResourceFilter ShiroAnnotationResourceFilter} using annoted resources
 * @author Ricardo JL Rufino
 * @date 10/09/16
 */
@Provider
public class AuthenticationFilter implements ContainerRequestFilter {

    public static final String TOKEN_CACHE = "AuthTokenCache";

    @Inject
    private UserDao userDao;

    @Override
    public ContainerRequest filter(ContainerRequest request) {

        // Ignore Web Resources.
        String path = request.getPath();
        if(WebUtils.isWebResource(path)){
            return request;
        }

        Subject subject = SecurityUtils.getSubject();

        Session session = subject.getSession(false);

        if(session != null && subject.isAuthenticated()){
            session.touch();
            return request;
        }

        if(!subject.isAuthenticated()) {

            // Google OAuth ( Ex.: Alexa Skill )
            String authorizationHeader = request.getHeaderValue(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Google ")) {
                String token = authorizationHeader.substring("Google".length()).trim(); // Token

                GoogleAuthToken bearerToken = new GoogleAuthToken(token);

                try{
                    subject.login(bearerToken); // Use BearerTokenRealm

                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }

            }

            // Extract the token from the HTTP Authorization header
            authorizationHeader = request.getHeaderValue(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring("Bearer".length()).trim(); // API_KEY

                BearerAuthToken bearerToken = new BearerAuthToken(token);

                try{
                    subject.login(bearerToken); // Use BearerTokenRealm

                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }
            }

        }


        // NOTE: if not Autenticated, the UnauthenticatedException will throw (AuthorizationExceptionMap)

        return request;
    }

}
