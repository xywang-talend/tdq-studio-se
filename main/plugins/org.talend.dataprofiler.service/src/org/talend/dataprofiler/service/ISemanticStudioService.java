package org.talend.dataprofiler.service;

import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.dataquality.analysis.Analysis;

public interface ISemanticStudioService {

    void recommandAnalysis(MetadataTable metadataTable);

    void addAnalysisToRef(Analysis analysis);

}
