///*
// * *****************************************************************************
// * Copyright (c) 2013-2014 CriativaSoft (www.criativasoft.com.br)
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// *  Contributors:
// *  Ricardo JL Rufino - Initial API and Implementation
// * *****************************************************************************
// */
//
//package br.com.criativasoft.opendevice.middleware.test;
//
//import org.neo4j.graphdb.GraphDatabaseService;
//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Relationship;
//import org.neo4j.graphdb.Transaction;
//import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//
//import java.io.File;
//
///**
// * TODO: Add Docs
// *
// * @author Ricardo JL Rufino on 01/05/15.
// */
//public class ClearDatabase {
//
//    public static void main(String[] args) {
//
//            final GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File("/media/ricardo/Dados/Codidos/Java/Projetos/opendevice-project/data"));
//            final Transaction tx = graphDb.beginTx();
//
//
//            try {
//                for (final Node node : graphDb.getAllNodes()) {
//
//                    Iterable<Relationship> relationships = node.getRelationships();
//                    for (Relationship relationship : relationships) {
//                        relationship.delete();
//                    }
//                    node.delete();
//
//                    System.out.println(node.getId() + ": ");
//                }
//                tx.success();
//            } finally {
//                tx.close();
//                graphDb.shutdown();
//            }
//        }
//}
