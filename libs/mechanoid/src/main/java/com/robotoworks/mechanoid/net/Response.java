/*
 * Copyright 2013 Robotoworks Limited
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.robotoworks.mechanoid.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import com.robotoworks.mechanoid.util.Streams;

/**
 * <p>A Mechanoid Net response that captures the HTTP response code and headers of a HTTP response.</p>
 * <p>The response defers parsing from the stream until {@link #parse()} is called, this allows examination
 * of the response code before reading the stream in case of an unexpected response code.</p>
 * 
 * @param <T> A result representing that which will be parsed when {@link #parse()} is called.
 */
public class Response<T> {
	public static final int HTTP_INVALID = -1;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_BAD_METHOD = 405;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_CLIENT_TIMEOUT = 408;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_ENTITY_TOO_LARGE = 413;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_INTERNAL_ERROR = 500;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_MOVED_PERM = 301;
    public static final int HTTP_MOVED_TEMP = 302;
    public static final int HTTP_MULT_CHOICE = 300;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_OK = 200;
    public static final int HTTP_PARTIAL = 206;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_PRECON_FAILED = 412;
    public static final int HTTP_PROXY_AUTH = 407;
    public static final int HTTP_REQ_TOO_LONG = 414;
    public static final int HTTP_RESET = 205;
    public static final int HTTP_SEE_OTHER = 303;
    public static final int HTTP_USE_PROXY = 305;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_UNSUPPORTED_TYPE = 415;
    public static final int HTTP_UNAVAILABLE = 503;
    public static final int HTTP_VERSION = 505;
    
	private T mContent;
	private HttpURLConnection mConn;
	private Parser<T> mParser;
	private int mResponseCode;
	private Map<String, List<String>> mHeaders;
	
	private byte[] mInputBytes;
	
	private boolean mParsed;

	/**
	 * @return The HTTP Response Code, i.e.:- 200
	 */
	public int getResponseCode() {
		return mResponseCode;
	}
	
	/**
	 * <p>Checks to see if the response code is HTTP_OK and if not, throws
	 * a {@link UnexpectedHttpStatusException}.</p>
	 * 
	 * <p>In some circumstances it is useful to call this to enforce a post condition
	 * on the response code to ensure its HTTP_OK before continuing</p>
	 */
	public void checkResponseCodeOk() throws UnexpectedHttpStatusException {
		if(mResponseCode != HTTP_OK) {
			throw new UnexpectedHttpStatusException(mResponseCode, HTTP_OK);
		}
	}
	
	/**
	 * <p>Checks to see if the response code is the given response code and if not, throws
	 * a {@link UnexpectedHttpStatusException}.</p>
	 * 
	 * <p>In some circumstances it is useful to call this to enforce a post condition
	 * on the response code to ensure its of a certain code before continuing</p>
	 */
	public void checkResponseCode(int responseCode) throws UnexpectedHttpStatusException {
		if(mResponseCode != responseCode) {
			throw new UnexpectedHttpStatusException(mResponseCode, HTTP_OK);
		}
	}

	/**
	 * @return A Map of header fields
	 */
	public Map<String, List<String>> getHeaders() {
		return mHeaders;
	}

	public Response(HttpURLConnection conn, Parser<T> parser) throws ServiceException {
		mConn = conn;
		mParser = parser;
		try {
		mResponseCode = conn.getResponseCode();
		mHeaders = conn.getHeaderFields();
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Parses the response stream into <T>
	 * @return The parsed response <T>
	 * @throws ServiceException
	 */
	public T parse() throws ServiceException {
		if(mParsed) {
			return mContent;
		}
		
		try {
			InputStream stream = null;
			if(mInputBytes == null) {
				stream = getInputStream();
			} else {
				if(mInputBytes.length > 0) {
					stream = new ByteArrayInputStream(mInputBytes);
				}
			}
			
			if(stream != null) {
				mContent = mParser.parse(stream);
			}
			
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		
		mParsed = true;
		
		return mContent;
	}

	private InputStream getInputStream() throws IOException {
		// error stream is available only if there was a connection with the 
		// server, the server returned an error and also returned some usefull 
		// data. Example being status 404 with page to search for content.
		InputStream stream = mConn.getErrorStream();
		if (stream != null)
			return stream;
		return mConn.getInputStream();
	}
	
	/**
	 * Reads the stream as text, the underlying stream will first be copied
	 * into a byte buffer before being returned as a string. this will make sure
	 * that calls to parse() will still succeed, this is useful for debugging
	 * purposes but should be avoided if the intention is just to parse
	 * 
	 * @return
	 * @throws IOException
	 */
	public String readAsText() throws IOException {
		if(mInputBytes == null) {
			InputStream stream = getInputStream();
			if(stream == null) {
				mInputBytes = new byte[]{};
			} else {
				mInputBytes = Streams.readAllBytes(getInputStream());
			}
		}
		
		return Streams.readAllText(new ByteArrayInputStream(mInputBytes));
	}
}
