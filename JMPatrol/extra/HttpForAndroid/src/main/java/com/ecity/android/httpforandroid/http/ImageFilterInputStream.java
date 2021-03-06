package com.ecity.android.httpforandroid.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFilterInputStream extends FilterInputStream {

	private final ByteParse.a a;
	private static byte[] b = new byte[0];
	@SuppressWarnings("unused")
	private static final int c = 2048;
	private boolean d;
	private byte[] e;
	private int f;
	private int g;

	public ImageFilterInputStream(InputStream paramInputStream, int paramInt) {
		this(paramInputStream, paramInt, false);
	}

	public ImageFilterInputStream(InputStream paramInputStream, int paramInt,
			boolean paramBoolean) {
		super(paramInputStream);
		this.d = false;
		this.e = new byte[2048];
		if (paramBoolean)
			this.a = new ByteParse.c(paramInt, null);
		else {
			this.a = new ByteParse.b(paramInt, null);
		}
		this.a.a = new byte[this.a.fa(2048)];
		this.f = 0;
		this.g = 0;
	}

	public boolean markSupported() {
		return false;
	}

	public void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	public void reset() {
		throw new UnsupportedOperationException();
	}

	public void close() throws IOException {
		this.in.close();
		this.e = null;
	}

	public int available() {
		return this.g - this.f;
	}

	public long skip(long n) throws IOException {
		if (this.f >= this.g) {
			a();
		}
		if (this.f >= this.g) {
			return 0L;
		}
		long l = Math.min(n, this.g - this.f);
		this.f = (int) (this.f + l);
		return l;
	}

	public int read() throws IOException {
		if (this.f >= this.g) {
			a();
		}
		if (this.f >= this.g) {
			return -1;
		}
		return this.a.a[(this.f++)];
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (this.f >= this.g) {
			a();
		}
		if (this.f >= this.g) {
			return -1;
		}
		int i = Math.min(len, this.g - this.f);
		System.arraycopy(this.a.a, this.f, b, off, i);
		this.f += i;
		return i;
	}

	private void a() throws IOException {
		if (this.d)
			return;
		int i = this.in.read(this.e);
		boolean bool;
		if (i == -1) {
			this.d = true;
			bool = this.a.fa(b, 0, 0, true);
		} else {
			bool = this.a.fa(this.e, 0, i, false);
		}
		if (!bool) {
			throw new IOException("bad base-64");
		}
		this.g = this.a.b;
		this.f = 0;
	}
}
