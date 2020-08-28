/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.shared.common.datatag;

import static cern.c2mon.shared.common.datatag.util.ValueDeadbandType.*;

import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;

/**
 * Definition of the supported value deadband types which can be configured for numeric DataTags
 * 
 * @author J. Stowisek
 * @deprecated Please use {@link ValueDeadbandType} instead.
 */
@Deprecated
public final class DataTagDeadband {
    /**
     * Constant to be used to disable value-based deadband filtering in a DAQ process.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     */
    public static final short DEADBAND_NONE = NONE.getId().shortValue();

    /**
     * Constant to be used to enable absolute value deadband filtering on the DAQ process level. When absolute value
     * deadband filtering is enabled, the DAQ process will only accept a new tag value if it is at least "deadbandValue"
     * greater or less than the last known value. Otherwise, the new value will be discarded.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_ABSOLUTE = PROCESS_ABSOLUTE.getId().shortValue();

    /**
     * Constant to be used to enable relative value deadband filtering on the DAQ process level. When absolute value
     * deadband filtering is enabled, the DAQ process will only accept a new tag value if it is at least "deadbandValue"
     * per cent (!) greater or less than the last known value. Otherwise, the new value will be discarded.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_RELATIVE = PROCESS_RELATIVE.getId().shortValue();

    /**
     * Constant to be used to enable absolute value deadband filtering on the equipment message handler level. When
     * absolute value deadband filtering is enabled, the message handler will only accept a new tag value if it is at
     * least "deadbandValue" greater or less than the last known value. Otherwise, the new value will be discarded. The
     * DAQ process framework will not perform any deadband filtering if this type is set.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_EQUIPMENT_ABSOLUTE = EQUIPMENT_ABSOLUTE.getId().shortValue();

    /**
     * Constant to be used to enable relative value deadband filtering on the equipment message handler level. When
     * absolute value deadband filtering is enabled, the message handler will only accept a new tag value if it is at
     * least "deadbandValue" per cent (!) greater or less than the last known value. Otherwise, the new value will be
     * discarded. The DAQ process framework will not perform any deadband filtering if this type is set.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_EQUIPMENT_RELATIVE = EQUIPMENT_RELATIVE.getId().shortValue();

    /**
     * Constant to be used to enable absolute value deadband filtering on the DAQ process level. As long as value
     * description stays unchanged, it works in exactly the same fashion as DEADBAND_PROCESS_ABSOLUTE_VALUE. If, however
     * value description change is detected, deadband filtering is skipped.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE = PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE.getId().shortValue();

    /**
     * Constant to be used to enable relative value deadband filtering on the DAQ process level. As long as value
     * description stays unchanged, it works in exactly the same fashion as DEADBAND_PROCESS_RELATIVE_VALUE. If, however
     * value description change is detected, deadband filtering is skipped.
     * 
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadbandType(short)
     * @see cern.c2mon.shared.common.datatag.DataTagAddress#setValueDeadband(float)
     */
    public static final short DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE = PROCESS_RELATIVE_VALUE_DESCR_CHANGE.getId().shortValue();

    /**
     * @return a String representation of the specified valueDeadbandType
     * @param valueDeadbandType {@link DataTagDeadband}
     */
    public static String toString(final short valueDeadbandType) {

        if (valueDeadbandType == DEADBAND_NONE)
            return "DEADBAND_NONE";
        else if (valueDeadbandType == DEADBAND_PROCESS_ABSOLUTE)
            return "DEADBAND_PROCESS_ABSOLUTE";
        else if (valueDeadbandType == DEADBAND_PROCESS_RELATIVE)
            return "DEADBAND_PROCESS_RELATIVE";
        else if (valueDeadbandType == DEADBAND_EQUIPMENT_ABSOLUTE)
            return "DEADBAND_EQUIPMENT_ABSOLUTE";
        else if (valueDeadbandType == DEADBAND_EQUIPMENT_RELATIVE)
            return "DEADBAND_EQUIPMENT_RELATIVE";
        else if (valueDeadbandType == DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE)
            return "DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE";
        else if (valueDeadbandType == DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE)
            return "DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE";
        else
            return "UNKNOWN";
    }

    /**
     * Check whether a parameter is a valid deadband
     */
    public static final boolean isValidType(final short valueDeadbandType) {
        return valueDeadbandType >= DEADBAND_NONE && valueDeadbandType <= DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE;
    }
}
