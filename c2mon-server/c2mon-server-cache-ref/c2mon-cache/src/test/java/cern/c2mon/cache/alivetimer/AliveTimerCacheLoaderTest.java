package cern.c2mon.cache.alivetimer;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.AliveTimerMapper;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
public class AliveTimerCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private C2monCache<Long, AliveTimer> aliveTimerCacheRef;

  @Autowired
  private AliveTimerMapper aliveTimerMapper;

  @Before
  public void prepare() {
    aliveTimerCacheRef.init();
  }

  @Test
  public void preloadCache() {
    assertNotNull("AliveTimer cache should be not null", aliveTimerCacheRef);

    List<AliveTimer> aliveList = aliveTimerMapper.getAll(); //IN FACT: GIVES TIME FOR CACHE TO FINISH LOADING ASYNCH BEFORE COMPARISON BELOW...

    //test the cache is the same size as in DB
    assertEquals("Size of cache and DB mapping should be equal", aliveList.size(), aliveTimerCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<AliveTimer> it = aliveList.iterator();
    while (it.hasNext()) {
      AliveTimerCacheObject currentTimer = (AliveTimerCacheObject) it.next();
      //only compares one field so far
      assertEquals("Cached AliveTimer should have the same name as AliveTimer in DB", currentTimer.getRelatedName(), ((aliveTimerCacheRef.get(currentTimer.getId())).getRelatedName()));
    }
  }
}
