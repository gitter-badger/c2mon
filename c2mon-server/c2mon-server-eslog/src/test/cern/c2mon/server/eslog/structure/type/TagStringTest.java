/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.eslog.structure.type;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.server.eslog.structure.types.TagNumeric;
import cern.c2mon.server.eslog.structure.types.TagString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.eslog.structure.mappings.Mapping.ValueType;
import cern.c2mon.server.eslog.structure.mappings.TagStringMapping;

/**
 * Tests the good behaviour of the TagString class.
 * verify that it builds correctly in JSON and accept/reject good/bad types of value.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagStringTest {
	@InjectMocks
	private TagString tagString;

	@Test
	public void testValue() {
		tagString.setValue("test");

		assertEquals("test", tagString.getValue());
		assertTrue(tagString.getValue() instanceof String);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadValue() {
		tagString.setValue(123456789);
	}

	@Test
	public void testBuild() throws IOException {
		tagString.setQuality("ok");
		String line = "\"quality\":\"ok\"";
		String text = "{\"id\":0,\"sourceTimestamp\":0,\"serverTimestamp\":0,\"daqTimestamp\":0,\"status\":0," + line + "}";

		assertEquals(text, tagString.toString());
	}

	@Test
	public void testNullValue() {
		tagString.setValue(null);
		assertNull(tagString.getValue());
	}

	@Test
	public void testGetObject() {
    String line = "{\"id\":192506,\"name\":\"CM.MEY.VGTCTESTCM11:STATUS\",\"dataType\":\"string\",\"sourceTimestamp\":0,\"serverTimestamp\":1451915554970,\"daqTimestamp\":0,\"status\":0," +
        "\"quality\":\"{}\",\"valueString\":\"DOWN\",\"valueDescription\":\"Communication fault tag indicates that equipment E_OPC_GTCTESTCM11 is down. Reason: Problems connecting to VGTCTESTCM11: Problems wih the DCOM connection occured\",\"process\":\"P_GTCTESTCM11\"," +
        "\"equipment\":\"E_OPC_GTCTESTCM11\"}";
		IFallback result = tagString.getObject(line);
    assertTrue(result instanceof TagString);
    assertEquals(line, result.toString());
	}
}