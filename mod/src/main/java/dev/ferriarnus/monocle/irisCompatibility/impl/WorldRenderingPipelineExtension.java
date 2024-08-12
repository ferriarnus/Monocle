package dev.ferriarnus.monocle.irisCompatibility.impl;

public interface WorldRenderingPipelineExtension {

    default EmbeddiumTerrainPipeline getEmbeddiumTerrainPipeline() {
        return null;
    }
}
