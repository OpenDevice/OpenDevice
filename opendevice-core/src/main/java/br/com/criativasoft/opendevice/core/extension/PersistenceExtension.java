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

package br.com.criativasoft.opendevice.core.extension;

import java.util.List;
import java.util.Map;

/**
 * Extension Point to add new JPA Entities
 *
 * @author Ricardo JL Rufino
 * @date 24/01/16
 */
public interface PersistenceExtension {

    List<Class> getEntityClasses();

    /**
     * Get Entity DAO
     *
     * @return Map<Interface, Implementation>
     */
    Map<Class, Class> getDaoClasses();

}
