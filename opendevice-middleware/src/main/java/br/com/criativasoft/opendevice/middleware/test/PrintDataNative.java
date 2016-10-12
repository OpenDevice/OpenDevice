/*
 *
 *  * ******************************************************************************
 *  *  Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
 *  *  All rights reserved. This program and the accompanying materials
 *  *  are made available under the terms of the Eclipse Public License v1.0
 *  *  which accompanies this distribution, and is available at
 *  *  http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  *  Contributors:
 *  *  Ricardo JL Rufino - Initial API and Implementation
 *  * *****************************************************************************
 *
 */

package br.com.criativasoft.opendevice.middleware.test;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;

/**
 * @author Ricardo JL Rufino on 01/05/15.
 */
public class PrintDataNative {

    public static void main(String[] args) {

        String path = "/media/Dados/Codigos/Java/Projetos/OpenDevice/Workspace/database";
        final GraphDatabaseService graphDb;
        graphDb =  new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(path))
                .setConfig(GraphDatabaseSettings.allow_store_upgrade, "false")
                .newGraphDatabase();
        final Transaction tx = graphDb.beginTx();


        try {
            ResourceIterable<Label> labels = graphDb.getAllLabels();

//            Result result = graphDb.execute("match (l {name: 'my node'}) return l, l.name" );

            System.out.println("Labels : \n =========================");
            for (Label label : labels) {
                System.out.println(label);
            }

            System.out.println("Nodes : \n =========================");
                for (final Node node : graphDb.getAllNodes()) {

                        System.out.print(node.getId() + ": ");
                        for (final String key : node.getPropertyKeys()) {
                            System.out.print(key + " - " + node.getProperty(key) + ", ");
                        }
                        System.out.print("\n");
                }
            tx.success();
        } finally {
            tx.close();
            graphDb.shutdown();
        }
    }
}
