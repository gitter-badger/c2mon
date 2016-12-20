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
package cern.c2mon.server.elasticsearch.indexer;

import java.sql.Timestamp;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.structure.mappings.EsAlarmMapping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.elasticsearch.structure.converter.EsAlarmLogConverter;
import cern.c2mon.server.elasticsearch.structure.types.EsAlarm;
import cern.c2mon.server.test.CacheObjectCreation;
import org.springframework.core.env.Environment;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the EsAlarmIndexer methods.
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class EsAlarmIndexerTest {
  private Alarm alarm;
  private EsAlarm esAlarm;
  private EsAlarmMapping esAlarmMapping;
  private Timestamp timestamp;

  @InjectMocks
  private EsAlarmIndexer<EsAlarm> indexer;

  @Mock
  private TransportConnector connector;

  private EsAlarmLogConverter esAlarmLogConverter = new EsAlarmLogConverter();

  private ElasticsearchProperties properties = new ElasticsearchProperties();

  @Before
  public void setup() throws IDBPersistenceException {
    alarm = CacheObjectCreation.createTestAlarm1();
    esAlarm = esAlarmLogConverter.convert(alarm);
    timestamp = alarm.getTimestamp();
    when(connector.logAlarmEvent(anyString(), anyString(), eq(esAlarm))).thenReturn(true);
    properties.setIndexType("M");
    esAlarmMapping = new EsAlarmMapping();
    indexer.setProperties(properties);
  }

  @Test
  public void testInitWell() throws IDBPersistenceException {
    when(connector.isConnected()).thenReturn(true);
    indexer.init();
    assertTrue(indexer.isAvailable());
  }

  @Test
  public void testLogAlarm() throws IDBPersistenceException {
    String expectedMapping = esAlarmMapping.getMapping();

    indexer.storeData(esAlarm);
    verify(connector).logAlarmEvent(eq(properties.getIndexPrefix() + "-alarm_"
        + indexer.millisecondsToYearMonth(timestamp.getTime())), eq(expectedMapping), eq(esAlarm));
  }

}