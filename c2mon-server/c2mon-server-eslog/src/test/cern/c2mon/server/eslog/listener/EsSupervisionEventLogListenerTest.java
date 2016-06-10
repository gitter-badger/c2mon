/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.listener;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.eslog.structure.converter.EsSupervisionEventConverter;
import cern.c2mon.server.eslog.structure.types.EsSupervisionEvent;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test if the the SupervisionEvent are well translated into EsSupervisionEvent for ElasticSearch.
 *
 * @author Alban Marguet
 */
@RunWith(MockitoJUnitRunner.class)
public class EsSupervisionEventLogListenerTest {
  private SupervisionConstants.SupervisionEntity entity = SupervisionConstants.SupervisionEntity.PROCESS;
  private SupervisionConstants.SupervisionStatus status = SupervisionConstants.SupervisionStatus.RUNNING;
  private Timestamp timestamp = new Timestamp(123456789);
  private long id = 1L;
  private String message = "message";
  private SupervisionEvent event = new SupervisionEventImpl(entity, id, status, timestamp, message);
  private EsSupervisionEvent esSupervisionEvent;

  @Mock
  private SupervisionNotifier supervisionNotifier;

  @InjectMocks
  private EsSupervisionEventListener listener;

  @Mock
  private EsSupervisionEventConverter esSupervisionEventConverter;

  @Test
  public void testNotifySupervisionEvent() throws IDBPersistenceException {
    when(esSupervisionEventConverter.convert(eq(event))).thenReturn(esSupervisionEvent);
    listener.notifySupervisionEvent(event);
  }

}