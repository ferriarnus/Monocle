package com.ferri.arnus.contacts.irisCompatibility.impl;

public interface WorldRenderingPipelineExtension {

    default EmbeddiumTerrainPipeline getEmbeddiumTerrainPipeline() {
        return null;
    }
}
