package com.metal.fetcher.model;

public class WeiboJSBean {
	String pid;
	String[] js;
	String[] css;
	String html;

	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String[] getJs() {
		return js;
	}
	public void setJs(String[] js) {
		this.js = js;
	}
	public String[] getCss() {
		return css;
	}
	public void setCss(String[] css) {
		this.css = css;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
}
