package com.metal.fetcher.model;

public class WeiboAccount {
	private String account;
	private String pwd;
	private String cookie;
	
	public WeiboAccount(String account, String pwd, String cookie) {
		super();
		this.account = account;
		this.pwd = pwd;
		this.cookie = cookie;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	@Override
	public String toString() {
		return account + "\t" + pwd + "\t" + cookie;
	}
}
