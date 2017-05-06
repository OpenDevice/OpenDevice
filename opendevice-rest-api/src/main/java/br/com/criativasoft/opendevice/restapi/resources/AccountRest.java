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
import br.com.criativasoft.opendevice.restapi.auth.AesRuntimeCipher;
import br.com.criativasoft.opendevice.restapi.io.ErrorResponse;
import br.com.criativasoft.opendevice.restapi.model.*;
import br.com.criativasoft.opendevice.restapi.model.dao.AccountDao;
import br.com.criativasoft.opendevice.restapi.model.dao.UserDao;
import br.com.criativasoft.opendevice.restapi.model.vo.AccountVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.HashingPasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
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
@Produces(MediaType.APPLICATION_JSON)
@RequiresAuthentication
public class AccountRest {

    @Inject
    private AccountDao dao;

    @Inject
    private UserDao userDao;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private AesRuntimeCipher cipher;

//    @POST
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createKey(String appName) {
//
//    }

    @GET @Path("keys")
    public List<ApiKey> listKeys() {

        AccountPrincipal principal = (AccountPrincipal) getSubject().getPrincipal();

        List<ApiKey> keys = dao.listKeys(principal.getUserAccountID());

        return keys;
    }


    @GET
    @RequiresRoles(AccountType.ROLES.CLOUD_MANAGER)
    public List<AccountVO> list() {

        List<Account> list = dao.listAll();

        List<AccountVO> accounts = new LinkedList<AccountVO>();

        String id = TenantProvider.getCurrentID();

        for (Account account : list) {

            if(!account.getUuid().equals(id)){
                accounts.add(new AccountVO(account));
            }

        }

        return accounts;
    }


    @GET @Path("invitationLink")
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public Response invitationLink() {

        String secret = TenantProvider.getCurrentID()+":"+System.currentTimeMillis();

        String link = cipher.encript(secret);

        String decript = cipher.decript(link);

        System.out.println(decript);

        return Response.ok().entity("{\"invitation\" : \""+link+"\"}").build();
    }

    @DELETE @Path("{id}")
    @RequiresRoles(AccountType.ROLES.CLOUD_MANAGER)
    public Response delete(@PathParam("id") long id) {

        String uid = TenantProvider.getCurrentID();

        Account account = dao.getById(id);

        if(account == null) ErrorResponse.status(Response.Status.NOT_FOUND, "Account not found !");

        if(account != null && account.getUuid().equals(uid)) {
            return ErrorResponse.BAD_REQUEST("You can not remove current account !");
        }

        // Delete Users
        Set<UserAccount> userAccounts = account.getUserAccounts();
        for (UserAccount userAccount : userAccounts) {
            userDao.delete(userAccount.getUser());
        }

        dao.delete(account);


        System.out.println("delete....!!");
        // FIXME: delete all data from account!!
        // Dash, Rules, Jobs, Context, Caches, etc...


        return Response.ok().build();
    }


    @GET @Path("users")
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public List<User> listUsers() {

        String id = TenantProvider.getCurrentID();

        Account account = dao.getAccountByUID(id);

        return dao.listUsers(account);
    }

    @POST @Path("users")
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public User addUser(User user) {

        AccountPrincipal principal = (AccountPrincipal) getSubject().getPrincipal();

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
    }


    @DELETE @Path("users/{id}")
    @RequiresRoles(AccountType.ROLES.ACCOUNT_MANAGER)
    public Response deleteUser(@PathParam("id") long id) {

        AccountPrincipal principal = (AccountPrincipal) getSubject().getPrincipal();

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

        boolean deleteUser = (user.getAccounts().size() == 1);
        account.getUserAccounts().remove(userAccount);
        userDao.delete(userAccount);
        if(deleteUser)  userDao.delete(user);

        return Response.ok().build();

    }


    private Subject getSubject(){
        return SecurityUtils.getSubject();
    }
}
