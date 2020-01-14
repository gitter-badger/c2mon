package cern.c2mon.server.common.cache;

import cern.c2mon.server.common.alive.AliveTag;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AliveTimerCacheObjectTest extends CacheableTest<AliveTag> {

  private static AliveTag sample = new AliveTag(1L);

  public AliveTimerCacheObjectTest() {
    super(sample);
  }

  @Override
  protected void mutateObject(AliveTag cloneObject) {
    cloneObject.setAliveInterval(420);
    cloneObject.setLastUpdate(1337);
  }

  /**
   * Set active should return whether the object changed
   *
   * This is mainly used in the AliveTimerService
   */
  @Test
  public void setActiveReturnsDifferent() {
    assertTrue(sample.setActive(true));
    assertTrue(sample.setActive(false));
    assertFalse(sample.setActive(false));
    assertTrue(sample.setActive(true));
    assertFalse(sample.setActive(true));
  }
}
