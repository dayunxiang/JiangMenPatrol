package com.ecity.android.httpforandroid.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class HttpRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String encoding = "UTF-8";
	public static final String userAgent = "ZiZhengzhuan";
	//public static final int stringLen = 8192;
	
	protected static final DefaultHttpClient httpClient;

	/***
	 * 获得GET 对象
	 * @param url
	 * @param paramters 参数，仅支持字符串类型的数据
	 * @return
	 */
	public static HttpGet getHttpGet(String url, List<NameValuePair> paramters) {
		String str = url
				+ (paramters.isEmpty() ? "" : new StringBuilder().append("?").append(URLEncodedUtils.format(paramters, "UTF-8")).toString());
		return new HttpGet(str);
	}
	/***
	 * 获得完整的请求路径
	 * @param url
	 * @param paramters 参数，仅支持字符串类型的数据
	 * @return
	 */
	public static String getHttpRequestCompleteURL(String url, List<NameValuePair> paramters) {
		String str = url
				+ (paramters.isEmpty() ? "" : new StringBuilder().append("?").append(URLEncodedUtils.format(paramters, "UTF-8")).toString());
		return str;
	}
	/***
	 * 获得POST对
	 * @param url
	 * @param paramters 参数，仅支持字符串类型的数据
	 * @return
	 * @throws Exception
	 */
	public static HttpPost getHttpPost(String url, List<NameValuePair> paramters) throws Exception {
		HttpPost httpRequest = new HttpPost(url);
		UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(paramters, "UTF-8");
		reqEntity.setContentType("application/x-www-form-urlencoded");
		httpRequest.setEntity(reqEntity);
		return httpRequest;
	}
	
	/***
	 * 
	 * @param url
	 * @param httpEntity 获得POST 对象
	 * @return
	 * @throws Exception
	 */
	public static HttpPost getHttpPost(String url, HttpEntity httpEntity) throws Exception {
		HttpPost httpRequest = new HttpPost(url);
		httpRequest.setEntity(httpEntity);
		return httpRequest;
	}
	
	
	/***
	 * 参数列表Map转为List <br/>
	 * 在之前版本中，如果字符串长度小于2000 使用GET 否则使用POST
	 * @param url
	 * @param paramters 参数，仅支持字符串类型的数据
	 * @param nameValuePairList
	 * @return
	 */
	public static final boolean mapToList(String url, Map<String, String> paramters, List<NameValuePair> nameValuePairList) {
		if ((paramters == null) || (paramters.isEmpty())) {
			return true;
		}
		int length = 0;
		for (Map.Entry<String, String> entry : paramters.entrySet()) {
			String str1 = entry.getKey();
			String str2 = entry.getValue();
			if (!isStringNullOrEmpty(str1)) {
				if (str2 == null) {
					str2 = "";
				}
				length += str1.length() + str2.length() + 2;
				nameValuePairList.add(new BasicNameValuePair(str1, str2));
			}
		}
		return length <= 2000;
	}
	/***
	 * 字符串是否为空
	 * @param string
	 * @return
	 */
	private static final boolean isStringNullOrEmpty(String string) {
		return (string == null) || (string.length() < 1);
	}

	static {
		BasicHttpParams basicHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(basicHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(basicHttpParams, "UTF-8");
		HttpConnectionParams.setStaleCheckingEnabled(basicHttpParams, false);
		
		HttpConnectionParams.setConnectionTimeout(basicHttpParams,Config.httpConnectionTimeout);
		HttpConnectionParams.setSoTimeout(basicHttpParams, Config.httpSoTimeout);
		
		HttpConnectionParams.setSocketBufferSize(basicHttpParams, 8192);

		HttpClientParams.setRedirecting(basicHttpParams, true);
		HttpProtocolParams.setUserAgent(basicHttpParams, userAgent);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(basicHttpParams, schemeRegistry);
		httpClient = new DefaultHttpClient(threadSafeClientConnManager, basicHttpParams);
		
		HttpParams params = null;  
		params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params,Config.httpConnectionTimeout);  
		HttpConnectionParams.setSoTimeout(params, Config.httpSoTimeout);
		
		httpClient.addRequestInterceptor(new HttpRequestInterceptorAddHeader());
		httpClient.addResponseInterceptor(new HttpRequestInterceptorSetEntity());
		
	}
	/***
	 * 设置代理
	 * @param proxy
	 */
	public static void setProxy(String proxy) {
		httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
				new HttpHost(proxy.split(":")[0], Integer.valueOf(proxy.split(":")[1])));
	}

	static class httpEntityWrapperClient extends HttpEntityWrapper {
		public httpEntityWrapperClient(HttpEntity httpEntity) {
			super(httpEntity);
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException {
			InputStream inputStream = this.wrappedEntity.getContent();
			return new GZIPInputStream(inputStream);
		}

		@Override
		public long getContentLength() {
			return -1L;
		}
	}
}
