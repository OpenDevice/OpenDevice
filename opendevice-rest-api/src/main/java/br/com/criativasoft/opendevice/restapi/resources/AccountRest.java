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

package br.com.criativasoft.opendevice.restapi.resources;

import br.com.criativasoft.opendevice.core.TenantProvider;
import br.com.criativasoft.opendevice.restapi.auth.AccountPrincipal;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import br.com.criativasoft.opendevice.restapi.model.*;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.secnod.shiro.jaxrs.Auth;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

/**
 * TODO: Add docs.
 *
 * Note: This resource is used in RestServerConnection
 *
 * @author Ricardo JL Rufino
 * @date 09/10/16
 */
@Path("/api/accounts")
@RequiresAuthentication
public class AccountRest {

    @Inject
    private AccountDao dao;

    @Inject
    private UserDao userDao;

    @PersistenceContext
    private EntityManager em;

//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createKey(String appName) {
//
//    }

    @GET @Path("keys")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public List<ApiKey> listKeys(@Auth Subject subject) {

        AccountPrincipal principal = (AccountPrincipal) subject.getPrincipal();

        List<ApiKey> keys = dao.listKeys(principal.getUserAccountID());

        return keys;
    }

    @GET @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public List<User> listUsers() {

        String id = TenantProvider.getCurrentID();

        Account account = dao.getAccountByUID(id);

        return dao.listUsers(account);
    }

    @POST @Path("users")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public User addUser(User user, @Auth Subject subject) {

        AccountPrincipal principal = (AccountPrincipal) subject.getPrincipal();

        Account account = dao.getAccountByUID(principal.getAccountUUID());

        HashingPasswordService service = new DefaultPasswordService();
        user.setPassword(service.encryptPassword(user.getPassword()));

        // Editing
        if(user.getId() > 0){

            boolean contains = dao.existUser(account, user);

            if(!contains) throw new AuthorizationException("This user does not belong to your account");

            userDao.update(user);
        }else{

            UserAccount userAccount = new UserAccount();
            userAccount.setType(AccountType.USER);
            userAccount.setOwner(account);
            userAccount.setUser(user);
            user.getAccounts().add(userAccount);
            userDao.persist(user);
        }

        return user;

    };


    @DELETE @Path("users/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public Response deleteUser(@PathParam("id") long id, @Auth Subject subject) {

        AccountPrincipal principal = (AccountPrincipal) subject.getPrincipal();

        Account account = dao.getAccountByUID(principal.getAccountUUID());

        User user = userDao.getById(id);

        boolean exist = dao.existUser(account, user);

        if(!exist) throw new AuthorizationException("This user does not belong to your account");

        List<User> users = dao.listUsers(account);

        if(users.size() == 1) {
            return ErrorResponse.BAD_REQUEST("You can not remove all users");
        }

        Set<UserAccount> userAccounts = account.getUserAccounts();

        UserAccount userAccount = null;

        // Find Account
        for (UserAccount ua : userAccounts) {
            if(ua.getUser().getId() == user.getId()){
                userAccount = ua;
            }
        }

        userDao.delete(userAccount);
        userDao.delete(user);

        return Response.ok().build();


    }
}
