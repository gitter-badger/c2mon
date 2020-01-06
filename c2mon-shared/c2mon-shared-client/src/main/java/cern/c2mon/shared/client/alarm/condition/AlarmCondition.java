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
package cern.c2mon.shared.client.alarm.condition;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cern.c2mon.shared.common.type.TypeConverter;
import cern.c2mon.shared.util.parser.SimpleXMLParser;


/**
 * <b>Imported as-is into C2MON.</b>
 * <p/>
 * Common interface for defining TIM alarm conditions.
 * <p/>
 * AlarmCondition objects are used in the TIM system (by the AlarmCacheObject
 * as well as the Alarm entity bean) in order to provide a simple means for
 * finding out whether the state of an alarm is supposed to be "active" or
 * "terminated" when a new value arrives.
 * <p/>
 * AlarmCondition is Serializable. Make sure to define a serialVersionUID in
 * all subclasses in order to make sure that no serialization problems occur
 * after minor modifications in the classes!
 *
 * @author Jan Stowisek
 */
public abstract class AlarmCondition implements Serializable {

  /** Serial version UID */
  private static final long serialVersionUID = 963875467077605494L;

  /**
   * The active fault state descriptor. Copied from
   * <code>cern.laser.source.alarmsysteminterface.FaultState</code>
   * to avoid dependencies.
   */
  public static final String ACTIVE = "ACTIVE";

  /**
   * The terminate fault state descriptor. Copied from
   * <code>cern.laser.source.alarmsysteminterface.FaultState</code>
   * to avoid dependencies.
   */
  public static final String TERMINATE = "TERMINATE";

  private static SimpleXMLParser xmlParser = null;


  /**
   * Returns the appropriate alarm state (i.e. the fault state descriptor
   * in LASER) for the given tag value.
   * The only allowed return values are FaultState.TERMINATE or FaultState.ACTIVE.
   */
  public abstract boolean evaluateState(Object value);

  /**
   * Clone method
   * @return a deep clone of this AlarmCondition object.
   */
  @Override
  public abstract Object clone();

  public final String getXMLCondition() {
    return this.toConfigXML();
  }

  /**
   * Returns a standardised XML representation of the AlarmCondition object.
   *
   * @throws RuntimeException if errors occur during encoding to XML
   */
  public final synchronized String toConfigXML() {
    // The concrete subclass of AlarmCondition
    Class<?> conditionClass = this.getClass();
    // The declared fields of this subclass
    Field[] fields = conditionClass.getDeclaredFields();


    Class<?> superClass = conditionClass.getSuperclass();
    while (superClass != null && superClass != AlarmCondition.class && superClass != Object.class) {
      fields = (Field[]) ArrayUtils.addAll(fields, superClass.getDeclaredFields());
      superClass = superClass.getSuperclass();
    }

    // Temporary variable for constructing the XML string
    StringBuilder str = new StringBuilder();
    // Temporary variable for storing the XML name of a field
    String fieldXMLName = null;
    // Temporary variable for storing the class name of a field's value
    String fieldClassName = null;
    // Temporary variable for storing the value of a field
    Object fieldVal = null;

    /* Open the <AlarmCondition> tag */
    str.append("<AlarmCondition class=\"");
    str.append(conditionClass.getName());
    str.append("\">\n");

    for (Field field : fields) {
      if (!Modifier.isFinal(field.getModifiers())) {
        try {
          field.setAccessible(true);
          fieldVal = field.get(this);
          if (fieldVal != null) {
            fieldClassName = fieldVal.getClass().getName();
            fieldXMLName = encodeFieldName(field.getName());

            str.append("  <");
            str.append(fieldXMLName);
            str.append(" type=\"");
            if (fieldClassName.indexOf("java.lang") == -1) {
              str.append(fieldClassName);
            }
            else {
              str.append(fieldClassName.substring(10));
            }
            str.append("\">");
            str.append(fieldVal);
            str.append("</");
            str.append(fieldXMLName);
            str.append(">\n");
          }
        } catch (IllegalAccessException iae) {
          throw new RuntimeException(iae);
        }
      }
    }

    str.append("</AlarmCondition>\n");
    return str.toString();
  }

  /**
   * Create an AlarmCondition object from its standardized XML representation.
   * @param element DOM element containing the XML representation of an
   * AlarmCondition object, as created by the toConfigXML() method.
   */
  public static final synchronized AlarmCondition fromConfigXML(Element element) {
    Class<?> alarmConditionClass = null;
    AlarmCondition alarmCondition = null;

    try {
      alarmConditionClass = Class.forName(element.getAttribute("class"));
      alarmCondition = (AlarmCondition) alarmConditionClass.newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
      throw new RuntimeException(ex);
    }

    setFields(element, alarmConditionClass, alarmCondition);

    // Return the fully configured HardwareAddress object
    return alarmCondition;
  }

  private static void setFields(Element element, Class<?> alarmConditionClass, AlarmCondition alarmCondition) {
    NodeList fields = element.getChildNodes();
    Node fieldNode = null;
    int fieldsCount = fields.getLength();
    String fieldName;
    String fieldValueString;

    for (int i = 0; i < fieldsCount; i++) {
      fieldNode = fields.item(i);
      if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
        fieldName = fieldNode.getNodeName();
        fieldValueString = fieldNode.getFirstChild().getNodeValue();
        try {
          Method[] methods = alarmConditionClass.getMethods();
          String setterMethod = decodeFieldMethodSetterName(fieldName);
          for (Method method : methods) {
            if (method.getName().equals(setterMethod)) {
              String fieldTypeName = fieldNode.getAttributes().getNamedItem("type").getNodeValue();
              method.invoke(alarmCondition, TypeConverter.cast(fieldValueString, fieldTypeName));
            }
          }
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }
  }

  /**
   * Create an AlarmCondition object from its standardized XML representation.
   *
   * @param pElement DOM element containing the XML representation of an
   * AlarmCondition object, as created by the toConfigXML() method.
   *
   * @throws RuntimeException if errors occur during parsing of XML
   */
  public static final synchronized AlarmCondition fromConfigXML(String pXML) {
    if (xmlParser == null) {
      try {
        xmlParser = new SimpleXMLParser();
      } catch (ParserConfigurationException e) {
        throw new RuntimeException(e);
      }
    }
    return fromConfigXML(xmlParser.parse(pXML).getDocumentElement());
  }


  //----------------------------------------------------------------------------
  // Private utility methods
  //----------------------------------------------------------------------------

  /**
   * Decodes a field name from XML notation (e.g. my-field-name) to a valid Java
   * field name (e.g. myFieldName)
   */
  private static final String decodeFieldName(final String pXmlFieldName) {
    // StringBuffer for constructing the resulting field name
    StringBuilder str = new StringBuilder();
    // Number of characters in the XML-encoded field name
    int fieldNameLength = pXmlFieldName.length();

    char currentChar;
    for (int i= 0; i < fieldNameLength; i++) {
      currentChar = pXmlFieldName.charAt(i);
      if (currentChar == '-') {
        str.append(Character.toUpperCase(pXmlFieldName.charAt(++i)));
      } else {
        str.append(currentChar);
      }
    }
    return str.toString();
  }

  /**
   * Decodes a field name from XML notation (e.g. my-field-name) to a valid Java
   * field name (e.g. myFieldName)
   */
  private static final String decodeFieldMethodSetterName(final String pXmlFieldName) {
    // StringBuffer for constructing the resulting field name
    StringBuilder str = new StringBuilder();
    // Number of characters in the XML-encoded field name
    int fieldNameLength = pXmlFieldName.length();

    str.append("set");
    str.append(Character.toUpperCase(pXmlFieldName.charAt(0)));

    char currentChar;
    for (int i = 1; i < fieldNameLength; i++) {
      currentChar = pXmlFieldName.charAt(i);
      if (currentChar == '-') {
        str.append(Character.toUpperCase(pXmlFieldName.charAt(++i)));
      } else {
        str.append(currentChar);
      }
    }

    return str.toString();
  }

  /**
   * Encodes a field name in Java notation (e.g. myFieldName) to an XML
   * field name (e.g. my-field-name).
   */
  private final String encodeFieldName(final String pFieldName) {
    // StringBuffer for constructing the resulting XML-encoded field name
    StringBuffer str = new StringBuffer();
    // Number of characters in the field name
    int fieldNameLength = pFieldName.length();

    char currentChar;
    for (int i= 0; i != fieldNameLength; i++) {
      currentChar =  pFieldName.charAt(i);
      if (Character.isUpperCase(currentChar)) {
        str.append('-');
        str.append(Character.toLowerCase(currentChar));
      } else {
        str.append(currentChar);
      }
    }
    return str.toString();
  }
}
