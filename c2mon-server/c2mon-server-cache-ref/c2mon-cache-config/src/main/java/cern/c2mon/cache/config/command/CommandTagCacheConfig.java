package cern.c2mon.cache.config.command;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.config.CacheName;
import cern.c2mon.cache.config.config.AbstractSimpleCacheConfig;
import cern.c2mon.server.cache.loader.CacheLoaderDAO;
import cern.c2mon.shared.common.command.CommandTag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Configuration
public class CommandTagCacheConfig extends AbstractSimpleCacheConfig<CommandTag> {

  @Inject
  public CommandTagCacheConfig(AbstractCacheFactory cachingFactory, CacheLoaderDAO<CommandTag> cacheLoaderDAORef) {
    super(cachingFactory, CacheName.COMMANDTAG, CommandTag.class, cacheLoaderDAORef);
  }

  @Bean(name = CacheName.Names.COMMANDTAG)
  @Override
  public C2monCache<CommandTag> createCache() {
    return super.createCache();
  }
}
