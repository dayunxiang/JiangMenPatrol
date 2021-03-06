package com.ecity.android.httpforandroid.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
/***
 * 
 * @Description
 * @version V1.0 
 * @Author ZiZhengzhuan
 * @CreateDate 2016年1月15日
 * @email
 */
public class CustomMultipartEntity extends MultipartEntity {
	
	private final ProgressListener listener;
	/**
	 * 
	 * @param listener
	 */
	public CustomMultipartEntity(final ProgressListener listener) {
		super();
		this.listener = listener;
	}
	/***
	 * 
	 * @param mode
	 * @param listener
	 */
	public CustomMultipartEntity(final HttpMultipartMode mode,
			final ProgressListener listener) {
		super(mode);
		this.listener = listener;
	}
	/***
	 * 
	 * @param mode
	 * @param boundary
	 * @param charset
	 * @param listener
	 */
	public CustomMultipartEntity(HttpMultipartMode mode, final String boundary,
			final Charset charset, final ProgressListener listener) {
		super(mode, boundary, charset);
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener));
	}
	/***
	 * 
	 * @Description
	 * @version V1.0 
	 * @Author ZiZhengzhuan
	 * @CreateDate 2016年1月15日
	 * @email
	 */
	public static interface ProgressListener {
		void transferred(long num);
	}
	/***
	 * 
	 * @Description
	 * @version V1.0 
	 * @Author ZiZhengzhuan
	 * @CreateDate 2016年1月15日
	 * @email
	 */
	public static class CountingOutputStream extends FilterOutputStream {
		
		private final ProgressListener listener;
		private long transferred;

		public CountingOutputStream(final OutputStream out,
				final ProgressListener listener) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			this.transferred += len;
			
			if(null != this.listener)
				this.listener.transferred(this.transferred);
		}

		public void write(int b) throws IOException {
			out.write(b);
			this.transferred++;
			
			if(null != this.listener)
				this.listener.transferred(this.transferred);
		}
	}

}
