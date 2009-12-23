// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dq.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.cwm.dependencies.DependenciesHandler;
import org.talend.cwm.helper.DataProviderHelper;
import org.talend.cwm.helper.ModelElementHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.xml.TdXMLElement;
import org.talend.dataquality.helpers.AnalysisHelper;
import org.talend.dataquality.helpers.IndicatorHelper;
import org.talend.dataquality.helpers.MetadataHelper;
import org.talend.dataquality.indicators.CompositeIndicator;
import org.talend.dataquality.indicators.DataminingType;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dq.indicators.definitions.DefinitionHandler;
import org.talend.utils.sugars.TypedReturnCode;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.Dependency;
import orgomg.cwm.objectmodel.core.ModelElement;

/**
 * @author scorreia
 * 
 * This class helps to handle a Column analysis.
 */
public class ColumnAnalysisHandler extends AnalysisHandler {

    private static Logger log = Logger.getLogger(ColumnAnalysisHandler.class);

    /**
     * The resources that are connected to this analysis and that are potentially modified.
     */
    private Collection<Resource> modifiedResources = new HashSet<Resource>();

    /**
     * Method "addColumnToAnalyze".
     * 
     * @param column
     * @return
     */
    public boolean addColumnToAnalyze(TdColumn column) {
        assert analysis != null;
        assert analysis.getContext() != null;
        return analysis.getContext().getAnalysedElements().add(column);
    }

    public boolean addColumnsToAnalyze(Collection<TdColumn> column) {
        assert analysis != null;
        assert analysis.getContext() != null;
        return analysis.getContext().getAnalysedElements().addAll(column);
    }

    public boolean addIndicator(TdColumn column, Indicator... indicators) {
        if (!analysis.getContext().getAnalysedElements().contains(column)) {
            analysis.getContext().getAnalysedElements().add(column);
        }

        for (Indicator indicator : indicators) {
            // store first level of indicators in result.
            analysis.getResults().getIndicators().add(indicator);
            initializeIndicator(indicator, column);
        }
        DataManager connection = analysis.getContext().getConnection();
        if (connection == null) {
            // try to get one
            log.error("Connection has not been set in analysis Context");
            connection = DataProviderHelper.getTdDataProvider(column);
            analysis.getContext().setConnection(connection);
            // FIXME connection should be set elsewhere
        }
        TypedReturnCode<Dependency> rc = DependenciesHandler.getInstance().setDependencyOn(analysis, connection);
        if (rc.isOk()) {
            // DependenciesHandler.getInstance().addDependency(rc.getObject());
            Resource resource = connection.eResource();
            if (resource != null) {
                this.modifiedResources.add(resource);
            }
        }
        return true;
    }

    public boolean addIndicator(ModelElement modelElement, Indicator... indicators) {
        if (!analysis.getContext().getAnalysedElements().contains(modelElement)) {
            analysis.getContext().getAnalysedElements().add(modelElement);
        }

        for (Indicator indicator : indicators) {
            // store first level of indicators in result.
            analysis.getResults().getIndicators().add(indicator);
            initializeIndicator(indicator, modelElement);
        }
        DataManager connection = analysis.getContext().getConnection();
        if (connection == null) {
            // try to get one
            log.error("Connection has not been set in analysis Context");
            connection = ModelElementHelper.getTdDataProvider(modelElement);
            analysis.getContext().setConnection(connection);
            // FIXME connection should be set elsewhere
        }
        TypedReturnCode<Dependency> rc = DependenciesHandler.getInstance().setDependencyOn(analysis, connection);
        if (rc.isOk()) {
            // DependenciesHandler.getInstance().addDependency(rc.getObject());
            Resource resource = connection.eResource();
            if (resource != null) {
                this.modifiedResources.add(resource);
            }
        }
        return true;
    }

    private void initializeIndicator(Indicator indicator, ModelElement modelElement) {
        indicator.setAnalyzedElement(modelElement);
        // Make sure that indicator definition is set
        if (indicator.getIndicatorDefinition() == null) {
            DefinitionHandler.getInstance().setDefaultIndicatorDefinition(indicator);
        }

        // FIXME scorreia in case of composite indicators, add children to result.
        if (indicator instanceof CompositeIndicator) {
            for (Indicator child : ((CompositeIndicator) indicator).getChildIndicators()) {
                initializeIndicator(child, modelElement); // recurse
            }
        }

    }

    /**
     * Method "getDatamingType".
     * 
     * @param column
     * @return the datamining type literal if any or empty string
     */
    public String getDatamingType(TdColumn column) {
        DataminingType dmType = MetadataHelper.getDataminingType(column);
        if (dmType == null) {
            return ""; //$NON-NLS-1$
        }
        // else
        return dmType.getLiteral();
    }

    /**
     * Method "getIndicators".
     * 
     * @param column
     * @return the indicators attached to this column
     */
    public Collection<Indicator> getIndicators(TdColumn column) {
        Collection<Indicator> indics = new ArrayList<Indicator>();
        EList<Indicator> allIndics = analysis.getResults().getIndicators();
        for (Indicator indicator : allIndics) {
            if (indicator.getAnalyzedElement() != null && indicator.getAnalyzedElement().equals(column)) {
                indics.add(indicator);
            }
        }
        return indics;
    }

    /**
     * Method "getIndicatorLeaves" returns the indicators for the given column at the leaf level.
     * 
     * @param column
     * @return the indicators attached to this column
     */
    public Collection<Indicator> getIndicatorLeaves(TdColumn column) {
        // get the leaf indicators
        Collection<Indicator> leafIndics = IndicatorHelper.getIndicatorLeaves(analysis.getResults());
        // filter only indicators for this column
        Collection<Indicator> indics = new ArrayList<Indicator>();
        for (Indicator indicator : leafIndics) {
            if (indicator.getAnalyzedElement() != null && indicator.getAnalyzedElement().equals(column)) {
                indics.add(indicator);
            }
        }
        return indics;
    }

    /**
     * Method "setStringDataFilter".
     * 
     * @param datafilterString
     * @return true when a new data filter is created, false if it is only updated
     */
    public boolean setStringDataFilter(String datafilterString) {
        return AnalysisHelper.setStringDataFilter(analysis, datafilterString);
    }

    public String getStringDataFilter() {
        return AnalysisHelper.getStringDataFilter(analysis);
    }

    /**
     * DOC xqliu Comment method "setDatamingType".
     * 
     * @param dataminingTypeLiteral
     * @param modelElement
     */
    public void setDatamingType(String dataminingTypeLiteral, ModelElement modelElement) {
        DataminingType type = DataminingType.get(dataminingTypeLiteral);
        if (modelElement instanceof TdColumn) {
            MetadataHelper.setDataminingType(type, (TdColumn) modelElement);
        } else if (modelElement instanceof TdXMLElement) {
            MetadataHelper.setDataminingType(type, (TdXMLElement) modelElement);
        } else {
            return;
        }
        Resource resource = modelElement.eResource();
        if (resource != null) {
            resource.setModified(true); // tell that the resource has been modified.
            // it would be better to handle modifications with EMF Commands
            this.modifiedResources.add(resource);
        }
    }
}
