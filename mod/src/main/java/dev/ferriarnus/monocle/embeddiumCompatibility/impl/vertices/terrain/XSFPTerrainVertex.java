package dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain;

import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.irisshaders.iris.vertices.sodium.terrain.BlockContextHolder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.XSFPModelVertexType.STRIDE;

public class XSFPTerrainVertex implements ChunkVertexEncoder, VertexEncoderInterface {
	private final QuadViewXSFPTerrain.QuadViewTerrainUnsafe quad = new QuadViewXSFPTerrain.QuadViewTerrainUnsafe();
	private final Vector3f normal = new Vector3f();

	private BlockContextHolder contextHolder;

	private int vertexCount;
	private float uSum;
	private float vSum;
	private boolean flipUpcomingNormal;

	// TODO: FIX

	/*@Override
	public void copyQuadAndFlipNormal() {
		ensureCapacity(4);

		MemoryUtil.memCopy(this.writePointer - STRIDE * 4, this.writePointer, STRIDE * 4);

		// Now flip vertex normals
		int packedNormal = MemoryUtil.memGetInt(this.writePointer + 32);
		int inverted = NormalHelper.invertPackedNormal(packedNormal);

		MemoryUtil.memPutInt(this.writePointer + 32, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE * 2, inverted);
		MemoryUtil.memPutInt(this.writePointer + 32 + STRIDE * 3, inverted);

		// We just wrote 4 vertices, advance by 4
		for (int i = 0; i < 4; i++) {
			this.advance();
		}

		// Ensure vertices are flushed
		this.flush();
	}*/

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		this.contextHolder = holder;
	}

	//@Override
	public void flipUpcomingQuadNormal() {
		flipUpcomingNormal = true;
	}

	@Override
	public long write(long ptr,
					  Material material, Vertex vertex, int chunkId) {
		uSum += vertex.u;
		vSum += vertex.v;
		vertexCount++;

		MemoryUtil.memPutFloat(ptr, vertex.x);
		MemoryUtil.memPutFloat(ptr + 4, vertex.y);
		MemoryUtil.memPutFloat(ptr + 8, vertex.z);

		MemoryUtil.memPutInt(ptr + 12, vertex.color);

		MemoryUtil.memPutFloat(ptr + 16, vertex.u);
		MemoryUtil.memPutFloat(ptr + 20, vertex.v);

		MemoryUtil.memPutInt(ptr + 24, encodeDrawParameters(material, chunkId) << 0 | encodeLight(vertex.light) << 16);

		MemoryUtil.memPutInt(ptr + 40, packBlockId(contextHolder));
		MemoryUtil.memPutInt(ptr + 44, contextHolder.ignoreMidBlock() ? 0 : ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, contextHolder.getLocalPosX(), contextHolder.getLocalPosY(), contextHolder.getLocalPosZ()));
		MemoryUtil.memPutByte(ptr + 47, contextHolder.getBlockEmission());

		if (vertexCount == 4) {
			vertexCount = 0;

			// FIXME
			// The following logic is incorrect because OpenGL denormalizes shorts by dividing by 65535. The atlas is
			// based on power-of-two values and so a normalization factor that is not a power of two causes the values
			// used in the shader to be off by enough to cause visual errors. These are most noticeable on 1.18 with POM
			// on block edges.
			//
			// The only reliable way that this can be fixed is to apply the same shader transformations to midTexCoord
			// as Sodium does to the regular texture coordinates - dividing them by the correct power-of-two value inside
			// of the shader instead of letting OpenGL value normalization do the division. However, this requires
			// fragile patching that is not yet possible.
			//
			// As a temporary solution, the normalized shorts have been replaced with regular floats, but this takes up
			// an extra 4 bytes per vertex.

			// NB: Be careful with the math here! A previous bug was caused by midU going negative as a short, which
			// was sign-extended into midTexCoord, causing midV to have garbage (likely NaN data). If you're touching
			// this code, be aware of that, and don't introduce those kinds of bugs!
			//
			// Also note that OpenGL takes shorts in the range of [0, 65535] and transforms them linearly to [0.0, 1.0],
			// so multiply by 65535, not 65536.
			//
			// TODO: Does this introduce precision issues? Do we need to fall back to floats here? This might break
			// with high resolution texture packs.
//			int midU = (int)(65535.0F * Math.min(uSum * 0.25f, 1.0f)) & 0xFFFF;
//			int midV = (int)(65535.0F * Math.min(vSum * 0.25f, 1.0f)) & 0xFFFF;
//			int midTexCoord = (midV << 16) | midU;

			uSum *= 0.25f;
			vSum *= 0.25f;

			int midUV = XHFPModelVertexType.encodeTexture(uSum, vSum);

			MemoryUtil.memPutInt(ptr + 28, midUV);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE, midUV);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 2, midUV);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 3, midUV);

			uSum = 0;
			vSum = 0;

			// normal computation
			// Implementation based on the algorithm found here:
			// https://github.com/IrisShaders/ShaderDoc/blob/master/vertex-format-extensions.md#surface-normal-vector

			quad.setup(ptr, STRIDE);
			if (flipUpcomingNormal) {
				NormalHelper.computeFaceNormalFlipped(normal, quad);
				flipUpcomingNormal = false;
			} else {
				NormalHelper.computeFaceNormal(normal, quad);
			}
			int packedNormal = NormI8.pack(normal);


			MemoryUtil.memPutInt(ptr + 36, packedNormal);
			MemoryUtil.memPutInt(ptr + 36 - STRIDE, packedNormal);
			MemoryUtil.memPutInt(ptr + 36 - STRIDE * 2, packedNormal);
			MemoryUtil.memPutInt(ptr + 36 - STRIDE * 3, packedNormal);

			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, quad);

			MemoryUtil.memPutInt(ptr + 32, tangent);
			MemoryUtil.memPutInt(ptr + 32 - STRIDE, tangent);
			MemoryUtil.memPutInt(ptr + 32 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(ptr + 32 - STRIDE * 3, tangent);
		}

		return ptr + STRIDE;
	}

	private static int encodeDrawParameters(Material material, int sectionIndex) {
		return (sectionIndex & 255) << 8 | (material.bits() & 255) << 0;
	}

	private static int encodeLight(int light) {
		int block = light & 255;
		int sky = light >> 16 & 255;
		return block << 0 | sky << 8;
	}

	private int packBlockId(BlockContextHolder contextHolder) {
		return ((contextHolder.getBlockId() + 1) << 1) | (contextHolder.getRenderType() & 1);
	}
}
