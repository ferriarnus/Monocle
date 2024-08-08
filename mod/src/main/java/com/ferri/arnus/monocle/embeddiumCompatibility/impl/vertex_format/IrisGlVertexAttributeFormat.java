package com.ferri.arnus.monocle.embeddiumCompatibility.impl.vertex_format;

import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeFormat;
import org.lwjgl.opengl.GL20C;

public class IrisGlVertexAttributeFormat {
	public static final GlVertexAttributeFormat BYTE = new GlVertexAttributeFormat(GL20C.GL_BYTE, 1);
	public static final GlVertexAttributeFormat SHORT = new GlVertexAttributeFormat(GL20C.GL_SHORT, 2);
}

