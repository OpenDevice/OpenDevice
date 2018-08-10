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

import br.com.criativasoft.opendevice.core.util.StringUtils;
import br.com.criativasoft.opendevice.restapi.auth.BearerAuthRealm;
import br.com.criativasoft.opendevice.restapi.auth.BearerAuthToken;
import br.com.criativasoft.opendevice.restapi.auth.GoogleAuthToken;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import br.com.criativasoft.opendevice.wsrest.io.WebUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import java.io.ByteArrayInputStream;

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

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

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

            if (authorizationHeader != null && authorizationHeader.startsWith("Google")) {
                String token = authorizationHeader.substring("Google".length()).trim(); // Token

                GoogleAuthToken bearerToken = new GoogleAuthToken(token);

                try{
                    subject.login(bearerToken); // Use BearerTokenRealm
                    return request;
                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }

            }

            // Extract the token from the HTTP Authorization header (OAuth2)
            authorizationHeader = request.getHeaderValue(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
                String token = authorizationHeader.substring("Bearer".length()).trim(); // API_KEY

                BearerAuthToken bearerToken = new BearerAuthToken(token);

                try{
                    subject.login(bearerToken); // Use BearerTokenRealm
                    return request;
                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }
            }

            // ApiKey in Header (no 2 step auth)
            String header = request.getHeaderValue("ApiKey");
            if ((authorizationHeader != null && authorizationHeader.startsWith("ApiKey")) || header != null ) {
                String apiKey = null;
                if(header != null){
                    apiKey = header;
                }else{
                    apiKey = authorizationHeader.substring("ApiKey".length()).trim(); // API_KEY
                }

                if(StringUtils.isEmpty(apiKey)){
                    log.warn("ApiKey not found in Request");
                    throw new AuthenticationException("ApiKey Required");
                }

                BearerAuthToken bearerToken = new BearerAuthToken(apiKey, true);

                try{
                    subject.login(bearerToken); // Use BearerTokenRealm
                    return request;
                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }
            }

            // WebSocket HttpHeader Upgrade (JavaScript Library).
            header = request.getHeaderValue("Upgrade");
            if (header != null && header.contains("websocket")) {

                String apiKey  = path.substring(path.lastIndexOf('/')+1, path.length());

                BearerAuthToken bearerToken = new BearerAuthToken(apiKey, true);

                try{
                    subject.login(bearerToken); // Use BearerTokenRealm
                    return request;
                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }
            }

            // GoogleAssistant / Dialogflow Integration
            header = request.getHeaderValue("GoogleAssistant");
            if (header != null && header.contains("Dialogflow")) {

                JsonNode entity = request.getEntity(JsonNode.class);
                JsonNode userNode = entity.get("originalDetectIntentRequest").get("payload").get("user");

                if(userNode == null){
                    log.warn("User not found in Request");
                    throw new AuthenticationException("Invalid User / Token");
                }
                String token = userNode.get("accessToken").asText();

                BearerAuthToken bearerToken = new BearerAuthToken(token);

                // request.setEntityInputStream(new ByteArrayInputStream(entity.toString().getBytes()));
                request.setEntityInputStream(new ByteArrayInputStream(entity.toString().getBytes()));
                try{
                    subject.login(bearerToken); // Use BearerTokenRealm
                    return request;
                }catch (AuthenticationException e){
                    throw new AuthenticationException("Invalid AuthToken");
                }
            }
        }


        // NOTE: if not Autenticated, the UnauthenticatedException will throw (AuthorizationExceptionMap)

        return request;
    }

}
