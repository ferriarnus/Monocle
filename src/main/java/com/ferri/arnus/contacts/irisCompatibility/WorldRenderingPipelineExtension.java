package com.ferri.arnus.contacts.irisCompatibility;

public interface WorldRenderingPipelineExtension {

    default EmbeddiumTerrainPipeline getEmbeddiumTerrainPipeline() {
        return null;
    }
}
