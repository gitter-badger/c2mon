package cern.c2mon.server.supervision;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.process.Process;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.spy;

@Profile("cache-spies")
@Configuration
public class CacheSpiesConfiguration {

  @Bean
  @Primary
  public C2monCache<Process> processCacheSpy(C2monCache<Process> processCache) {
    return spy(processCache);
  }
}
