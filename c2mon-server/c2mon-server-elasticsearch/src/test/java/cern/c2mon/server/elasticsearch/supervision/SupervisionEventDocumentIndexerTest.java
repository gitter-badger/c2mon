/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.elasticsearch.supervision;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.IndexNameManager;
import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.util.EntityUtils;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Alban Marguet
 * @author Justin LEwis Salmon
 */
public class SupervisionEventDocumentIndexerTest extends BaseElasticsearchIntegrationTest {

  @Autowired
  private IndexNameManager indexNameManager;

  @Autowired
  private SupervisionEventDocumentIndexer indexer;

  private SupervisionEventDocument document;

  @Before
  public void setUp() {
    SupervisionEvent event = EntityUtils.createSupervisionEvent();
    document = new SupervisionEventDocumentConverter().convert(event);
    indexName = indexNameManager.indexFor(document);
  }

  @Test
  public void logSingleSupervisionEventTest() throws IDBPersistenceException, IOException {
    indexer.storeData(document);

    getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.", doesIndexExist(indexName));

    List<String> indexData = getEmbeddedNode().fetchAllDocuments(indexName);
    Assert.assertEquals("Index should have one document inserted.", 1, indexData.size());
  }

  @Test
  public void logMultipleSupervisionEventsTest() throws IDBPersistenceException, IOException {
    indexer.storeData(document);
    indexer.storeData(document);

    getEmbeddedNode().refreshIndices();

    assertTrue("Index should have been created.", doesIndexExist(indexName));

    List<String> indexData = getEmbeddedNode().fetchAllDocuments(indexName);
    Assert.assertEquals("Index should have two documents inserted.", 2, indexData.size());
  }
}