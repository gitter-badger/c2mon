package cern.c2mon.cache.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.configuration.CacheConfiguration;

import cern.c2mon.cache.api.AbstractFactory;
import cern.c2mon.cache.api.C2monCache;

/**
 * @author Szymon Halastra
 */

@Slf4j
public class IgniteFactory extends AbstractFactory {

  @Override
  public C2monCache createCache(String name, Class<?> keyType, Class<?> valueType) {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();

    cacheConfiguration.setName(name);
    cacheConfiguration.setIndexedTypes(keyType, valueType);

    log.info("And here it is, working IgniteFactory");

    return new IgniteCache(cacheConfiguration);
  }

  @Override
  public AbstractFactory getCachingFactory() {
    return this;
  }
}
