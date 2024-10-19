package dev.ferriarnus.monocle.irisCompatibility.impl;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;

public class EmbeddiumCoreTransformer {
	public static void transform(
		ASTParser t,
		TranslationUnit tree,
		Root root,
		EmbeddiumParameters parameters) {
		root.rename("alphaTestRef", "iris_currentAlphaTest");
		root.rename("modelViewMatrix", "iris_ModelViewMatrix");
		root.rename("modelViewMatrixInverse", "iris_ModelViewMatrixInverse");
		root.rename("projectionMatrix", "iris_ProjectionMatrix");
		root.rename("projectionMatrixInverse", "iris_ProjectionMatrixInverse");
		root.rename("normalMatrix", "iris_NormalMatrix");
		root.rename("chunkOffset", "u_RegionOffset");
		tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
			"uniform mat4 iris_LightmapTextureMatrix;");
		if (parameters.type == PatchShaderType.VERTEX) {
			// _draw_translation replaced with Chunks[_draw_id].offset.xyz
			root.replaceReferenceExpressions(t, "vaPosition", "_vert_position + _get_draw_translation(_draw_id)");
			root.replaceReferenceExpressions(t, "vaColor", "_vert_color");
			root.rename("vaNormal", "iris_Normal");
			root.replaceReferenceExpressions(t, "vaUV0", "_vert_tex_diffuse_coord");
			root.replaceReferenceExpressions(t, "vaUV1", "ivec2(0, 10)");
			root.rename("vaUV2", "a_LightCoord");

			root.replaceReferenceExpressions(t, "textureMatrix", "mat4(1.0)");

			EmbeddiumTransformer.injectVertInit(t, tree, root, parameters);
		}
	}
}
