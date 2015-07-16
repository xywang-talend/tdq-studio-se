// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.datascience.common.inference.type;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * Data type bean hold type to frequency and type to value maps.
 */
public class DataType {

    private Map<Type, Long> typeFrequencies = new EnumMap<Type, Long>(Type.class);

    private Map<Type, List<String>> type2Values = new EnumMap<Type, List<String>>(Type.class);

    public Map<Type, Long> getTypeFrequencies() {
        return typeFrequencies;
    }

    /**
     * Get suggested type
     * 
     * @return type suggested by system automatically given frequencies.
     */
    public Type getSuggestedType() {
        long max = 0;
        Type electedType = Type.STRING; // String by default
        for (Map.Entry<Type, Long> entry : typeFrequencies.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                electedType = entry.getKey();
            }
        }
        return electedType;
    }

    /**
     * Increment by 1 from frequency table.
     * @param type the type from which the frequencies incremnt.
     */
    public void increment(Type type) {
        if (!typeFrequencies.containsKey(type)) {
            typeFrequencies.put(type, 1l);
        } else {
            typeFrequencies.put(type, typeFrequencies.get(type) + 1);
        }
    }

    /**
     * Increment the frequency of given type and value simultaneously .
     * @param type the given type.
     * @param value value to be appended to map given type
     */
    public void increment(Type type, String value) {
        increment(type);
        // update type to values map
        if (!type2Values.containsKey(type)) {
            List<String> values = type2Values.get(type);
            if (values == null) {
                values = Collections.synchronizedList(new LinkedList<String>());
            }
            values.add(value);
            type2Values.put(type, values);
        } else {
            List<String> values = type2Values.get(type);
            values.add(value);
        }
    }

    public enum Type {
        BOOLEAN,
        CHAR,
        INTEGER,
        DOUBLE,
        STRING,
        DATE
    }
}
