package cern.c2mon.cache.loading.common;

/**
 * Interface that must be implemented by all C2MON
 * cache loading mechanisms.
 *
 * @author Szymon Halastra
 */
public interface C2monCacheLoader {

  /**
   * At server start-up, loads the cache from the DB into memory.
   * In distributed set-up, this is not performed once the
   * cache has already been loaded once (only performed if the disk
   * store is cleaned).
   */
  void preload();
}
