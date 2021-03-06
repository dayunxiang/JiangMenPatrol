

package com.ecity.android.httpforandroid.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
/***
 * 新增加了POST支持
 * @author ZiZhengzhuan
 * @version 2.0
 */
public class HttpRequestJsonParse extends HttpRequest {
	private static final long serialVersionUID = 1L;

	static final JsonFactory jsonFactory = new JsonFactory();

	public static final String executeFromMapToString(String url,
			Map<String, String> paramters) throws Exception
	{
		return executeFromMapToString(url,paramters,HttpRequestType.AUTO);
	}
	
	public static final String executeFromMapToString(String url,
			Map<String, String> paramters,HttpRequestType type) throws Exception {
		return executeFromMapToStringNew(url,paramters,type);
	}
	/***
	 * 
	 * @param url
	 * @param paramters
	 * @return
	 * @throws Exception
	 * @version 2.0
	 */
	public static final String executeFromMapToStringNew(String url,
			Map<String, String> paramters) throws Exception 
	{
		return executeFromMapToStringNew(url, paramters,HttpRequestType.AUTO);
	}
	/***
	 * 
	 * @param url
	 * @param paramters
	 * @param type
	 * @return
	 * @throws Exception
	 * @version 2.0
	 */
	public static final String executeFromMapToStringNew(String url,
			Map<String, String> paramters,HttpRequestType type) throws Exception {
		String str = paramters != null ? paramters.get("$ReturnResultString$")
				: null;
		String errorMsg = "";
		if (str == null) {
			ArrayList<NameValuePair> nameValuePairArray = new ArrayList<NameValuePair>();
			HttpRequestBase httpRequest = null;
			boolean bool = HttpRequest.mapToList(url, addFJson(paramters),nameValuePairArray);
			
			if(Config.SPECIAL_CODE){
				String completeURL =  HttpRequest.getHttpRequestCompleteURL(url, nameValuePairArray);
				URL url1 = new URL(completeURL);
				HttpURLConnection connect = null;
				connect = (HttpURLConnection) url1.openConnection();
				connect.setConnectTimeout( Config.httpConnectionTimeout);
				connect.setReadTimeout(Config.httpSoTimeout);

	            boolean isPOST = false;
	            switch(type){
	                case GET:
	                    isPOST = false;
	                    break;
	                case POST:
	                    isPOST = true;
	                    break;
	                 default:
	                     if(bool){
	                         isPOST = false;
	                     }else{
	                         isPOST = true;
	                     }
	                     break;
	            }
	            connect.setConnectTimeout(Config.httpConnectionTimeout);
				if(!isPOST){
				    connect.setRequestMethod("GET");
				}else{
				    connect.setRequestMethod("POST");
				    connect.setDoOutput(true);
				    connect.setRequestProperty("Content-Type", "application/x-javascript; charset="+ encoding);
				}
				
				if (connect.getResponseCode() != 200) {
					throw new IOException(connect.getResponseMessage());
				}
				
				BufferedReader brd = null;
				brd = new BufferedReader(new InputStreamReader(
						connect.getInputStream(), "utf-8"));

				StringBuilder sb = new StringBuilder();
				String line;

				while ((line = brd.readLine()) != null) {
					sb.append(line);
				}
				brd.close();
				connect.disconnect();
				str = new String(sb.toString());
			} else {
                switch (type) {
                    case GET:
                        httpRequest = HttpRequest.getHttpGet(url, nameValuePairArray);
                        break;
                    case POST:
                        httpRequest = HttpRequest.getHttpPost(url, nameValuePairArray);
                        break;

                    default:
                        httpRequest = bool ? HttpRequest.getHttpGet(url, nameValuePairArray) : HttpRequest.getHttpPost(url, nameValuePairArray);
                        break;
                }

                HttpParams params = null;

                params = httpClient.getParams();
                HttpConnectionParams.setConnectionTimeout(params, Config.httpConnectionTimeout);
                HttpConnectionParams.setSoTimeout(params, Config.httpSoTimeout);
                httpClient.setParams(params);

				str = httpClient.execute(httpRequest, new BasicResponseHandler());
				String ec = TranCharsetUtil.getEncoding(str);
				str = new String(str.getBytes(ec), "UTF-8");
			}
			
			if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1
					&& (str.charAt(1) == '[' || str.charAt(1) == '{')) {
				str = str.substring(1, str.length() - 1);
			}
			if (errorMsg.contains("failed to connect to"))
				throw new Exception(errorMsg);

		}
		return str;
	}
	/***
	 * 
	 * @param url
	 * @param paramters
	 * @return
	 * @throws Exception
	 * @version 2.0
	 */
	public static final JsonParser executeFromMap(String url,
			Map<String, String> paramters) throws Exception 
	{
		return executeFromMap(url,paramters,HttpRequestType.AUTO);
	}
	/***
	 * 
	 * @param url
	 * @param paramters 
	 * @param type 
	 * @return
	 * @throws Exception
	 */
	public static final JsonParser executeFromMap(String url,
			Map<String, String> paramters,HttpRequestType type) throws Exception {
		String str = paramters != null ? paramters.get("$ReturnResultString$")
				: null;

		if (str == null) {
			HttpRequestBase httpRequest = null;
			ArrayList<NameValuePair> nameValuePairArray = new ArrayList<NameValuePair>();
			
			boolean bool = HttpRequest.mapToList(url, addFJson(paramters),nameValuePairArray);
			switch (type) {
			case GET:
				httpRequest = HttpRequest.getHttpGet(url, nameValuePairArray);
				break;
			case POST:
				httpRequest = HttpRequest.getHttpPost(url, nameValuePairArray);
				break;
				
			default:
				httpRequest = bool ? HttpRequest
						.getHttpGet(url, nameValuePairArray) : HttpRequest
						.getHttpPost(url, nameValuePairArray);
				break;
			}
			
			str = httpClient.execute(httpRequest, new BasicResponseHandler());
			if (str.startsWith("\"") && str.endsWith("\"") && str.length() > 1
					&& (str.charAt(1) == '[' || str.charAt(1) == '{')) {
				str = str.substring(1, str.length() - 1);
			}
			//str = str.replace("\\", "");
		}
		JsonParser jsonParser = jsonFactory.createJsonParser(str);
		jsonParser.nextToken();
		return jsonParser;
	}
	/***
	 * 
	 * @param url
	 * @param file
	 * @param fileHeader
	 * @return
	 * @throws Exception
	 */
	public static final JsonParser fromFile(String url, File file,
			String fileHeader) throws Exception {
		byte[] arrayOfByte1 = "---------------------------".getBytes();
		byte[] currentTimeByteArray = Long.toString(System.currentTimeMillis())
				.getBytes();
		HttpURLConnection httpURLConnection = null;
		try {
			httpURLConnection = (HttpURLConnection) new URL(url)
					.openConnection();

			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + new String(arrayOfByte1)
							+ new String(currentTimeByteArray));

			ByteArrayOutputStream optStream = new ByteArrayOutputStream();
			optStream.write(45);
			optStream.write(45);
			optStream.write(arrayOfByte1);
			optStream.write(currentTimeByteArray);
			optStream.write(13);
			optStream.write(10);

			optStream
					.write("Content-Disposition: form-data; name=\"attachment\"; filename=\""
							.getBytes());
			optStream.write(file.getName().getBytes());
			optStream.write("\"\r\n".getBytes());
			optStream.write("Content-Type: ".getBytes());
			optStream
					.write(fileHeader == null ? "Content-Type: application/octet-stream"
							.getBytes() : fileHeader.getBytes());
			optStream.write(13);
			optStream.write(10);

			optStream.write(13);
			optStream.write(10);
			optStream.write(fileToByte(file));
			optStream.write(13);
			optStream.write(10);

			optStream.write(45);
			optStream.write(45);
			optStream.write(arrayOfByte1);
			optStream.write(currentTimeByteArray);
			optStream.write(13);
			optStream.write(10);

			optStream
					.write("Content-Disposition: form-data; name=\"f\"\r\n"
							.getBytes());
			optStream.write("\r\njson\r\n".getBytes());

			optStream.write(45);
			optStream.write(45);
			optStream.write(arrayOfByte1);
			optStream.write(currentTimeByteArray);
			optStream.write(45);
			optStream.write(45);

			optStream.flush();
			optStream.close();
			
			byte[] arrayOfByte3 = optStream.toByteArray();
			httpURLConnection.setFixedLengthStreamingMode(arrayOfByte3.length);
			OutputStream localOutputStream = httpURLConnection
					.getOutputStream();
			localOutputStream.write(arrayOfByte3);
			localOutputStream.flush();
			localOutputStream.close();

			int i = httpURLConnection.getResponseCode();
			if (i != 200) {
				throw new Exception("Received the response code " + i
						+ " from the URL " + url);
			}

			InputStream inputStream = httpURLConnection.getInputStream();

			JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
			jsonParser.nextToken();
			return jsonParser;
		} finally {
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
		}
	}
	/***
	 * 将文件转换为BYTE数组
	 * @param file
	 * @return 
	 * @throws Exception
	 */
	private static final byte[] fileToByte(File file) throws Exception {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

			byte[] arrayOfByte1 = new byte[1024];
			int i;
			while ((i = fileInputStream.read(arrayOfByte1)) != -1) {
				byteArrayOutputStream.write(arrayOfByte1, 0, i);
			}
			byteArrayOutputStream.close();
			byte[] fileByte = byteArrayOutputStream.toByteArray();
			return fileByte;
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}
	/***
	 * 自动进行POST GET 切换
	 * @param url 请求地址
	 * @param paramters 请求参数
	 * @param isCheckError 是否进行参数错误检查(目前没有实际用途)
	 * @version 2.0
	 * @return
	 * @throws Exception
	 */
	public static final JsonParser executeFromMap(String url,
			Map<String, String> paramters, boolean isCheckError)
			throws Exception {
		return executeFromMap(url,paramters,isCheckError,HttpRequestType.AUTO);
	}
	/***
	 * 
	 * @param url 请求地址
	 * @param paramters 请求参数
	 * @param isCheckError 是否进行参数错误检查(目前没有实际用途)
	 * @param type GET POST AUTO
	 * @version 2.0
	 * @return
	 * @throws Exception
	 */
	public static final JsonParser executeFromMap(String url,
			Map<String, String> paramters, boolean isCheckError,HttpRequestType type)
			throws Exception {
		
		ArrayList<NameValuePair> nameValuePairArray = new ArrayList<NameValuePair>();
		HttpRequestBase httpRequest = null;
		boolean bool = HttpRequest.mapToList(url, addFJson(paramters),nameValuePairArray);
		switch (type) {
		case GET:
			httpRequest = HttpRequest.getHttpGet(url, nameValuePairArray);
			break;
		case POST:
			httpRequest = HttpRequest.getHttpPost(url, nameValuePairArray);
			break;
			
		default:
			httpRequest = bool ? HttpRequest
					.getHttpGet(url, nameValuePairArray) : HttpRequest
					.getHttpPost(url, nameValuePairArray);
			break;
		}
		String str = httpClient.execute(httpRequest, new BasicResponseHandler());
		JsonParser localJsonParser = jsonFactory.createJsonParser(str);
		localJsonParser.nextToken();
		return localJsonParser;
	}

	public static JsonFactory getJsonFactory() {
		return jsonFactory;
	}

	private static final Map<String, String> addFJson(
			Map<String, String> paramMap) {
		if (paramMap == null) {
			paramMap = new HashMap<String, String>();
		}
		if (!paramMap.containsKey("f")) {
			paramMap.put("f", "json");
		}
		return paramMap;
	}
}
