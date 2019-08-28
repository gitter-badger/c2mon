package cern.c2mon.server.cache.rule;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.server.cache.AbstractBatchCacheConfig;
import cern.c2mon.server.cache.CacheName;
import cern.c2mon.server.cache.loader.BatchCacheLoaderDAO;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.rule.RuleTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class RuleTagCacheConfig extends AbstractBatchCacheConfig<RuleTag> {

  @Autowired
  public RuleTagCacheConfig(AbstractCacheFactory cachingFactory, ThreadPoolTaskExecutor cacheLoaderTaskExecutor, CacheLoaderProperties properties, BatchCacheLoaderDAO<RuleTag> batchCacheLoaderDAORef) {
    super(cachingFactory, CacheName.RULETAG, RuleTag.class, cacheLoaderTaskExecutor, properties, batchCacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.RULETAG)
  @Override
  public C2monCache<RuleTag> createCache() {
    return super.createCache();
  }
}
