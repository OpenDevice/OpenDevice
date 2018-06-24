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

package nounit;

import br.com.criativasoft.opendevice.core.model.DeviceHistory;
import br.com.criativasoft.opendevice.middleware.persistence.LocalEntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TODO: Add Docs
 *
 * @author Ricardo JL Rufino on 06/05/15.
 */
public class TesteSelects {

    public static void main(String[] args) throws ParseException {


        EntityManager em = LocalEntityManagerFactory.getInstance().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();


        TypedQuery<DeviceHistory> query = em.createQuery("from DeviceHistory where deviceID = 1852620", DeviceHistory.class); // 1852619 (temp), 1852620
        List<DeviceHistory> histories = query.getResultList();

        try {

            PrintStream out = new PrintStream(new FileOutputStream("/media/ricardo/Dados/Outros/Eletronica/Projetos/EsperimentoTemperatura/data1.csv"));
            int index = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            for (DeviceHistory history : histories) {
                out.println(history.getTimestamp() + "," + history.getValue());
            }

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("[DONE]");
        tx.commit();
        em.close();

    }
}