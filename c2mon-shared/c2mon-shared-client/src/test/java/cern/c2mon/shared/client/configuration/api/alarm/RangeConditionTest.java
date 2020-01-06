package cern.c2mon.shared.client.configuration.api.alarm;

import org.junit.Assert;
import org.junit.Test;

import cern.c2mon.shared.client.alarm.condition.RangeAlarmCondition;

public class RangeConditionTest {

  private static final String expectedXmlString = "<AlarmCondition class=\"cern.c2mon.server.common.alarm.RangeAlarmCondition\">"
      + "\n  <min-value type=\"java.lang.Float\">0.0</min-value>"
      + "\n  <max-value type=\"java.lang.Float\">100.0</max-value>"
      + "\n  <out-of-range-alarm type=\"java.lang.Boolean\">false</out-of-range-alarm>"
      + "\n</AlarmCondition>";

  @Test
  public void testClass() {
    RangeAlarmCondition<Float> intRangeCondition = new RangeAlarmCondition<>(0f, 100f);
    String xmlString = intRangeCondition.getXMLCondition();
    Assert.assertEquals(expectedXmlString, xmlString);
  }
}
