package cern.c2mon.server.configuration.loader;

import cern.c2mon.cache.actions.device.DeviceService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.DeviceClassMapper;
import cern.c2mon.server.common.device.*;
import cern.c2mon.server.configuration.helper.ObjectEqualityComparison;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import org.junit.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DeviceClassConfigTest extends ConfigurationCacheLoaderTest<Device> {

  @Inject
  private DeviceClassMapper deviceClassMapper;

  @Inject
  private DeviceService deviceService;

  @Inject
  private C2monCache<DeviceClass> deviceClassCache;

  @Inject
  private C2monCache<Device> deviceCache;

  @Test
  public void createUpdateDeviceClass() throws ClassNotFoundException {
    ConfigurationReport report = configurationLoader.applyConfiguration(30);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));

    DeviceClassCacheObject expectedObject = (DeviceClassCacheObject) deviceClassMapper.getItem(10L);
    assertNotNull(expectedObject);

    DeviceClassCacheObject cacheObject = (DeviceClassCacheObject) deviceClassCache.get(10L);
    expectedObject = new DeviceClassCacheObject(10L, "TEST_DEVICE_CLASS_10", "Description of TEST_DEVICE_CLASS_10");

    List<Property> expectedProperties = new ArrayList<>();
    expectedProperties.add(new Property(10L, "cpuLoadInPercent", "The current CPU load in percent"));
    expectedProperties.add(new Property(11L, "responsiblePerson", "The person responsible for this device"));
    expectedProperties.add(new Property(12L, "someCalculations", "Some super awesome calculations"));

    List<Property> expectedFields = new ArrayList<>();
    expectedFields.add(new Property(10L, "field1", null));
    expectedFields.add(new Property(11L, "field2", null));

    expectedProperties.add(new Property(13L, "TEST_PROPERTY_WITH_FIELDS", "A property containing fields", expectedFields));

    List<Command> expectedCommands = new ArrayList<>();
    expectedCommands.add(new Command(10L, "TEST_COMMAND_1", "Description of TEST_COMMAND_1"));
    expectedCommands.add(new Command(11L, "TEST_COMMAND_2", "Description of TEST_COMMAND_2"));

    expectedObject.setProperties(expectedProperties);
    expectedObject.setCommands(expectedCommands);

    ObjectEqualityComparison.assertDeviceClassEquals(expectedObject, cacheObject);

    // Assert that the object from the DB is also the same
    DeviceClassCacheObject dbObject = (DeviceClassCacheObject) deviceClassMapper.getItem(10L);
    assertNotNull(dbObject);
    ObjectEqualityComparison.assertDeviceClassEquals(expectedObject, dbObject);
  }

  @Test
  public void updateDeviceClass() throws ClassNotFoundException {
    configurationLoader.applyConfiguration(30);
    ConfigurationReport report = configurationLoader.applyConfiguration(31);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    DeviceClassCacheObject cacheObject = (DeviceClassCacheObject) deviceClassCache.get(10L);

    List<Property> expectedProperties = new ArrayList<>();
    DeviceClassCacheObject expectedObject = (DeviceClassCacheObject) deviceClassMapper.getItem(10L);
    expectedProperties.add(new Property(14L, "numCores", "The number of CPU cores on this device"));

    expectedObject.setProperties(expectedProperties);
    ObjectEqualityComparison.assertDeviceClassEquals(expectedObject, cacheObject);
  }

  @Test
  public void removeDeviceClass() {
    ConfigurationReport report = configurationLoader.applyConfiguration(32);

    assertFalse(report.toXML().contains(ConfigConstants.Status.FAILURE.toString()));
    assertFalse(deviceClassCache.containsKey(400L));
    DeviceClass cacheObject = deviceClassMapper.getItem(400L);
    assertNull(cacheObject);
  }
}
