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

package br.com.criativasoft.opendevice.middleware.test;

import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * @author Ricardo JL Rufino on 01/05/15.
 */
public class UpdateDataNative {

    public static void main(String[] args) {

        String path = "/media/Dados/Codigos/Java/Projetos/OpenDevice/Workspace/database";
        final GraphDatabaseService graphDb;
        graphDb =  new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path)
                .setConfig(GraphDatabaseSettings.allow_store_upgrade, "false")
                .newGraphDatabase();
        final Transaction tx = graphDb.beginTx();

        ExecutionEngine engine = new ExecutionEngine(graphDb, NullLogProvider.getInstance());

//        ExecutionResult result = engine.execute("MATCH (n:DeviceHistory) DELETE n");
//        System.out.println("ExecutionResult:" + result);

//        ExecutionResult result = engine.execute("MATCH (n:DeviceCategory) DELETE n");
//        System.out.println("ExecutionResult:" + result);

        try {
            ResourceIterable<Label> labels = GlobalGraphOperations.at(graphDb).getAllLabels();

            System.out.println("Labels : \n =========================");
            for (Label label : labels) {
                System.out.println(label);
            }

            // update
            System.out.println("Nodes : \n =========================");
            ResourceIterable<Node> nodesWithLabel = GlobalGraphOperations.at(graphDb).getAllNodesWithLabel(DynamicLabel.label("Dashboard"));
            int i=1;
            for (Node node : nodesWithLabel) {
                System.out.println(" - " + node);
                node.setProperty("title", "Dashboard "+ (i++));
            }

            // remove
//            System.out.println("Nodes : \n =========================");
//            ResourceIterable<Node> nodesWithLabel = GlobalGraphOperations.at(graphDb).getAllNodesWithLabel(DynamicLabel.label("Sensor"));
//            for (Node node : nodesWithLabel) {
//                System.out.println(" - " + node);
//                Iterable<Relationship> relationships = node.getRelationships();
//                for (Relationship relationship : relationships) {
//                    relationship.delete();
//                }
//                node.delete();
//            }


            tx.success();
        } finally {
            tx.finish();
            graphDb.shutdown();
        }
    }
}
