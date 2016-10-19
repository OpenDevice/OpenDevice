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

package br.com.criativasoft.opendevice.restapi.model.dao;

import br.com.criativasoft.opendevice.core.dao.Dao;
import br.com.criativasoft.opendevice.restapi.model.User;
import br.com.criativasoft.opendevice.restapi.model.UserAccount;

/**
 * @author Ricardo JL Rufino
 * @date 24/09/16
 */
public interface UserDao extends Dao<User> {

    User getUser(String username);

    void delete(UserAccount userAccount);
}
