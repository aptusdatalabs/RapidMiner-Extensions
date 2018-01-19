/**
 * RapidMiner PMML Extension
 *
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 *      http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.io.pmml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.container.Pair;

/**
 * This class offers methods for converting types of rapid miner into PMML types, as well as for values as for
 * their type name.
 * 
 * @author Sebastian Land
 */
public class PMMLTranslation {

    private final static List<Pair<Integer, String>> OP_TYPE_MAPPING = new ArrayList<Pair<Integer, String>>();
    static {
        OP_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.NUMERICAL, "continuous"));
        OP_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.NOMINAL, "categorical"));
    }

    private final static List<Pair<Integer, String>> VALUE_TYPE_MAPPING = new ArrayList<Pair<Integer, String>>();
    static {
        VALUE_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.NOMINAL, "string"));
        VALUE_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.INTEGER, "integer"));
        VALUE_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.NUMERICAL, "double"));
        VALUE_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.DATE, "date"));
        VALUE_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.TIME, "time"));
        VALUE_TYPE_MAPPING.add(new Pair<Integer, String>(Ontology.DATE_TIME, "dateTime"));
    }


    /**
     * This method returns the OpType defined in the DataDictionary section of the PMML standard
     * for the given attribute.
     */
    public static String getOpType(Attribute attribute) {
        return getOpType(attribute.getValueType());
    }

    /**
     * This method returns the OpType defined in the DataDictionary section of the PMML standard
     * for the given value type of rapid miner.
     */
    public static String getOpType(int valueType) {
        for (Pair<Integer, String> pair: OP_TYPE_MAPPING) {
            if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, pair.getFirst()))
                return pair.getSecond();
        }
        throw new RuntimeException("Undefined rapid miner type (" + valueType +") in OP_TYPE_MAPPING of " + PMMLTranslation.class.getCanonicalName());
    }

    /**
     * This method returns the valueType defined in the DataDictionary section of the PMML standard
     * for the given attribute.
     */
    public static String getValueType(Attribute attribute) {
        return getValueType(attribute.getValueType());
    }

    /**
     * This method returns the OpType defined in the DataDictionary section of the PMML standard
     * for the given value type of rapid miner.
     */
    public static String getValueType(int valueType) {
        int foundType = 0;
        String foundTypeName = null;
        for (Pair<Integer, String> pair: VALUE_TYPE_MAPPING) {
            if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, pair.getFirst())) {
                if (foundTypeName == null) {
                    foundTypeName = pair.getSecond();
                    foundType = valueType;
                } else {
                    // if already found a type test if it is more specific
                    if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, foundType)) {
                        foundType = valueType;
                        foundTypeName = pair.getSecond();
                    }
                }
            }

        }
        if (foundTypeName == null)
            throw new RuntimeException("Undefined rapid miner type in VALUE_TYPE_MAPPING of " + PMMLTranslation.class.getCanonicalName());
        return foundTypeName;
    }

    /**
     * This method returns the field usage derived from the role.
     */
    public static String getFieldUsage(String role) {
        if (role == null)
            return "active";
        else if (role.equals(Attributes.LABEL_NAME))
            return "predicted";
        else if (role.equals(Attributes.WEIGHT_NAME))
            return "analysisWeight";
        else
            return "supplementary";
    }

    /**
     * This returns an double array as Array type of PMML
     */
    public static Element toArray(Document pmml, double[] array) {
        Element arrayElement = pmml.createElement("Array");
        arrayElement.setAttribute("type", "real");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0)
                builder.append(" ");
            builder.append(array[i]);
        }
        arrayElement.setTextContent(builder.toString());
        return arrayElement;
    }
}
