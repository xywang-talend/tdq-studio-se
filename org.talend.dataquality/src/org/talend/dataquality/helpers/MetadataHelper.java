// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.helpers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.emf.EMFUtil;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesPackage;
import org.talend.core.model.properties.Status;
import org.talend.cwm.constants.DevelopmentStatus;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.xml.TdXmlElementType;
import org.talend.dataquality.PluginConstant;
import org.talend.dataquality.indicators.DataminingType;
import org.talend.resource.ResourceManager;
import org.talend.utils.sql.Java2SqlType;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * @author scorreia
 * 
 * This class is a helper for handling data quality metadata.
 */
public final class MetadataHelper {

    private static Logger log = Logger.getLogger(MetadataHelper.class);

    private final static String PROJECT_FILE = "talend.project";

    private MetadataHelper() {
    }

    /**
     * Method "setDataminingType" sets the type of the content of a column.
     * 
     * @param type
     * @param column
     */
    public static void setDataminingType(DataminingType type, TdColumn column) {
        column.setContentType(type.getLiteral());
    }

    /**
     * DOC xqliu Comment method "setDataminingType".
     * 
     * @param type
     * @param xmlElement
     */
    public static void setDataminingType(DataminingType type, TdXmlElementType xmlElement) {
        xmlElement.setContentType(type.getLiteral());
    }

    /**
     * DOC xqliu Comment method "setDataminingType".
     * 
     * @param type
     * @param modelElement
     */
    public static void setDataminingType(DataminingType type, ModelElement modelElement) {
        if (modelElement instanceof TdColumn) {
            setDataminingType(type, (TdColumn) modelElement);
        } else if (modelElement instanceof TdXmlElementType) {
            setDataminingType(type, (TdXmlElementType) modelElement);
        }
    }

    /**
     * Method "getDataminingType" gets the type of the content of a column.
     * 
     * @param column
     * @return the DataminingType or null if none has been set.
     */
    public static DataminingType getDataminingType(TdColumn column) {
        // MOD xqliu 2009-11-27 bug 8690
        // MOD xqliu 2010-03-23 bug 12014
        String contentType = column.getContentType();
        if (contentType == null || contentType.equals("")) {
            if (ColumnHelper.isPrimaryKey(column) || ColumnHelper.isForeignKey(column)) {
                return DataminingType.NOMINAL;
            } else {
                return getDefaultDataminingType(column.getSqlDataType().getJavaDataType());
            }
        } else {
            return DataminingType.get(contentType);
        }
    }

    /**
     * method "getDefaultDataminingType" gets the default type of the content of a column.
     * 
     * @param javaSqlType
     * @return
     */
    public static DataminingType getDefaultDataminingType(int javaSqlType) {

        if (Java2SqlType.isTextInSQL(javaSqlType)) {
            return DataminingType.NOMINAL;
        }

        if (Java2SqlType.isNumbericInSQL(javaSqlType)) {
            return DataminingType.INTERVAL;
        }

        if (Java2SqlType.isDateInSQL(javaSqlType)) {
            return DataminingType.INTERVAL;
        }

        if (Java2SqlType.isOtherTypeInSQL(javaSqlType)) {
            return DataminingType.OTHER;
        }

        return null;
    }

    /**
     * DOC bZhou Comment method "setAuthor".
     * 
     * @param element
     * @param author
     * @return
     */
    public static boolean setAuthor(ModelElement element, String author) {
        return TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.AUTHOR, author);
    }

    /**
     * DOC bZhou Comment method "getAuthor".
     * 
     * @param element
     * @return
     */
    public static String getAuthor(ModelElement element) {
        TaggedValue tv = TaggedValueHelper.getTaggedValue(TaggedValueHelper.AUTHOR, element.getTaggedValue());
        if (tv == null) {
            return PluginConstant.EMPTY_STRING;
        }
        return tv.getValue();
    }

    /**
     * DOC bZhou Comment method "getDescription".
     * 
     * @param element
     * @return
     */
    public static String getDescription(ModelElement element) {
        TaggedValue tv = TaggedValueHelper.getTaggedValue(TaggedValueHelper.DESCRIPTION, element.getTaggedValue());
        if (tv == null) {
            return PluginConstant.EMPTY_STRING;
        }
        return tv.getValue();
    }

    /**
     * DOC bZhou Comment method "setDescription".
     * 
     * @param description
     * @param element
     * @return
     */
    public static boolean setDescription(String description, ModelElement element) {
        return TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.DESCRIPTION, description);
    }

    /**
     * Method "setDevStatus" sets the development status of the given element.
     * 
     * @param element
     * @param status the state to set.
     * @return
     */
    public static boolean setDevStatus(ModelElement element, String statusLabel) {
        // MOD mzhao feature 7479 2009-10-16
        if (statusLabel == null) {
            return false;
        }
        return TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.DEV_STATUS, statusLabel);
    }

    /**
     * Method "getDevStatus". MOD mzhao feature 7479
     * 
     * @param element such as Analysis, DataProvider...
     * @return the development status of the element
     */
    public static String getDevStatus(ModelElement element) {
        TaggedValue taggedValue = TaggedValueHelper.getTaggedValue(TaggedValueHelper.DEV_STATUS, element.getTaggedValue());
        if (taggedValue == null) {
            List<org.talend.core.model.properties.Status> statusList = MetadataHelper.getTechnicalStatus();
            if (statusList != null && statusList.size() > 0) {
                return statusList.get(0).getLabel();
            } else
                return DevelopmentStatus.DRAFT.getLiteral();
        }
        String statusValueString = taggedValue.getValue();
        return statusValueString;
    }

    /**
     * Method "setPurpose".
     * 
     * @param purpose the purpose to set or create
     * @param element a CWM element
     */
    public static void setPurpose(String purpose, ModelElement element) {
        TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.PURPOSE, purpose);
    }

    /**
     * Method "getPurpose".
     * 
     * @param element a CWM element
     * @return the purpose or null
     */
    public static String getPurpose(ModelElement element) {
        TaggedValue tv = TaggedValueHelper.getTaggedValue(TaggedValueHelper.PURPOSE, element.getTaggedValue());
        if (tv == null) {
            return PluginConstant.EMPTY_STRING;
        }
        return tv.getValue();
    }

    /**
     * Method "setVersion" sets the version of the given element.
     * 
     * @param version the version to set
     * @param element the element
     * @return true if the value was not set before.
     */
    public static boolean setVersion(String version, ModelElement element) {
        String statusStr = String.valueOf(version);
        return TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.VERSION, statusStr);
    }

    /**
     * Method "getVersion".
     * 
     * @param element
     * @return the version of the element
     */
    public static String getVersion(ModelElement element) {
        TaggedValue tv = TaggedValueHelper.getTaggedValue(TaggedValueHelper.VERSION, element.getTaggedValue());
        if (tv == null) {
            return VersionUtils.DEFAULT_VERSION;
        }
        return tv.getValue();
    }

    /**
     * DOC bZhou Comment method "getPropertyPath".
     * 
     * @param element
     * @return
     */
    public static String getPropertyPath(ModelElement element) {
        TaggedValue taggedValue = TaggedValueHelper.getTaggedValue(TaggedValueHelper.PROPERTY_FILE, element.getTaggedValue());
        return taggedValue == null ? null : taggedValue.getValue();
    }

    /**
     * DOC bZhou Comment method "setPropertyPath".
     * 
     * @param path
     * @param element
     */
    public static void setPropertyPath(String path, ModelElement element) {
        if (path != null && path.endsWith(FactoriesUtil.PROPERTIES_EXTENSION)) {
            TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.PROPERTY_FILE, path);
        }
    }

    public static List<Status> getTechnicalStatus() {
        org.talend.core.model.properties.Project loadProject = null;
        try {
            loadProject = loadProject();
        } catch (Exception e) {
            log.error(e, e);
            return null;
        }
        if (loadProject == null) {
            return null;
        }

        return copyList(loadProject.getTechnicalStatus());
    }

    public static List<String> toArray(List<org.talend.core.model.properties.Status> status) {
        List<String> res = new ArrayList<String>();
        int i = 0;
        for (org.talend.core.model.properties.Status s : status) {
            res.add(s.getLabel());
        }
        return res;
    }

    private static Project loadProject() throws PersistenceException {
        IProject rootProject = ResourceManager.getRootProject();
        IFile talendProjectFile = rootProject.getFile(PROJECT_FILE);
        if (!talendProjectFile.exists()) {
            return null;
        }
        URI uri = URI.createPlatformResourceURI(talendProjectFile.getFullPath().toString(), false);
        // EMFSharedResources.getInstance().unloadResource(uri.toString());
        Resource rs = new EMFUtil().getResourceSet().getResource(uri, true);
        Project emfProject = (Project) EcoreUtil.getObjectByType(rs.getContents(), PropertiesPackage.eINSTANCE.getProject());
        return emfProject;
    }

    private static List<Status> copyList(List<Status> listOfStatus) {
        List<Status> result = new ArrayList<Status>();
        for (Object obj : listOfStatus) {
            result.add((Status) obj);
        }
        return result;
    }

    /**
     * DOC xqliu Comment method "getDataminingType".
     * 
     * @param modelElement
     * @return
     */
    public static DataminingType getDataminingType(ModelElement modelElement) {
        if (modelElement instanceof TdColumn) {
            return getDataminingType((TdColumn) modelElement);
        } else if (modelElement instanceof TdXmlElementType) {
            DataminingType.get(((TdXmlElementType) modelElement).getContentType());
        }
        return getDefaultDataminingType(0);
    }

    /**
     * DOC xqliu Comment method "setRetrieveAllMetadata".
     * 
     * @param retrieveAllMetadata
     * @param element
     * @return
     */
    public static boolean setRetrieveAllMetadata(String retrieveAllMetadata, ModelElement element) {
        return TaggedValueHelper.setTaggedValue(element, TaggedValueHelper.RETRIEVE_ALL, retrieveAllMetadata);
    }

    /**
     * DOC xqliu Comment method "getRetrieveAllMetadata".
     * 
     * @param element
     * @return
     */
    public static String getRetrieveAllMetadata(ModelElement element) {
        TaggedValue tv = TaggedValueHelper.getTaggedValue(TaggedValueHelper.RETRIEVE_ALL, element.getTaggedValue());
        if (tv == null) {
            return "true";
        }
        return tv.getValue();
    }
}
