package com.muksihs.farhorizons.steemapi;

public class JsonRpcCallMethod<T> {
	private long id=System.currentTimeMillis();
	private String jsonrpc="2.0";
	private String method="call";
	private T params;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getJsonrpc() {
		return jsonrpc;
	}
	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}
	public T getParams() {
		return params;
	}
	public void setParams(T params) {
		this.params = params;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
}