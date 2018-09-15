/*
 * Copyright (C) 2013-2017 Steveice10, 2018 Una Thompson (unascribed)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.steveice10.opennbt;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.github.steveice10.opennbt.tag.NBTCompound;
import io.github.steveice10.opennbt.tag.NBTTag;

/**
 * A class containing methods for reading/writing NBT tags.
 */
public class NBTIO {
	/**
	 * Reads the compressed, big endian root CompoundTag from the given file.
	 *
	 * @param path Path of the file.
	 * @return The read compound tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTCompound readFile(String path) throws IOException {
		return readFile(new File(path));
	}

	/**
	 * Reads the compressed, big endian root CompoundTag from the given file.
	 *
	 * @param file File to read from.
	 * @return The read compound tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTCompound readFile(File file) throws IOException {
		return readFile(file, true, false);
	}

	/**
	 * Reads the root CompoundTag from the given file.
	 *
	 * @param path		 Path of the file.
	 * @param compressed   Whether the NBT file is compressed.
	 * @param littleEndian Whether the NBT file is little endian.
	 * @return The read compound tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTCompound readFile(String path, boolean compressed, boolean littleEndian) throws IOException {
		return readFile(new File(path), compressed, littleEndian);
	}

	/**
	 * Reads the root CompoundTag from the given file.
	 *
	 * @param file		 File to read from.
	 * @param compressed   Whether the NBT file is compressed.
	 * @param littleEndian Whether the NBT file is little endian.
	 * @return The read compound tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTCompound readFile(File file, boolean compressed, boolean littleEndian) throws IOException {
		InputStream in = new FileInputStream(file);
		if(compressed) {
			in = new GZIPInputStream(in);
		}

		NBTTag tag = readTag(in, littleEndian);
		if(!(tag instanceof NBTCompound)) {
			throw new IOException("Root tag is not a CompoundTag!");
		}

		return (NBTCompound) tag;
	}

	/**
	 * Writes the given root CompoundTag to the given file, compressed and in big endian.
	 *
	 * @param tag  Tag to write.
	 * @param path Path to write to.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeFile(NBTCompound tag, String path) throws IOException {
		writeFile(tag, new File(path));
	}

	/**
	 * Writes the given root CompoundTag to the given file, compressed and in big endian.
	 *
	 * @param tag  Tag to write.
	 * @param file File to write to.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeFile(NBTCompound tag, File file) throws IOException {
		writeFile(tag, file, true, false);
	}

	/**
	 * Writes the given root CompoundTag to the given file.
	 *
	 * @param tag		  Tag to write.
	 * @param path		 Path to write to.
	 * @param compressed   Whether the NBT file should be compressed.
	 * @param littleEndian Whether to write little endian NBT.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeFile(NBTCompound tag, String path, boolean compressed, boolean littleEndian) throws IOException {
		writeFile(tag, new File(path), compressed, littleEndian);
	}

	/**
	 * Writes the given root CompoundTag to the given file.
	 *
	 * @param tag		  Tag to write.
	 * @param file		 File to write to.
	 * @param compressed   Whether the NBT file should be compressed.
	 * @param littleEndian Whether to write little endian NBT.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeFile(NBTCompound tag, File file, boolean compressed, boolean littleEndian) throws IOException {
		if(!file.exists()) {
			if(file.getParentFile() != null && !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			file.createNewFile();
		}

		OutputStream out = new FileOutputStream(file);
		if(compressed) {
			out = new GZIPOutputStream(out);
		}

		writeTag(out, tag, littleEndian);
		out.close();
	}

	/**
	 * Reads a big endian NBT tag.
	 *
	 * @param in Input stream to read from.
	 * @return The read tag, or null if the tag is an end tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTTag readTag(InputStream in) throws IOException {
		return readTag(in, false);
	}

	/**
	 * Reads an NBT tag.
	 *
	 * @param in		   Input stream to read from.
	 * @param littleEndian Whether to read little endian NBT.
	 * @return The read tag, or null if the tag is an end tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTTag readTag(InputStream in, boolean littleEndian) throws IOException {
		return readTag((DataInput)(littleEndian ? new LittleEndianDataInputStream(in) : new DataInputStream(in)));
	}

	/**
	 * Reads an NBT tag.
	 *
	 * @param in Data input to read from.
	 * @return The read tag, or null if the tag is an end tag.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static NBTTag readTag(DataInput in) throws IOException {
		int id = in.readUnsignedByte();
		if(id == 0) {
			return null;
		}

		String name = in.readUTF();
		NBTTag tag;

		try {
			tag = NBTRegistry.createInstance(id, name);
		} catch(Exception e) {
			throw new IOException("Failed to create tag.", e);
		}

		tag.read(in);
		return tag;
	}

	/**
	 * Writes an NBT tag in big endian.
	 *
	 * @param out Output stream to write to.
	 * @param tag Tag to write.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeTag(OutputStream out, NBTTag tag) throws IOException {
		writeTag(out, tag, false);
	}

	/**
	 * Writes an NBT tag.
	 *
	 * @param out		  Output stream to write to.
	 * @param tag		  Tag to write.
	 * @param littleEndian Whether to write little endian NBT.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeTag(OutputStream out, NBTTag tag, boolean littleEndian) throws IOException {
		writeTag((DataOutput) (littleEndian ? new LittleEndianDataOutputStream(out) : new DataOutputStream(out)), tag);
	}

	/**
	 * Writes an NBT tag.
	 *
	 * @param out Data output to write to.
	 * @param tag Tag to write.
	 * @throws java.io.IOException If an I/O error occurs.
	 */
	public static void writeTag(DataOutput out, NBTTag tag) throws IOException {
		out.writeByte(NBTRegistry.idForClass(tag.getClass()));
		out.writeUTF(tag.getName());
		tag.write(out);
	}

	public static class LittleEndianDataInputStream extends FilterInputStream implements DataInput {
		public LittleEndianDataInputStream(InputStream in) {
			super(in);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return this.in.read(b, 0, b.length);

		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return this.in.read(b, off, len);
		}

		@Override
		public void readFully(byte[] b) throws IOException {
			this.readFully(b, 0, b.length);
		}

		@Override
		public void readFully(byte[] b, int off, int len) throws IOException {
			if(len < 0) {
				throw new IndexOutOfBoundsException();
			} else {
				int read;
				for(int pos = 0; pos < len; pos += read) {
					read = this.in.read(b, off + pos, len - pos);
					if(read < 0) {
						throw new EOFException();
					}
				}
			}
		}

		@Override
		public int skipBytes(int n) throws IOException {
			int total = 0;
			int skipped = 0;
			while(total < n && (skipped = (int) this.in.skip(n - total)) > 0) {
				total += skipped;
			}

			return total;
		}

		@Override
		public boolean readBoolean() throws IOException {
			int val = this.in.read();
			if(val < 0) {
				throw new EOFException();
			}

			return val != 0;
		}

		@Override
		public byte readByte() throws IOException {
			int val = this.in.read();
			if(val < 0) {
				throw new EOFException();
			}

			return (byte) val;
		}

		@Override
		public int readUnsignedByte() throws IOException {
			int val = this.in.read();
			if(val < 0) {
				throw new EOFException();
			}

			return val;
		}

		@Override
		public short readShort() throws IOException {
			int b1 = this.in.read();
			int b2 = this.in.read();
			if((b1 | b2) < 0) {
				throw new EOFException();
			}

			return (short) (b1 | (b2 << 8));
		}

		@Override
		public int readUnsignedShort() throws IOException {
			int b1 = this.in.read();
			int b2 = this.in.read();
			if((b1 | b2) < 0) {
				throw new EOFException();
			}

			return b1 | (b2 << 8);
		}

		@Override
		public char readChar() throws IOException {
			int b1 = this.in.read();
			int b2 = this.in.read();
			if((b1 | b2) < 0) {
				throw new EOFException();
			}

			return (char) (b1 | (b2 << 8));
		}

		@Override
		public int readInt() throws IOException {
			int b1 = this.in.read();
			int b2 = this.in.read();
			int b3 = this.in.read();
			int b4 = this.in.read();
			if((b1 | b2 | b3 | b4) < 0) {
				throw new EOFException();
			}

			return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
		}

		@Override
		public long readLong() throws IOException {
			long b1 = this.in.read();
			long b2 = this.in.read();
			long b3 = this.in.read();
			long b4 = this.in.read();
			long b5 = this.in.read();
			long b6 = this.in.read();
			long b7 = this.in.read();
			long b8 = this.in.read();
			if((b1 | b2 | b3 | b4 | b5 | b6 | b7 | b8) < 0) {
				throw new EOFException();
			}

			return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24) | (b5 << 32) | (b6 << 40) | (b7 << 48) | (b8 << 56);
		}

		@Override
		public float readFloat() throws IOException {
			return Float.intBitsToFloat(this.readInt());
		}

		@Override
		public double readDouble() throws IOException {
			return Double.longBitsToDouble(this.readLong());
		}

		@Override
		public String readLine() throws IOException {
			throw new UnsupportedOperationException("Use readUTF.");
		}

		@Override
		public String readUTF() throws IOException {
			byte[] bytes = new byte[this.readUnsignedShort()];
			this.readFully(bytes);

			return new String(bytes, "UTF-8");
		}
	}

	public static class LittleEndianDataOutputStream extends FilterOutputStream implements DataOutput {
		public LittleEndianDataOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public synchronized void write(int b) throws IOException {
			this.out.write(b);
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			this.out.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			this.out.flush();
		}

		@Override
		public void writeBoolean(boolean b) throws IOException {
			this.out.write(b ? 1 : 0);
		}

		@Override
		public void writeByte(int b) throws IOException {
			this.out.write(b);
		}

		@Override
		public void writeShort(int s) throws IOException {
			this.out.write(s & 0xFF);
			this.out.write((s >>> 8) & 0xFF);
		}

		@Override
		public void writeChar(int c) throws IOException {
			this.out.write(c & 0xFF);
			this.out.write((c >>> 8) & 0xFF);
		}

		@Override
		public void writeInt(int i) throws IOException {
			this.out.write(i & 0xFF);
			this.out.write((i >>> 8) & 0xFF);
			this.out.write((i >>> 16) & 0xFF);
			this.out.write((i >>> 24) & 0xFF);
		}

		@Override
		public void writeLong(long l) throws IOException {
			this.out.write((int) (l & 0xFF));
			this.out.write((int) ((l >>> 8) & 0xFF));
			this.out.write((int) ((l >>> 16) & 0xFF));
			this.out.write((int) ((l >>> 24) & 0xFF));
			this.out.write((int) ((l >>> 32) & 0xFF));
			this.out.write((int) ((l >>> 40) & 0xFF));
			this.out.write((int) ((l >>> 48) & 0xFF));
			this.out.write((int) ((l >>> 56) & 0xFF));
		}

		@Override
		public void writeFloat(float f) throws IOException {
			this.writeInt(Float.floatToIntBits(f));
		}

		@Override
		public void writeDouble(double d) throws IOException {
			this.writeLong(Double.doubleToLongBits(d));
		}

		@Override
		public void writeBytes(String s) throws IOException {
			int len = s.length();
			for(int index = 0; index < len; index++) {
				this.out.write((byte) s.charAt(index));
			}
		}

		@Override
		public void writeChars(String s) throws IOException {
			int len = s.length();
			for(int index = 0; index < len; index++) {
				char c = s.charAt(index);
				this.out.write(c & 0xFF);
				this.out.write((c >>> 8) & 0xFF);
			}
		}

		@Override
		public void writeUTF(String s) throws IOException {
			byte[] bytes = s.getBytes("UTF-8");

			this.writeShort(bytes.length);
			this.write(bytes);
		}
	}
	
}
