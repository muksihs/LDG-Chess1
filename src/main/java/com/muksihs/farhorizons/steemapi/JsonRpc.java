package com.muksihs.farhorizons.steemapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonRpc<T> {
	private String jsonrpc;
	private T result;
	public String getJsonrpc() {
		return jsonrpc;
	}
	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}
	public T getResult() {
		return result;
	}
	public void setResult(T result) {
		this.result = result;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	private long id;
}