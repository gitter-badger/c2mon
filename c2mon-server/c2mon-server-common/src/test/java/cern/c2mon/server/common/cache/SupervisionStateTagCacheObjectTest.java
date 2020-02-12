package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.supervision.SupervisionStateTag;

import java.sql.Timestamp;

public class SupervisionStateTagCacheObjectTest extends CacheableTest<SupervisionStateTag> {

  private static SupervisionStateTag sample = new SupervisionStateTag(0L, 1L, "EQ", null, null);

  public SupervisionStateTagCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(SupervisionStateTag cloneObject) {
    cloneObject.setSupervision(cloneObject.getSupervisionStatus(), "Going dowwwn!", new Timestamp(12));
  }
}