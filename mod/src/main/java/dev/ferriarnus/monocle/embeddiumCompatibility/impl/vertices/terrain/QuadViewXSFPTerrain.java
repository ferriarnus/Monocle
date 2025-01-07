package dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain;

import net.irisshaders.iris.vertices.views.QuadView;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class QuadViewXSFPTerrain implements QuadView {
	long writePointer;
	int stride;

	@Override
	public float x(int index) {
		return getFloat(writePointer - (long) stride * (3 - index));
	}

	@Override
	public float y(int index) {
		return getFloat(writePointer + 4 - (long) stride * (3 - index));
	}

	@Override
	public float z(int index) {
		return getFloat(writePointer + 8 - (long) stride * (3 - index));
	}

	@Override
	public float u(int index) {
		return getFloat(writePointer + 16 - (long) stride * (3 - index));
	}

	@Override
	public float v(int index) {
		return getFloat(writePointer + 20 - (long) stride * (3 - index));
	}

	abstract float getFloat(long writePointer);

	public static class QuadViewTerrainUnsafe extends QuadViewXSFPTerrain {
		public void setup(long writePointer, int stride) {
			this.writePointer = writePointer;
			this.stride = stride;
		}

		@Override
		float getFloat(long writePointer) {
			return MemoryUtil.memGetFloat(writePointer);
		}
	}

	public static class QuadViewTerrainNio extends QuadViewXSFPTerrain {
		private ByteBuffer buffer;

		public void setup(ByteBuffer buffer, int writePointer, int stride) {
			this.buffer = buffer;
			this.writePointer = writePointer;
			this.stride = stride;
		}

		@Override
		float getFloat(long writePointer) {
			return buffer.getFloat((int) writePointer);
		}
	}
}
