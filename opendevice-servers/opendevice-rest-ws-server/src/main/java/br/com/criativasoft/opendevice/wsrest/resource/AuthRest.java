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

import br.com.criativasoft.opendevice.core.ODev;
import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.core.model.OpenDeviceConfig;
import br.com.criativasoft.opendevice.core.util.DefaultPasswordGenerator;
import br.com.criativasoft.opendevice.restapi.auth.AccountAuth;
import br.com.criativasoft.opendevice.restapi.auth.AccountPrincipal;
import br.com.criativasoft.opendevice.restapi.auth.AesRuntimeCipher;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import br.com.criativasoft.opendevice.restapi.model.Account;
import br.com.criativasoft.opendevice.restapi.model.AccountType;
import br.com.criativasoft.opendevice.restapi.model.User;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.core.util.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;
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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static br.com.criativasoft.opendevice.restapi.auth.BearerTokenRealm.TOKEN_CACHE;


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
@Produces(MediaType.APPLICATION_JSON)
public class AuthRest {

    private static final Logger LOG = LoggerFactory.getLogger(AuthRest.class);
    public static final String SESSION_ID = "JSESSIONID";

    @Inject
    private AccountDao accountDao;

    @Inject
    private UserDao userDao;

    @Inject
    private ObjectMapper mapper;

    @Inject
    private AesRuntimeCipher encryptionCipher;

    @PersistenceContext
    private EntityManager em;

    @GET
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

        if(currentUser.isAuthenticated()) return noCache(Response.status(Status.OK).entity("{\"messages\":[\"Already logged\"]}"));

        Response response = doLogin(currentUser, username, password, false);

        if(currentUser.isAuthenticated()){

            AccountPrincipal principal = (AccountPrincipal) currentUser.getPrincipal();

            // Generate Cookie to indentify user on Shiro (see NewShiroInterceptor)
            Session session = currentUser.getSession(true); // this will force session creation
            javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie(AuthRest.SESSION_ID,  (String) session.getId());
            cookie.setPath("/");
            res.getResponse().addCookie(cookie);
            session.setTimeout((1000 * 60) * 30); // min

//            // Generate Cookie to indentify ApiKey/AuthToken
//            cookie = new javax.servlet.http.Cookie(TenantProvider.HTTP_HEADER_KEY, principal.getAccountUUID()); // (String) session.getId()
//            cookie.setPath("/");
//            res.getResponse().addCookie(cookie);

        }

        return response;

    }

    private Response doLogin(Subject currentUser, String username, String password, boolean isApiKey){

        LOG.debug("Using ApiKey ("+isApiKey+"), username : " + username);

        Account account = null;
        String authtoken = null;
        boolean logged = false;

        // Login using: ApiKey
        if(isApiKey){

            account = accountDao.getAccountByApiKey(username);

            // Generate and cache the 'AuthToken', this will be used in AuthenticationFilter
            // This token will be used in BearerTokenRealm
            // TODO: Need configure expire using EhCache
            if(account != null){

                // NOTE(RR): To simplify the development of clients, AuthToken and API Key will be the AccountUUID.
                // This can be changed in the future (issues #57)
                // authtoken = UUID.randomUUID().toString();
                authtoken = account.getUuid();

                DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
                Cache<Object, Object> cache = securityManager.getCacheManager().getCache(TOKEN_CACHE);
                cache.put(authtoken, username); // username (is Api_Key in this case)
                logged = true;
            }

        // login using: Form
        }else if (!currentUser.isAuthenticated()){

            try {

                User user = userDao.getUser(username);

                if(user == null) throw new AuthenticationException("Incorrect username");

                // ckeck plain version (loaded from database)
                boolean passwordsMatch = password.equals(user.getPassword());

                // Check encryption version
                if(!passwordsMatch){
                    HashingPasswordService service = new DefaultPasswordService();
                    passwordsMatch = service.passwordsMatch(password, user.getPassword());
                }

                if (!passwordsMatch)  throw new AuthenticationException("Incorrect password");

                Set<UserAccount> uaccounts = user.getAccounts();

                if(uaccounts.isEmpty()) throw new AuthenticationException("No accounts for user");

                if(uaccounts.size() > 1){
                    // TODO: Need return list and redirect to annother page...
                    return ErrorResponse.status(Status.FORBIDDEN,"Multiple Accounts not supported for now !! (open ticket !)");
                }

                AccountAuth token = new AccountAuth(uaccounts.iterator().next().getId(), user.getId());
                //token.setRememberMe(false); // to be remembered across sessions

                currentUser.login(token);

                // currentUser.getSession(true).setTimeout(xxxxx);

                if(currentUser.isAuthenticated()){
                    AccountPrincipal principal = (AccountPrincipal) currentUser.getPrincipal();
                    logged = true;
                    authtoken = principal.getAccountUUID();
                    user.setLastLogin(new Date());
                }

            } catch (UnknownAccountException e) {
                return ErrorResponse.UNAUTHORIZED("Unknown Account");
            } catch (IncorrectCredentialsException e) {
                return ErrorResponse.status(Status.FORBIDDEN,"Incorrect Credentials");
            } catch (AuthenticationException e) {
                return ErrorResponse.UNAUTHORIZED(e.getMessage());
            }
        }

        if (logged) {
            return noCache(Response.status(Status.OK).entity("{\"token\":\""+authtoken+"\"}"));
        } else {
            return ErrorResponse.UNAUTHORIZED("Authentication Fail");
        }

    }

    /**
     * Avoid cache login request
     */
    private Response noCache(Response.ResponseBuilder resp) {
        return ErrorResponse.noCache(resp);
    }


    @GET
    @Path("logout")
    public Response logout(@Context AtmosphereResource res, @Auth Subject currentUser) {

        if (currentUser.isAuthenticated()) {
            currentUser.logout();
            return noCache(Response.status(Status.OK).entity("{\"messages\":[\"Logout OK\"]}"));
        } else {
            return noCache(Response.status(Status.OK).entity("{\"messages\":[\"Not Logged\"]}"));
        }

    }

    @GET
    @Path("ping")
    public Response ping(@Auth Subject currentUser) {
           return noCache(Response.status(Status.OK));
    }

//    @GET
//    @Path("googleoauth")
//    public Response googleoauth(String data) {
//        System.out.println("teste >>> " + data);
//        return noCache(Response.status(Status.OK));
//    }

    @POST
    @Path("loginGoogle")
    public Response loginGoogle(@Auth Subject currentUser,
                                @FormParam("idtoken") String idtoken, @FormParam("invitation")  String invitation) {

        System.err.println("invitation >>> " + invitation);

        try {
            String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=";
            CloseableHttpClient client = HttpClientBuilder.create().build();
            CloseableHttpResponse response = client.execute(new HttpGet(url+idtoken));
            String bodyAsString = EntityUtils.toString(response.getEntity());

            if(response.getStatusLine().getStatusCode() == 200){

                System.out.println("google resp : " + bodyAsString);

                String appID = ODev.getConfig().getString(OpenDeviceConfig.ConfigKey.google_appid);

                if(appID == null) return ErrorResponse.status(Status.INTERNAL_SERVER_ERROR, "Google AppID not configured !");

                JsonNode json = mapper.readTree(bodyAsString);

                String aud = json.get("aud").asText();

                if(!appID.equals(aud)) return noCache(Response.status(Status.UNAUTHORIZED));


                // Add new User (to Account) and Login
                if(invitation != null){

                    invitation = encryptionCipher.decript(invitation);

                    String accountID = invitation.split(":")[0];

                    long time  = Long.parseLong(invitation.split(":")[1]);

                    Account account = accountDao.getAccountByUID(accountID);

                    if(account == null) return ErrorResponse.status(Status.NOT_FOUND, "Account Not Found !");

                    User user = userDao.getUser(json.get("email").asText());

                    if(user == null){
                        String password = new DefaultPasswordGenerator().generatePassword();
                        user = userDao.createUser(account, json.get("email").asText(), password);
                    }

                    em.flush(); // Force save in database

                    return doLogin(currentUser, user.getUsername(), user.getPassword(), false);

                // Login or Create Account (using Google)
                }else{

                    User user = userDao.getUser(json.get("email").asText());

                    // Create Account
                    if(user == null){

                        Account account = new Account();
                        accountDao.persist(account);

                        String password = new DefaultPasswordGenerator().generatePassword(); // raw password
                        user = userDao.createUser(account, json.get("email").asText(), password);

                        account.getUserAccounts().iterator().next().setType(AccountType.ACCOUNT_MANAGER);

                        em.flush(); // Force save in database

                        TenantProvider.getTenantProvider().addNewContext(account.getUuid());
                    }

                    return doLogin(currentUser, user.getUsername(), user.getPassword(), false);

                }


            }

            return noCache(Response.status(response.getStatusLine().getStatusCode()));

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return noCache(Response.status(Status.INTERNAL_SERVER_ERROR));
        }

    }


}