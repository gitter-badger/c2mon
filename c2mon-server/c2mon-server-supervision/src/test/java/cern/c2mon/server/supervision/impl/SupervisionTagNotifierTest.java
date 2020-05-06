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
package cern.c2mon.server.supervision.impl;

import cern.c2mon.cache.actions.CacheActionsModuleRef;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.SupervisionAppender;
import cern.c2mon.cache.config.CacheConfigModuleRef;
import cern.c2mon.cache.config.collections.TagCacheCollection;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loading.config.CacheLoadingModuleRef;
import cern.c2mon.server.common.config.CommonModule;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.supervision.config.SupervisionModule;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.supervision.SupervisionEventImpl;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionEntity;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;

import static cern.c2mon.server.common.util.Java9Collections.setOf;
import static cern.c2mon.server.common.util.KotlinAPIs.apply;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Unit test of SupervisionTagNotifier class.
 *
 * @author Mark Brightwell
 */
@ActiveProfiles("cache-spies")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheConfigModuleRef.class,
  CacheDbAccessModule.class,
  SupervisionModule.class,
  CacheActionsModuleRef.class,
  CacheLoadingModuleRef.class,
  C2monIgniteConfiguration.class
})
public class SupervisionTagNotifierTest {

  @Inject private SupervisionTagNotifier supervisionTagNotifier;

  @Inject private SupervisionAppender supervisionAppender;
  @Inject private TagCacheCollection tagLocationService;

  @Inject private C2monCache<Process> processCache;
  @Inject private C2monCache<Equipment> equipmentCache;
  @Inject private C2monCache<SubEquipment> subEquipmentCache;
  @Inject private C2monCache<DataTag> dataTagCache;
  @Inject private C2monCache<RuleTag> ruleTagCache;

  private ProcessCacheObject process;
  private EquipmentCacheObject equipment;
  private SubEquipmentCacheObject subEquipment;
  private DataTagCacheObject dataTag;
  private DataTagCacheObject dataTag2;
  private DataTagCacheObject dataTag3;
  private DataTagCacheObject dataTag4;
  private RuleTagCacheObject ruleTag;
  private RuleTagCacheObject ruleTag2;
  private RuleTagCacheObject ruleTag3;
  private RuleTagCacheObject ruleTag4;
  private RuleTagCacheObject ruleTag5;

  @Before
  public void setUp() {
    equipment = (EquipmentCacheObject) putAndGet(
      equipmentCache,
      apply(new EquipmentCacheObject(30L), e -> {})
    );

    subEquipment = (SubEquipmentCacheObject) putAndGet(
      subEquipmentCache,
      apply(new SubEquipmentCacheObject(50L), se -> {
        se.setParentId(equipment.getId());
      })
    );

    process = (ProcessCacheObject) putAndGet(
      processCache,
      apply(new ProcessCacheObject(10L), p -> {
        p.setStateTagId(100L); // FIXME?
        p.setEquipmentIds(new ArrayList<>(singletonList(equipment.getId())));
      })
    );

    ruleTag = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(200L), r -> {
        r.setEquipmentIds(setOf(equipment.getId()));
        r.setProcessIds(setOf(process.getId()));
      })
    );

    ruleTag2 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(201L), r -> {
        r.setEquipmentIds(setOf(equipment.getId()));
        r.setProcessIds(setOf(process.getId()));
      })
    );

    ruleTag3 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(202L), r -> {
        r.setEquipmentIds(setOf(equipment.getId()));
        r.setProcessIds(setOf(process.getId()));
      })
    );

    ruleTag4 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(203L), r -> {
        r.setSubEquipmentIds(setOf(subEquipment.getId()));
        r.setEquipmentIds(setOf(equipment.getId()));
        r.setProcessIds(setOf(process.getId()));
      })
    );

    ruleTag5 = (RuleTagCacheObject) putAndGet(
      ruleTagCache,
      apply(new RuleTagCacheObject(204L), r -> {
        r.setSubEquipmentIds(setOf(subEquipment.getId()));
        r.setEquipmentIds(setOf(equipment.getId()));
        r.setProcessIds(setOf(process.getId()));
      })
    );

    dataTag = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(100L), t -> {
        t.setRuleIds(asList(ruleTag.getId(), ruleTag2.getId()));
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
      })
    );

    dataTag2 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(101L), t -> {
        t.setRuleIds(asList(ruleTag.getId(), ruleTag3.getId()));
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
      })
    );

    dataTag3 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(102L), t -> {
        t.setRuleIds(asList(ruleTag4.getId(), ruleTag5.getId()));
        t.setSubEquipmentId(subEquipment.getId());
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
      })
    );

    dataTag4 = (DataTagCacheObject) putAndGet(
      dataTagCache,
      apply(new DataTagCacheObject(103L), t -> {
        t.setRuleIds(asList(ruleTag4.getId(), ruleTag5.getId()));
        t.setSubEquipmentId(subEquipment.getId());
        t.setEquipmentId(equipment.getId());
        t.setProcessId(process.getId());
      })
    );
  }

  @Test
  public void testInitialization() {
    supervisionTagNotifier.init();
  }

  @Test
  @DirtiesContext
  public void testNotifySupervisionEventForProcessEvent() {
    SupervisionEvent event = new SupervisionEventImpl(
      SupervisionEntity.PROCESS,
      process.getId(),
      "P_TEST",
      SupervisionStatus.DOWN,
      new Timestamp(System.currentTimeMillis()),
      "test message"
    );

    // expect(processCache.get(10L)).andReturn(process);
    // expect(equipmentFacade.getProcessForAbstractEquipment(30L)).andReturn(process);
    // expect(equipmentFacade.getDataTagIds(30L)).andReturn(asList(100L, 101L));
    // expect(tagLocationService.get(100L)).andReturn(dataTag);
    // expect(tagLocationService.get(101L)).andReturn(dataTag2);
    // expect(tagLocationService.get(200L)).andReturn(ruleTag).times(2);
    // expect(tagLocationService.get(201L)).andReturn(ruleTag2);
    // expect(tagLocationService.get(202L)).andReturn(ruleTag3);

    supervisionAppender.addSupervisionQuality(dataTag, event);
    dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, dataTag);
    supervisionAppender.addSupervisionQuality(dataTag2, event);
    dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, dataTag2);
    supervisionAppender.addSupervisionQuality(ruleTag, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag); //only once although uses triggered by 2 different tags
    supervisionAppender.addSupervisionQuality(ruleTag2, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag2);
    supervisionAppender.addSupervisionQuality(ruleTag3, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag3);

    // mockControl.replay();

    supervisionTagNotifier.notifySupervisionEvent(event);

    // verify(processCache).get(process.getId());

    // mockControl.verify();
  }

  @Test
  @DirtiesContext
  public void testNotifySupervisionEventForEquipmentEvent() {
    SupervisionEvent event = new SupervisionEventImpl(
      SupervisionEntity.EQUIPMENT,
      equipment.getId(),
      "E_TEST",
      SupervisionStatus.RUNNING,
      new Timestamp(System.currentTimeMillis()),
      "test message"
    );

    // mockControl.reset();

    // expect(equipmentFacade.getProcessForAbstractEquipment(30L)).andReturn(process);
    // expect(equipmentFacade.getDataTagIds(30L)).andReturn(asList(100L, 101L));
    // expect(tagLocationService.get(100L)).andReturn(dataTag);
    // expect(tagLocationService.get(101L)).andReturn(dataTag2);
    // expect(tagLocationService.get(200L)).andReturn(ruleTag).times(2);
    // expect(tagLocationService.get(201L)).andReturn(ruleTag2);
    // expect(tagLocationService.get(202L)).andReturn(ruleTag3);

    supervisionAppender.addSupervisionQuality(dataTag, event);
    dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, dataTag);
    supervisionAppender.addSupervisionQuality(dataTag2, event);
    dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, dataTag2);
    supervisionAppender.addSupervisionQuality(ruleTag, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag); //only once although uses triggered by 2 different tags
    supervisionAppender.addSupervisionQuality(ruleTag2, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag2);
    supervisionAppender.addSupervisionQuality(ruleTag3, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag3);

    // mockControl.replay();

    supervisionTagNotifier.notifySupervisionEvent(event);

    // mockControl.verify();
  }

  @Test
  @DirtiesContext
  public void testNotifySubEquipmentEvent() {
    SupervisionEvent event = new SupervisionEventImpl(SupervisionEntity.SUBEQUIPMENT, 50L, "E_SUBTEST", SupervisionStatus.DOWN, new Timestamp(System.currentTimeMillis()),
      "test message");

    // mockControl.reset();

    // expect(subEquipmentFacade.getDataTagIds(50L)).andReturn(asList(102L, 103L));
    // expect(tagLocationService.get(102L)).andReturn(dataTag3);
    // expect(tagLocationService.get(103L)).andReturn(dataTag4);
    // expect(tagLocationService.get(203L)).andReturn(ruleTag4).times(2);
    // expect(tagLocationService.get(204L)).andReturn(ruleTag5).times(2);

    supervisionAppender.addSupervisionQuality(dataTag3, event);
    dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, dataTag3);
    supervisionAppender.addSupervisionQuality(dataTag4, event);
    dataTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, dataTag4);
    supervisionAppender.addSupervisionQuality(ruleTag4, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag4);
    supervisionAppender.addSupervisionQuality(ruleTag5, event);
    ruleTagCache.getCacheListenerManager().notifyListenersOf(CacheEvent.SUPERVISION_CHANGE, ruleTag5);

    // mockControl.replay();

    supervisionTagNotifier.notifySupervisionEvent(event);

    // mockControl.verify();
  }

  private static <C extends C2monCache<T>, T extends Cacheable> T putAndGet(C cache, T thing) {
    cache.put(thing.getId(), thing);
    return cache.get(thing.getId());
  }
}
