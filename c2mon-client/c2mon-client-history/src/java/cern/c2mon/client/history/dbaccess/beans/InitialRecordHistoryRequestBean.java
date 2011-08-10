/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.history.dbaccess.beans;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class is passed as a parameter when requesting the initial record for a
 * tag
 * 
 * @author vdeila
 * 
 */
public class InitialRecordHistoryRequestBean {

  /**
   * The format of the string with only the date
   */
  private static final String DATE_FORMAT = "dd.MM.yyyy";

  /** The tag id for this request */
  private final long tagId;

  /** The requested record will be having the value that was on this time */
  private Timestamp beforeTime;

  /** The format used when having only date */
  private final SimpleDateFormat dateFormat;

  /**
   * 
   * @param tagId
   *          The tag id to get the initial record for
   * @param beforeTime
   *          The requested record will be having the value that it had on this
   *          time
   */
  public InitialRecordHistoryRequestBean(final long tagId, final Timestamp beforeTime) {
    this.tagId = tagId;
    this.beforeTime = beforeTime;
    this.dateFormat = new SimpleDateFormat(DATE_FORMAT);
  }

  /**
   * 
   * @return A Timestamp which have the before date with correct timezone and
   *         zero hours, minutes and seconds
   */
  public Timestamp getLogStart() {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(getBeforeTime().getTime());
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return new Timestamp(calendar.getTimeInMillis());
  }

  /**
   * 
   * @return The day before the <code>beforeTime</code>, which then becomes the
   *         date which will be compared with to get the value from the snapshot
   *         table
   */
  public Timestamp getLogInitialDay() {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(getLogStart().getTime());
    calendar.add(Calendar.DATE, -1);
    return new Timestamp(calendar.getTimeInMillis());
  }
  
  /**
   * 
   * @return The String version of <code>getLogInitialDay</code>.
   */
  public String getLogInitialDayStr() {
    return this.dateFormat.format(getLogInitialDay());
  }

  /*
   * Getters and setters
   */

  /**
   * @return the beforeTime
   */
  public Timestamp getBeforeTime() {
    return beforeTime;
  }

  /**
   * @param beforeTime
   *          the beforeTime to set
   */
  public void setBeforeTime(final Timestamp beforeTime) {
    this.beforeTime = beforeTime;
  }

  /**
   * @return the tagId
   */
  public long getTagId() {
    return tagId;
  }
}
