package com.viatom.azur.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by wangjiang on 2017/10/16.
 */

public class JsonRequest extends Request<JSONObject> {
    private Response.Listener<JSONObject> listener;
    private Map<String, String> headers;
    private String jsonRequest;

    public JsonRequest(String url, Map<String, String> headers, String jsonRequest,
                       Response.Listener<JSONObject> responseListener,
                       Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = responseListener;
        this.headers = headers;
        this.jsonRequest = jsonRequest;
    }

    public JsonRequest(int method, String url, Map<String, String> headers,
                       String jsonRequest, Response.Listener<JSONObject> responseListener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = responseListener;
        this.headers = headers;
        this.jsonRequest = jsonRequest;
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    @Override
    public byte[] getBody() throws AuthFailureError{
        return jsonRequest == null ? null : jsonRequest.getBytes();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        listener.onResponse(response);
    }
}
