package dev.ferriarnus.monocle.irisCompatibility.impl;

public interface WorldRenderingPipelineExtension {

    default EmbeddiumPrograms getEmbeddiumPrograms() {
        return null;
    }
}
