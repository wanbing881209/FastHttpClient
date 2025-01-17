package io.itit.itf.okhttp;

import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.itit.itf.okhttp.PostRequest.FileInfo;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 
 * @author icecooly
 *
 */
public class PutRequest extends OkHttpRequest {
	//
	public static Logger logger = LoggerFactory.getLogger(PutRequest.class);
	//
	public PutRequest(String url,
			Object tag,
			Map<String, String> params, 
			Map<String, String> headers,
			List<FileInfo> fileInfos,
			String body,
			MultipartBody multipartBody,int id) {
		super(url, tag,params,headers, fileInfos,body,multipartBody,id);
	}

	@Override
	protected RequestBody buildRequestBody() {
		if(multipartBody!=null) {
			return multipartBody;
		}else if(fileInfos!=null && fileInfos.size()>0) {
			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			addParams(builder);
//			fileInfos.forEach(fileInfo -> {
//				RequestBody fileBody=null;
//				if(fileInfo.file!=null) {
//					fileBody = RequestBody.create(MediaType.parse("application/octet-stream"),fileInfo.file);
//				}else if(fileInfo.fileInputStream!=null) {
//					fileBody = createRequestBody(MediaType.parse("application/octet-stream"),fileInfo.fileInputStream);
//				}else {
//					fileBody = RequestBody.create(MediaType.parse(getMimeType(fileInfo.fileName)),
//						fileInfo.fileContent);
//				}
//				builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);
//			});

			for(FileInfo fileInfo:fileInfos) {
				RequestBody fileBody=null;
				if(fileInfo.file!=null) {
					fileBody = RequestBody.create(MediaType.parse("application/octet-stream"),fileInfo.file);
				}else if(fileInfo.fileInputStream!=null) {
					fileBody = createRequestBody(MediaType.parse("application/octet-stream"),fileInfo.fileInputStream);
				}else {
					fileBody = RequestBody.create(MediaType.parse(getMimeType(fileInfo.fileName)),
						fileInfo.fileContent);
				}
				builder.addFormDataPart(fileInfo.partName, fileInfo.fileName, fileBody);

			}

			if(body!=null&&body.length()>0){
				builder.addPart(RequestBody.create(MultipartBody.FORM,body));
			}
			return builder.build();
		}else if(body!=null&&body.length()>0){
			MediaType mediaType=null;
			if(headers.containsKey("Content-Type")){
				mediaType = MediaType.parse(headers.get("Content-Type"));
			}else{
				mediaType = MediaType.parse("text/plain;charset=utf-8");
			}
			return RequestBody.create(mediaType,body);
		}else{
			FormBody.Builder builder = new FormBody.Builder();
			addParams(builder);
			FormBody formBody = builder.build();
			return formBody;
		}
	}

	@Override
	protected Request buildRequest(RequestBody requestBody) {
		return builder.put(requestBody).build();
	}

	private void addParams(FormBody.Builder builder) {
		if (params!= null) {
			//params.forEach((k,v)->builder.add(k,v));
			for(String k:params.keySet()){
				String v = params.get(k);
				builder.add(k,v);
			}
		}
		if (encodedParams!= null) {
			//encodedParams.forEach((k,v)->builder.addEncoded(k,v));
			for(String k:encodedParams.keySet()){
				String v = encodedParams.get(k);
				builder.addEncoded(k,v);
			}
		}
	}
	//
	private void addParams(MultipartBody.Builder builder) {
		if (params != null && !params.isEmpty()) {
//			params.forEach((k,v)->{
//				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + k + "\""),
//						RequestBody.create(null,v));
//			});
			for(String k:params.keySet()){
				String v = params.get(k);
				builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + k + "\""),
						RequestBody.create(null,v));
			}
		}
	}
	//
	public static String getMimeType(String path) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = null;
		try {
			contentTypeFor = fileNameMap.getContentTypeFor(URLEncoder.encode(path, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(),e);
		}
		if (contentTypeFor == null) {
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}
}
