// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.views;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.talend.commons.emf.FactoriesUtil;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.builder.database.JavaSqlFactory;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.cwm.db.connection.ConnectionUtils;
import org.talend.cwm.db.connection.MdmWebserviceConnection;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.ResourceHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.cwm.management.api.SoftwareSystemManager;
import org.talend.cwm.relational.TdColumn;
import org.talend.cwm.relational.TdTable;
import org.talend.cwm.softwaredeployment.TdSoftwareSystem;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.editor.CommonFormEditor;
import org.talend.dataprofiler.core.ui.exchange.ExchangeComponentRepNode;
import org.talend.dataprofiler.core.ui.utils.WorkbenchUtils;
import org.talend.dataprofiler.ecos.model.IEcosComponent;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.analysis.AnalysisContext;
import org.talend.dataquality.domain.pattern.Pattern;
import org.talend.dataquality.domain.pattern.PatternComponent;
import org.talend.dataquality.domain.pattern.RegularExpression;
import org.talend.dataquality.helpers.MetadataHelper;
import org.talend.dataquality.helpers.ReportHelper;
import org.talend.dataquality.properties.TDQAnalysisItem;
import org.talend.dataquality.properties.TDQPatternItem;
import org.talend.dataquality.properties.TDQReportItem;
import org.talend.dataquality.reports.TdReport;
import org.talend.dq.helper.resourcehelper.AnaResourceFileHelper;
import org.talend.dq.helper.resourcehelper.PatternResourceFileHelper;
import org.talend.dq.helper.resourcehelper.RepResourceFileHelper;
import org.talend.dq.nodes.AnalysisRepNode;
import org.talend.dq.nodes.DBCatalogRepNode;
import org.talend.dq.nodes.DBColumnRepNode;
import org.talend.dq.nodes.DBConnectionRepNode;
import org.talend.dq.nodes.DBSchemaRepNode;
import org.talend.dq.nodes.DBTableRepNode;
import org.talend.dq.nodes.DBViewRepNode;
import org.talend.dq.nodes.DFConnectionRepNode;
import org.talend.dq.nodes.DQRepositoryNode;
import org.talend.dq.nodes.MDMConnectionRepNode;
import org.talend.dq.nodes.PatternRepNode;
import org.talend.dq.nodes.ReportRepNode;
import org.talend.dq.nodes.RuleRepNode;
import org.talend.dq.nodes.SourceFileRepNode;
import org.talend.dq.nodes.SysIndicatorDefinitionRepNode;
import org.talend.repository.model.IRepositoryNode;
import org.talend.resource.ResourceManager;
import orgomg.cwm.foundation.softwaredeployment.DataManager;
import orgomg.cwm.objectmodel.core.Expression;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.PrimaryKey;
import orgomg.cwm.resource.relational.Schema;
import orgomg.cwmx.analysis.informationreporting.Report;

/**
 * Detail view of the Data profiler.
 * 
 * @author qzhang
 */
public class RespositoryDetailView extends ViewPart implements ISelectionListener {

    private Group gContainer;

    private Group tContainer;

    /**
     * DOC qzhang RespositoryDetailView constructor comment.
     */
    public RespositoryDetailView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        comp.setLayout(new FillLayout());
        ScrolledComposite scomp = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
        scomp.setLayout(new FillLayout());

        Composite composite = new Composite(scomp, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        scomp.setExpandHorizontal(true);
        scomp.setExpandVertical(true);
        scomp.setMinWidth(400);
        scomp.setMinHeight(350);
        scomp.setContent(composite);

        gContainer = new Group(composite, SWT.NONE);
        gContainer.setText(DefaultMessagesImpl.getString("RespositoryDetailView.group.General")); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2, false);
        GridData data = new GridData(GridData.FILL_BOTH);
        gContainer.setLayout(layout);
        gContainer.setLayoutData(data);

        // create extend group
        if (PluginChecker.isTDQLoaded()) {
            tContainer = new Group(composite, SWT.NONE);
            tContainer.setText(DefaultMessagesImpl.getString("RespositoryDetailView.group.Technical")); //$NON-NLS-1$
            tContainer.setLayout(new GridLayout(2, false));
            tContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

            createExtDefault();
        }

        createDefault();
        getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
        initializeToolBar();

    }

    private void createTechnicalDetail(EObject fe) {
        newLabelAndText(tContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.group.Identifier"), ResourceHelper.getUUID(fe)); //$NON-NLS-1$

        // MOD xqliu 2009-4-27 bug 6507
        // newLabelAndText(
        // tContainer,
        // DefaultMessagesImpl
        //						.getString("RespositoryDetailView.group.FilePath"), fe.eResource() //$NON-NLS-1$
        // .getURI().toPlatformString(false));
        newLabelAndText(tContainer, DefaultMessagesImpl.getString("RespositoryDetailView.group.FilePath"), //$NON-NLS-1$
                fe.eResource() == null ? "" : fe.eResource().getURI().toPlatformString(false)); //$NON-NLS-1$
        // ~
    }

    private void createTechnicalDetail(IRepositoryViewObject reposViewObj) {
        // Connection connection = ((ConnectionItem) reposViewObj.getProperty().getItem()).getConnection();
        // if (connection != null) {
        // createTechnicalDetail(connection);
        // }
        // MOD klliu the DQRepositoryview unified with tos, so refactor as follow bug 19154 2011-02-28
        if (reposViewObj.getProperty() != null) {
            Item item = reposViewObj.getProperty().getItem();
            if (item instanceof ConnectionItem) {
                Connection conn = ((ConnectionItem) item).getConnection();
                createTechnicalDetail(conn);
            } else if (item instanceof TDQAnalysisItem) {
                Analysis analysis = ((TDQAnalysisItem) item).getAnalysis();
                createTechnicalDetail(analysis);
            } else if (item instanceof TDQPatternItem) {
                Pattern pattern = ((TDQPatternItem) item).getPattern();
                createTechnicalDetail(pattern);
            } else if (item instanceof TDQReportItem) {
                Report report = ((TDQReportItem) item).getReport();
                createTechnicalDetail((TdReport) report);
            }
        }
    }

    private void createTechnicalDetail(IFile fe) {
        EObject object = getEObject(fe);

        if (object != null) {
            createTechnicalDetail(object);
        }
    }

    private void createDefault() {
        // feature 19053
        if (!gContainer.isDisposed()) {
            newText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.noAvailable")); //$NON-NLS-1$
        }
    }

    private void createExtDefault() {
        // feature 19053
        if (!tContainer.isDisposed()) {
            newText(tContainer, DefaultMessagesImpl.getString("RespositoryDetailView.noAvailable")); //$NON-NLS-1$
        }
    }

    @Override
    public void setFocus() {
        gContainer.setFocus();
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui. IWorkbenchPart,
     * org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        clearContainer();
        boolean is = true;
        if (part instanceof DQRespositoryView) {
            StructuredSelection sel = (StructuredSelection) selection;
            // MOD by zshen for bug 15750 TODO 39(13) make Detail View can be used.
            Object fe = sel.getFirstElement();
            // if(fe instanceof IRepositoryNode){
            // fe = RepositoryNodeHelper.getModelElementFromRepositoryNode((IRepositoryNode) fe);
            // }
            // MOD klliu 2011-02-24 if choose diffirent node ,that will load diffirent child ,so that not use up.
            if (fe instanceof AnalysisRepNode || fe instanceof ReportRepNode || fe instanceof SysIndicatorDefinitionRepNode
                    || fe instanceof PatternRepNode || fe instanceof RuleRepNode) {
                fe = ((IRepositoryNode) fe).getObject();
            }
            if (fe instanceof IFile) {
                IFile fe2 = (IFile) fe;
                is = createFileDetail(is, fe2);
            } else if (fe instanceof IRepositoryViewObject) {
                is = createFileDetail(is, (IRepositoryViewObject) fe);
            } else if (fe instanceof DBConnectionRepNode) {
                DBConnectionRepNode connNode = (DBConnectionRepNode) fe;
                DatabaseConnection databaseConnection = connNode.getDatabaseConnection();
                createDataProviderDetail(databaseConnection);
                is = false;
            } else if (fe instanceof DBCatalogRepNode) {
                DBCatalogRepNode catalogNode = (DBCatalogRepNode) fe;
                Catalog catalog = catalogNode.getCatalog();
                createTdCatalogDetail(catalog);
                is = false;
            } else if (fe instanceof DBSchemaRepNode) {
                DBSchemaRepNode schemaNode = (DBSchemaRepNode) fe;
                Schema schema = schemaNode.getSchema();
                createTdSchemaDetail(schema);
                is = false;
            } else if (fe instanceof DBTableRepNode) {
                DBTableRepNode tableNode = (DBTableRepNode) fe;
                // MOD gdbu 2011-9-14 TDQ-3243
                if (!DQRepositoryNode.isOnFilterring()) {
                    tableNode.getChildren().get(0).getChildren();
                }
                // ~TDQ-3243
                TdTable tdTable = tableNode.getTdTable();
                createTableDetail(tdTable);
                is = false;
            } else if (fe instanceof DBViewRepNode) {
                DBViewRepNode viewNode = (DBViewRepNode) fe;
                // MOD gdbu 2011-9-14 TDQ-3243
                if (!DQRepositoryNode.isOnFilterring()) {
                    viewNode.getChildren().get(0).getChildren();
                }
                // ~TDQ-3243
                createNameCommentDetail(viewNode.getTdView());
                is = false;
            } else if (fe instanceof DBColumnRepNode) {
                DBColumnRepNode columnNode = (DBColumnRepNode) fe;
                TdColumn column = columnNode.getTdColumn();
                createTdColumn(column);
                is = false;
            } else if (fe instanceof IEcosComponent) {
                IEcosComponent component = (IEcosComponent) fe;
                createEcosComponent(component);
                is = false;
            } else if (fe instanceof RegularExpression) {
                // MOD mzhao 2009-04-20,Bug 6349.
                RegularExpression regularExpression = (RegularExpression) fe;
                createRegularExpression(regularExpression);
                is = false;
            } else if (fe instanceof SourceFileRepNode) {
                // MOD klliu 2001-02-28 bug 19154
                IPath filePath = WorkbenchUtils.getFilePath((SourceFileRepNode) fe);
                IFile file = ResourceManager.getRootProject().getFile(filePath);
                createSqlFileDetail(file);
            } else if (fe instanceof ExchangeComponentRepNode) {
                // MOD klliu 2001-02-28 bug 19154
                IEcosComponent ecosComponent = ((ExchangeComponentRepNode) fe).getEcosComponent();
                IEcosComponent component = (IEcosComponent) ecosComponent;
                createEcosComponent(component);
                is = false;

                // ADD by msjian 2011-5-12 21186: don't check whether the selected object is "MDMConnectionRepNode"
            } else if (fe instanceof MDMConnectionRepNode) {
                MDMConnectionRepNode mdmNode = (MDMConnectionRepNode) fe;
                MDMConnection mdmConnection = mdmNode.getMdmConnection();
                createDataProviderDetail(mdmConnection);
                is = false;
            } else if (fe instanceof DFConnectionRepNode) {
                DFConnectionRepNode dfNode = (DFConnectionRepNode) fe;
                DelimitedFileConnection dfConnection = dfNode.getDfConnection();
                createDFconnectionName(dfNode.getObject().getLabel());
                createDataProviderDetail(dfConnection);
                is = false;
            }
            if (PluginChecker.isTDQLoaded()) {
                if (fe instanceof EObject) {
                    createTechnicalDetail((EObject) fe);
                } else if (fe instanceof IFile) {
                    createTechnicalDetail((IFile) fe);
                } else if (fe instanceof IRepositoryViewObject) {
                    createTechnicalDetail((IRepositoryViewObject) fe);
                } else {
                    createExtDefault();
                }
            }
        } else if (part instanceof CommonFormEditor) {
            CommonFormEditor editor = (CommonFormEditor) part;
            IEditorInput editorInput = editor.getEditorInput();
            if (editorInput instanceof IFileEditorInput) {
                IFileEditorInput input = (IFileEditorInput) editorInput;
                IFile file = input.getFile();
                is = createFileDetail(is, file);
            }
        }

        if (is) {
            createDefault();
        }
        // feature 19053
        if (!gContainer.isDisposed()) {
            gContainer.layout();
            if (tContainer != null) {
                tContainer.layout();
            }
        }
    }

    /**
     * DOC klliu Comment method "createDFconnectionName".
     * 
     * @param label
     */
    private void createDFconnectionName(String label) {
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.name"), label); //$NON-NLS-1$

    }

    /**
     * DOC bZhou Comment method "createEcosComponent".
     * 
     * @param component
     */
    private void createEcosComponent(IEcosComponent component) {
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.Name"), component.getName()); //$NON-NLS-1$
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.Author"), component.getAuthor()); //$NON-NLS-1$
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.Description"), component.getDescription()); //$NON-NLS-1$
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.Type"), component.getCategry().getName()); //$NON-NLS-1$

    }

    private void createRegularExpression(RegularExpression regularExpression) {
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.Expression"), regularExpression.getExpression().getBody()); //$NON-NLS-1$
    }

    private void createTableDetail(TdTable table) {
        createNameCommentDetail(table);
        List<PrimaryKey> primaryKeys = TableHelper.getPrimaryKeys(table);
        newLabelAndText(
                gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.PrimaryKeys"), primaryKeys.isEmpty() ? null : primaryKeys.get(0).getName() + "(" + String.valueOf(primaryKeys.get(0).getFeature().size()) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Map<String, Integer> foreignInfo = TableHelper.getForeignKeysInformation(table);
        String[] foreignNameArray = foreignInfo.keySet().toArray(new String[foreignInfo.keySet().size()]);
        newLabelAndText(
                gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.Foreignkeys"), foreignNameArray.length == 0 ? null : foreignNameArray[0] + "(" + foreignInfo.get(foreignNameArray[0]) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 1; i < foreignNameArray.length; i++) {
            newLabelAndText(gContainer, "", foreignNameArray[i] + "(" + foreignInfo.get(foreignNameArray[i]) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private boolean createFileDetail(boolean is, IFile fe2) {
        if (fe2.getFileExtension().equals(FactoriesUtil.PATTERN)) {
            Pattern pattern = PatternResourceFileHelper.getInstance().findPattern(fe2);
            createPatternDetail(pattern);
            is = false;
        } else if (fe2.getFileExtension().equals(FactoriesUtil.ANA)) {
            Analysis ana = AnaResourceFileHelper.getInstance().findAnalysis(fe2);
            createAnaysisDetail(ana);
            is = false;
        } else if (fe2.getFileExtension().equals(FactoriesUtil.REP)) {
            TdReport rep = RepResourceFileHelper.getInstance().findReport(fe2);
            createReportDetail(rep);
            is = false;
        } else if (fe2.getFileExtension().equals(FactoriesUtil.SQL)) {
            createSqlFileDetail(fe2);
            is = false;
        }
        return is;
    }

    private boolean createFileDetail(boolean is, IRepositoryViewObject reposViewObj) {
        // MOD klliu 2001-02-28 bug 19154
        if (reposViewObj.getProperty() != null) {
            Item item = reposViewObj.getProperty().getItem();
            if (item instanceof ConnectionItem) {
                Connection conn = ((ConnectionItem) item).getConnection();
                createDataProviderDetail(conn);
                is = false;
            }
            if (item instanceof TDQAnalysisItem) {
                Analysis analysis = ((TDQAnalysisItem) item).getAnalysis();
                createAnaysisDetail(analysis);
                is = false;
            }
            if (item instanceof TDQPatternItem) {
                Pattern pattern = ((TDQPatternItem) item).getPattern();
                createPatternDetail(pattern);
                is = false;
            }
            if (item instanceof TDQReportItem) {
                Report report = ((TDQReportItem) item).getReport();
                createReportDetail((TdReport) report);
                is = false;
            }
        }
        return is;
    }

    private EObject getEObject(IFile fe2) {
        EObject object = null;

        if (fe2.getFileExtension().equals(FactoriesUtil.PATTERN)) {
            object = PatternResourceFileHelper.getInstance().findPattern(fe2);
        } else if (fe2.getFileExtension().equals(FactoriesUtil.ANA)) {
            object = AnaResourceFileHelper.getInstance().findAnalysis(fe2);
        } else if (fe2.getFileExtension().equals(FactoriesUtil.REP)) {
            object = RepResourceFileHelper.getInstance().findReport(fe2);
        }

        return object;
    }

    private void createPatternDetail(Pattern pattern) {
        createName(pattern);
        createPurpose(pattern);
        createDescription(pattern);

        EList<PatternComponent> components = pattern.getComponents();
        StringBuilder description = new StringBuilder();
        for (PatternComponent poc : components) {
            if (poc instanceof RegularExpression) {
                RegularExpression expression = (RegularExpression) poc;
                description.append("  ").append(expression.getExpression().getLanguage()); //$NON-NLS-1$
            }
        }
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.type"), description.toString().trim()); //$NON-NLS-1$
    }

    private void createAnaysisDetail(Analysis ana) {
        createName(ana);
        createPurpose(ana);
        createDescription(ana);

        String description = ana.getParameters().getAnalysisType().getLiteral();
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.otherType"), description); //$NON-NLS-1$

        AnalysisContext context = ana.getContext();
        int numn = context.getAnalysedElements().size();
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.numberOfAnalyzedElements"), String.valueOf(numn)); //$NON-NLS-1$

        DataManager connection = context.getConnection();
        if (connection == null) {
            description = null;
        } else {
            description = connection.getName();
        }
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.connection"), description); //$NON-NLS-1$
    }

    private void newText(Composite composite, String inputText) {
        newText(composite, inputText, DefaultMessagesImpl.getString("RespositoryDetailView.none")); //$NON-NLS-1$
    }

    private void newLabelAndText(Composite composite, String labelString, String inputText) {
        if (composite.isDisposed()) {
            return;
        }
        Label label = new Label(composite, SWT.NONE);
        label.setText(labelString);
        newText(composite, inputText, DefaultMessagesImpl.getString("RespositoryDetailView.none")); //$NON-NLS-1$
    }

    private void newText(Composite composite, String inputText, String defaultText) {
        Text text = new Text(composite, SWT.NONE);
        text.setEditable(false);
        if (inputText == null || inputText.trim().length() == 0) {
            text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
            text.setText(defaultText);
        } else {
            text.setText(inputText);
        }
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        text.setLayoutData(data);
    }

    private void createSqlFileDetail(IFile fe2) {
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.filename"), fe2.getFullPath().toPortableString()); //$NON-NLS-1$

        // MODSCA 20080728 changed to getLocalTimeStamp() because
        // modificationStamp was 1 or 2 (=> year 1970)
        // long modificationStamp = fe2.getModificationStamp();
        long modificationStamp = fe2.getLocalTimeStamp();
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.modificationDate"), new Date(modificationStamp).toString()); //$NON-NLS-1$
    }

    private void createReportDetail(TdReport rep) {
        createName(rep);
        createPurpose(rep);
        createDescription(rep);
        int description = ReportHelper.getAnalyses(rep).size();
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.numberOfAnalyses"), String.valueOf(description)); //$NON-NLS-1$
    }

    private void createTdColumn(TdColumn column) {
        createNameCommentDetail(column);
        newLabelAndText(
                gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.typex"), column.getSqlDataType() != null ? column.getSqlDataType().getName() : ""); //$NON-NLS-1$ //$NON-NLS-2$
        String purpose = "" + column.isNullable(); //$NON-NLS-1$
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.nullable"), purpose); //$NON-NLS-1$
        final Expression initialValue = column.getInitialValue();
        String defValueText = (initialValue != null) ? initialValue.getBody() : null;
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.DefaultValue"), defValueText); //$NON-NLS-1$
        newLabelAndText(gContainer,
                DefaultMessagesImpl.getString("RespositoryDetailView.Size"), String.valueOf(column.getLength())); //$NON-NLS-1$
    }

    private void createNameCommentDetail(ModelElement element) {
        createName(element);
        String purpose = ColumnHelper.getComment(element);
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.remarks"), purpose); //$NON-NLS-1$
    }

    private void createName(ModelElement element) {
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.name"), element.getName()); //$NON-NLS-1$
    }

    private void createContextDetail(Connection connection) {
        if (connection == null) {
            return;
        }
        String contextId = connection.getContextId();
        if (contextId != null && !contextId.equals(PluginConstant.EMPTY_STRING)) {
            ContextItem contextItem = ContextUtils.getContextItemById2(contextId);
            if (contextItem != null) {
                newLabelAndText(gContainer,
                        DefaultMessagesImpl.getString("RespositoryDetailView.contextName"), contextItem.getProperty().getLabel()); //$NON-NLS-1$
            }
        }
        String contextName = connection.getContextName();
        if (contextName != null) {
            newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.contextGroupName"), contextName); //$NON-NLS-1$
        }

    }

    private void createTdSchemaDetail(Schema schema) {
        createName(schema);
    }

    private void createTdCatalogDetail(Catalog catalog) {
        createName(catalog);
    }

    private void createDataProviderDetail(Connection dataProvider) {
        boolean isDelimitedFile = ConnectionUtils.isDelimitedFileConnection(dataProvider);
        // MOD qiongli 2011-9-21 TDQ-3317
        Connection origValueConn = null;
        if (dataProvider.isContextMode()) {
            if (dataProvider instanceof DatabaseConnection) {
                origValueConn = ConnectionUtils.getOriginalDatabaseConnection((DatabaseConnection) dataProvider);

            } else if (dataProvider instanceof FileConnection) {
                origValueConn = ConnectionUtils.getOriginalFileConnection((FileConnection) dataProvider);
            }
        }
        if (!isDelimitedFile) {
            createName(origValueConn == null ? dataProvider : origValueConn);
        }
        if (origValueConn != null) {
            createContextDetail(dataProvider);
        }
        createPurpose(origValueConn == null ? dataProvider : origValueConn);
        createDescription(origValueConn == null ? dataProvider : origValueConn);
        // MOD mzhao xmldb have no actual connection.
        if (dataProvider != null) {
            String connectionString = JavaSqlFactory.getURL(dataProvider);
            newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.URL"), connectionString); //$NON-NLS-1$
        }

        // MOD sizhaoliu TDQ-5408 retrieve DB version info from software system.
        TdSoftwareSystem softwareSystem = ConnectionHelper.getSoftwareSystem(dataProvider);
        if (softwareSystem == null) { // software system info not present in
                                      // "TDQ_Libraries/.softwaresystem.softwaredeployment" file
            softwareSystem = SoftwareSystemManager.getInstance().getSoftwareSystem(dataProvider);
        }

        // MOD gdbu 2011-9-16 TDQ-3337
        String subtype = PluginConstant.EMPTY_STRING;
        String version = PluginConstant.EMPTY_STRING;
        if (softwareSystem == null) {
            if (dataProvider instanceof DatabaseConnection) {
                subtype = ((DatabaseConnection) dataProvider).getDatabaseType();
                // do not retrieve DB version here because the format (ex: MYSQL_5) is different from softwareSystem.
                if (EDatabaseConnTemplate.GENERAL_JDBC.getDBDisplayName().equals(subtype)) {
                    String dbTypeFromMetaData = ExtractMetaDataUtils.getDbTypeByClassNameAndDriverJar(
                            ((DatabaseConnection) dataProvider).getDriverClass(),
                            ((DatabaseConnection) dataProvider).getDriverJarPath());
                    subtype = PluginConstant.EMPTY_STRING.equals(dbTypeFromMetaData) ? subtype : dbTypeFromMetaData;
                }
            } else {
                boolean isMdm = ConnectionUtils.isMdmConnection(dataProvider);
                subtype = isMdm ? SupportDBUrlType.MDM.getLanguage() : isDelimitedFile ? SupportDBUrlType.DELIMITEDFILE
                        .getLanguage() : PluginConstant.EMPTY_STRING;
                if (!DQRepositoryNode.isOnFilterring()) {
                    version = isMdm ? getMDMVersion((MDMConnection) dataProvider) : PluginConstant.EMPTY_STRING;
                }
            }
        } else {
            subtype = softwareSystem.getSubtype();
            version = softwareSystem.getVersion();
        }
        // ~TDQ-3337
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.type2"), subtype); //$NON-NLS-1$
        newLabelAndText(gContainer, "Version: ", version); //$NON-NLS-1$
    }

    private void createDescription(ModelElement dataProvider) {
        String description = MetadataHelper.getDescription(dataProvider);
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.description"), description); //$NON-NLS-1$
    }

    private void createPurpose(ModelElement dataProvider) {
        String purpose = MetadataHelper.getPurpose(dataProvider);
        newLabelAndText(gContainer, DefaultMessagesImpl.getString("RespositoryDetailView.purpose"), purpose); //$NON-NLS-1$
    }

    private void clearContainer() {
        if (gContainer != null && !gContainer.isDisposed()) {
            Control[] children = gContainer.getChildren();
            for (Control control : children) {
                control.dispose();
            }
        }

        if (tContainer != null && !tContainer.isDisposed()) {
            Control[] children = tContainer.getChildren();
            for (Control control : children) {
                control.dispose();
            }
        }
    }

    private void initializeToolBar() {
        getViewSite().getActionBars().getToolBarManager();
    }

    /**
     * 
     * DOC qiongli Comment method "getMDMVersion".
     * 
     * @param mdmConn
     * @return
     */
    private String getMDMVersion(MDMConnection mdmConn) {
        String version = null;
        Properties props = new Properties();
        props.put(TaggedValueHelper.USER, mdmConn.getUsername());
        props.put(TaggedValueHelper.PASSWORD, mdmConn.getPassword());
        props.put(TaggedValueHelper.UNIVERSE, mdmConn.getUniverse() == null ? "" : mdmConn.getUniverse()); //$NON-NLS-1$
        MdmWebserviceConnection mdmWsConn = new MdmWebserviceConnection(mdmConn.getPathname(), props);
        version = mdmWsConn.getVersion();
        return version;
    }

}
