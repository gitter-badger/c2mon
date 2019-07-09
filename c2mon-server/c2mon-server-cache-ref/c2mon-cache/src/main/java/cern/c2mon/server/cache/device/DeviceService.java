package cern.c2mon.server.cache.device;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.device.Device;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class DeviceService {

  private C2monCache<Device> deviceCacheRef;

//  @Autowired
//  public DeviceService(final C2monCache<Long, Device> deviceCacheRef) {
//    this.deviceCacheRef = deviceCacheRef;
//  }
}
