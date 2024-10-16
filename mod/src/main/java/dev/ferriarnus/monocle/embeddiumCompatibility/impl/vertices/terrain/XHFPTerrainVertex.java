package dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain;

import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.irisshaders.iris.vertices.NormI8;
import net.irisshaders.iris.vertices.NormalHelper;
import net.irisshaders.iris.vertices.sodium.terrain.BlockContextHolder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.minecraft.util.Mth;
import org.embeddedt.embeddium.impl.render.chunk.terrain.material.Material;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import static dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.XHFPModelVertexType.STRIDE;

public class XHFPTerrainVertex implements ChunkVertexEncoder, VertexEncoderInterface {
	private final QuadViewTerrain.QuadViewTerrainUnsafe quad = new QuadViewTerrain.QuadViewTerrainUnsafe();
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

		MemoryUtil.memPutShort(ptr, XHFPModelVertexType.encodePosition(vertex.x));
		MemoryUtil.memPutShort(ptr + 2L, XHFPModelVertexType.encodePosition(vertex.y));
		MemoryUtil.memPutShort(ptr + 4L, XHFPModelVertexType.encodePosition(vertex.z));
		MemoryUtil.memPutByte(ptr + 6L, (byte) material.bits());
		MemoryUtil.memPutByte(ptr + 7L, (byte) chunkId);

		MemoryUtil.memPutInt(ptr + 8, vertex.color);

		MemoryUtil.memPutInt(ptr + 12, XHFPModelVertexType.encodeTexture(vertex.u, vertex.v));

		MemoryUtil.memPutInt(ptr + 16, vertex.light);

		MemoryUtil.memPutInt(ptr + 32, packBlockId(contextHolder));
		MemoryUtil.memPutInt(ptr + 36, contextHolder.ignoreMidBlock() ? 0 : ExtendedDataHelper.computeMidBlock(vertex.x, vertex.y, vertex.z, contextHolder.getLocalPosX(), contextHolder.getLocalPosY(), contextHolder.getLocalPosZ()));
		MemoryUtil.memPutByte(ptr + 39, contextHolder.getBlockEmission());

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

			MemoryUtil.memPutInt(ptr + 20, midUV);
			MemoryUtil.memPutInt(ptr + 20 - STRIDE, midUV);
			MemoryUtil.memPutInt(ptr + 20 - STRIDE * 2, midUV);
			MemoryUtil.memPutInt(ptr + 20 - STRIDE * 3, midUV);

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


			MemoryUtil.memPutInt(ptr + 28, packedNormal);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE, packedNormal);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 2, packedNormal);
			MemoryUtil.memPutInt(ptr + 28 - STRIDE * 3, packedNormal);

			int tangent = NormalHelper.computeTangent(normal.x, normal.y, normal.z, quad);

			MemoryUtil.memPutInt(ptr + 24, tangent);
			MemoryUtil.memPutInt(ptr + 24 - STRIDE, tangent);
			MemoryUtil.memPutInt(ptr + 24 - STRIDE * 2, tangent);
			MemoryUtil.memPutInt(ptr + 24 - STRIDE * 3, tangent);
		}

		return ptr + STRIDE;
	}

	private static int packPositionHi(int x, int y, int z) {
		return (x >>> 10 & 1023) << 0 | (y >>> 10 & 1023) << 10 | (z >>> 10 & 1023) << 20;
	}

	private static int packPositionLo(int x, int y, int z) {
		return (x & 1023) << 0 | (y & 1023) << 10 | (z & 1023) << 20;
	}

	private static int quantizePosition(float position) {
		return (int)(normalizePosition(position) * 1048576.0F) & 1048575;
	}

	private static float normalizePosition(float v) {
		return (8.0F + v) / 32.0F;
	}

	private static int packTexture(int u, int v) {
		return (u & '\uffff') << 0 | (v & '\uffff') << 16;
	}

	private static int encodeTexture(float center, float x) {
		int bias = x < center ? 1 : -1;
		int quantized = floorInt(x * 32768.0F) + bias & 32767;
		if (bias < 0) {
			quantized = -quantized;
		}

		return quantized;
	}

	private static int encodeLight(int light) {
		int sky = Mth.clamp(light >>> 16 & 255, 8, 248);
		int block = Mth.clamp(light >>> 0 & 255, 8, 248);
		return block << 0 | sky << 8;
	}

	private static int packLightAndData(int light, int material, int section) {
		return (light & '\uffff') << 0 | (material & 255) << 16 | (section & 255) << 24;
	}

	private static int floorInt(float x) {
		return (int)Math.floor((double)x);
	}

	private int packBlockId(BlockContextHolder contextHolder) {
		return ((contextHolder.getBlockId() + 1) << 1) | (contextHolder.getRenderType() & 1);
	}
}
