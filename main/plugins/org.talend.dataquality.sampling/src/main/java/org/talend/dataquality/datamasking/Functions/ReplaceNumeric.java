// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.datamasking.Functions;

import org.talend.dataquality.datamasking.Function;

/**
 * created by jgonzalez on 22 juin 2015. This function will replace every digit by the parameter.
 *
 */
public abstract class ReplaceNumeric<T2> extends Function<T2> {

    @Override
    public abstract T2 generateMaskedRow(T2 t);
}