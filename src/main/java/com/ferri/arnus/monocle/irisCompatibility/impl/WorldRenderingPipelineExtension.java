package com.ferri.arnus.monocle.irisCompatibility.impl;

public interface WorldRenderingPipelineExtension {

    default EmbeddiumTerrainPipeline getEmbeddiumTerrainPipeline() {
        return null;
    }
}
