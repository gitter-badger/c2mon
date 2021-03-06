/*******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package cern.c2mon.daq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

import cern.c2mon.shared.common.config.CommonJmsProperties;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.daq.config.DaqJmsProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties("c2mon.daq")
public class DaqProperties {

  /**
   * Unique name of this DAQ process
   */
  private String name;

  /**
   * Time (in ms) which the DAQ waits for a server response
   */
  private long serverRequestTimeout = 120000;

  /**
   * Tolerance of the freshness monitor. A tag will be considered stale if it
   * is not updated within freshnessInterval * freshnessTolerance seconds. The
   * freshness interval is configured on the tag itself
   */
  private double freshnessTolerance = 1.5;

  /**
   * Path to a local configuration file. If set, the DAQ will load its
   * configuration from this file rather than retrieving it from the server.
   */
  private String localConfigFile = null;

  /**
   * Path on the local machine to which to save the remote configuration. This
   * can then subsequently be modified and used as a local configuration.
   */
  private String saveRemoteConfig = null;

  /**
   * JMS properties
   */
  private final Jms jms = new Jms();
  
  /**
   * Defines equipment specific properties.
   */
  private final EquipmentProperties equipment = new EquipmentProperties();

  @Data
  public static class Jms extends DaqJmsProperties {

    /**
     * Tag publication mode. Possible values are:
     *
     * single: publish to a single broker (default)
     * double: publish to two brokers (e.g for feeding a test server with
     *         operational data)
     * test:   do not publish at all
     */
    private String mode = "single";

    /**
     * URL of the secondary JMS broker to which to publish (only relevant when
     * running in double publication mode)
     */
    private String secondaryUrl = "tcp://0.0.0.0:61617";
    
    /**
     * Set the time-to-live in seconds for all requests that are sent via JMS to the C2MON server.
     * Default is 60 seconds
     */
    private int requestMsgtimeToLive = 60;
    
    /**
     * Maximum number of tag value objects to be packed into a single 
     * JMS message sent to the server.
     */
    private int maxMessageFrameSize = 1000;

    /**
     * Interval in milliseconds at which High-Priority messages are to be sent to the server, if
     * there are tag updates to be processed and {@link #maxMessageFrameSize} is not reached.
     * <p>
     * Default is 500 ms
     * 
     * @see DataTagAddress#PRIORITY_HIGH
     */
    private long maxMessageDelayPriorityHigh = 500L;
    
    /**
     * Interval in milliseconds at which messages are to be sent to the server, if
     * there are tag updates to be processed and {@link #maxMessageFrameSize} is not reached.
     * <p>
     * Default is 1000 ms
     * 
     * @see DataTagAddress#PRIORITY_MEDIUM
     */
    private long maxMessageDelayPriorityMedium = 1000L;
    
    /**
     * Interval in milliseconds at which messages are to be sent to the server, if
     * there are tag updates to be processed and {@link #maxMessageFrameSize} is not reached.
     * <p>
     * Default is 1000 ms
     * 
     * @see DataTagAddress#PRIORITY_LOW
     */
    private long maxMessageDelayPriorityLow = 1000L;
  }

  /**
   * Filtering properties
   */
  private final Filter filter = new Filter();

  @Data
  public static class Filter {

    /**
     * Maximum capacity of the filter buffer. If this capacity is exceeded, a
     * FIFO strategy will be applied to the buffer
     */
    private int bufferCapacity = 10000;

    /**
     * Dynamic deadband properties
     */
    private final DynamicDeadband dynamicDeadband = new DynamicDeadband();

    @Data
    public static class DynamicDeadband {

      /**
       * Enable/disable the dynamic time-deadband support. C2MON uses therefore a
       * Moving Average Counter strategy. 
       */
      private boolean enabled = false;

      /**
       * The number of counters used per tag
       */
      private int windowSize = 5;

      /**
       * The time [ms] in which the average number of tag updates is checked and the next counter is used.
       * <p>
       * Default is 1 minute.
       */
      private int checkInterval = 60_000;

      /**
       * The maximum number of tag updates per check interval averaged over the counters (windowSize).
       * If there are more than this number of updates within the window, the time deadband
       * is activated for the given tag.
       */
      private int activationThreshold = 20;

      /**
       * Threshold at which the dynamic deadband will be deactivated. If there
       * are fewer than this number of updates within the window, the time deadband
       * will deactivate for the given tag.
       */
      private int deactivationThreshold = 15;

      /**
       * The time deadband interval (ms) that will be forced if the activation threshold
       * is exceeded. Only the latest value will be sent to the sever at the given interval rate. 
       * All other value updates are filtered out.
       * <p>
       * Default is 30 sec.
       */
      private int forcedDeadbandInterval = 30_000;
    }

    /**
     * Enable/disable publication of filtered values to a broker. This is often
     * useful for gathering statistics about filtered data
     */
    private boolean publishFilteredValues = false;

    /**
     * Filtered data JMS settings
     */
    private final CommonJmsProperties jms = new CommonJmsProperties();
  }
  
  /**
   * Defines equipment specific properties.
   */
  @Data
  public static class EquipmentProperties {
    EquipmentAliveProperties alive = new EquipmentAliveProperties();
  
    @Data
    public static class EquipmentAliveProperties {
      /**
       * Enable this to option to prevent sending more alive message updates to the 
       * server than actually required by the configured frequency.
       */
      boolean filtering = false;
    }
  }
}
