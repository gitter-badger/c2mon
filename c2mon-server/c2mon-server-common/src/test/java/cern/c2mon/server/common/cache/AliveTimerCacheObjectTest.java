package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alive.AliveTimerCacheObject;

public class AliveTimerCacheObjectTest extends CacheableTest<AliveTimerCacheObject> {

  private static AliveTimerCacheObject sample = new AliveTimerCacheObject(1L);

  public AliveTimerCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(AliveTimerCacheObject cloneObject) {
      cloneObject.setAliveInterval(420);
      cloneObject.setLastUpdate(1337);
  }
}