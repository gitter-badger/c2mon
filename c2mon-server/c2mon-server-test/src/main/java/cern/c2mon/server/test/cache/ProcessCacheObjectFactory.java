/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionStatus;

import java.sql.Timestamp;

public class ProcessCacheObjectFactory extends AbstractCacheObjectFactory<ProcessCacheObject> {

  @Override
  public ProcessCacheObject sampleBase() {
    ProcessCacheObject processCacheObject = new ProcessCacheObject(90L, "Test Process", 1200L, 100, 100);
    processCacheObject.setDescription("Test process description");
    processCacheObject.setAliveInterval(60);
    processCacheObject.setAliveTagId(510L); //FK ref
    processCacheObject.setStateTagId(510L);
    processCacheObject.setSupervision(SupervisionStatus.DOWN, "Status description", new Timestamp(System.currentTimeMillis()));
    processCacheObject.setStartupTime(new Timestamp(0));
    processCacheObject.setCurrentHost("test host");
    processCacheObject.setRequiresReboot(false);
    processCacheObject.setProcessPIK(12345L);
    processCacheObject.setLocalConfig(ProcessCacheObject.LocalConfig.Y);

    return processCacheObject;
  }
}
