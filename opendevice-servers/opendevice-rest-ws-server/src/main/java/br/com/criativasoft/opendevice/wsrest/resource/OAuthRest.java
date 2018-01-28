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


import br.com.criativasoft.opendevice.restapi.auth.AccountPrincipal;
import br.com.criativasoft.opendevice.restapi.model.ApiKey;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.wsrest.filter.AuthenticationFilter;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author Ricardo JL Rufino
 * Date: 14/10/17
 */
@Path("/oauth2")
@Produces(MediaType.APPLICATION_JSON)
public class OAuthRest {

    @Inject
    private AccountDao accountDao;

    @GET
    @Path("/authorize")
    public Response authorize(@Context HttpServletRequest request)
            throws URISyntaxException, OAuthSystemException {

        Subject subject = SecurityUtils.getSubject();

        // Save request and go to login page
        if(!subject.isAuthenticated()){
            WebUtils.saveRequest(request);
            URI uri = UriBuilder.fromUri("/login").build();
            return Response.seeOther( uri ).build();
        }

        OAuthAuthzRequest oauthRequest;

        OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthAuthzRequest(request);

            // build response according to response_type
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);

            String authCode = oauthIssuerImpl.authorizationCode();

            if (responseType.equals(ResponseType.CODE.toString())) {
                builder.setCode(authCode);
            }else{
               throw new IllegalArgumentException("responseType not allowed = " + responseType);
            }

            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

            final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
            URI url = new URI(response.getLocationUri());

            // Store autentication code in Token cache to validade in next phase (method: tokenPost)
            DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
            Cache<Object, Object> cache = securityManager.getCacheManager().getCache(AuthenticationFilter.TOKEN_CACHE);

            AccountPrincipal principal = (AccountPrincipal) subject.getPrincipal();
            cache.put(authCode, principal.getUserAccountID());

            return Response.status(response.getResponseStatus()).location(url).build();

        } catch (OAuthProblemException e) {

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(responseBuilder.entity("OAuth callback url needs to be provided by client!!!").build());
            }

            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
                    .location(redirectUri).buildQueryMessage();

            final URI location = new URI(response.getLocationUri());

            return responseBuilder.location(location).build();
        }
    }


    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenPost(@Context HttpServletRequest request,
                              MultivaluedMap<String, String> formParams) throws OAuthSystemException {

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        Long userAccountID;

        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(new ParameterizedHttpRequest(request, formParams));

            DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
            Cache<Object, Object> cache = securityManager.getCacheManager().getCache(AuthenticationFilter.TOKEN_CACHE);

            String clientID = oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID);

            // do checking for different grant types
            if (GrantType.AUTHORIZATION_CODE.toString().equals(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {

                String codeParam = oauthRequest.getParam(OAuth.OAUTH_CODE);
                userAccountID = (Long) cache.get(codeParam);

                if (userAccountID == null) {
                    OAuthResponse response =
                            OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                    .setError(OAuthError.TokenResponse.INVALID_GRANT)
                                    .setErrorDescription("invalid authorization code").buildJSONMessage();

                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }else{
                    cache.remove(codeParam); // not required anymore
                }
            }
//            else if (
//                    oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString()))
//            {
//                if (!Common.PASSWORD.equals(oauthRequest.getPassword())
//                        ||!Common.USERNAME.equals(oauthRequest.getUsername()))
//                {
//                    OAuthResponse response =
//                            OAuthASResponse.errorResponse(
//                                    HttpServletResponse.SC_BAD_REQUEST).setError(
//                                    OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription(
//                                    "invalid username or password").buildJSONMessage();
//
//                    return Response.status(response.getResponseStatus()).entity(
//                            response.getBody()).build();
//                }
            else if (GrantType.REFRESH_TOKEN.toString().equals(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {

                String key = oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN);

                UserAccount account = accountDao.getUserAccountByApiKey(key);

                if(account == null){
                    OAuthResponse response =
                            OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                    .setError(OAuthError.TokenResponse.INVALID_GRANT)
                                    .setErrorDescription("Invalid REFRESH_TOKEN").buildJSONMessage();

                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }else{
                    userAccountID = account.getId();
                }
            } else{
                throw OAuthProblemException.error("Invalid Rrequest");
            }

            String accessToken = oauthIssuerImpl.accessToken();

            // This token will be handled by AuthenticationFilter
            UserAccount userAccount = accountDao.getUserAccountByID(userAccountID);
            ApiKey apiKeyUser = userAccount.getKeys().iterator().next();
            cache.put(accessToken, apiKeyUser.getKey());

            OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .setRefreshToken(apiKeyUser.getKey())
                    .setExpiresIn("3600").buildJSONMessage();

            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }  catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse
                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .error(e).buildJSONMessage();

            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    public static class ParameterizedHttpRequest extends HttpServletRequestWrapper
    {

        public ParameterizedHttpRequest(HttpServletRequest request,
                                        MultivaluedMap<String, String> parameters)
        {
            super(request);
            this.parameters = parameters;
        }

        @Override
        public String getParameter(String name)
        {
            return parameters.getFirst(name);
        }

        @Override
        public Map getParameterMap()
        {
            return parameters;
        }

        @Override
        public Enumeration getParameterNames() {
            Enumeration<String> x = new Vector(parameters.keySet()).elements();
            return x;
        }

        @Override
        public String[] getParameterValues(String name)
        {
            String[] result = null;
            List<String> values = parameters.get(name);

            if (values != null)
            {
                result = values.toArray(new String[values.size()]);
            }

            return result;
        }

        /** Field description */
        private MultivaluedMap<String, String> parameters;
    }
}
