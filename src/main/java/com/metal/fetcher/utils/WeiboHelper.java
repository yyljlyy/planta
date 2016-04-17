package com.metal.fetcher.utils;

public class WeiboHelper {  
	
	public static String mid2Id(String mid) {
		if(mid.length() != 16) {
			return null;
		}
		try {
			Long.parseLong(mid);
		} catch (NumberFormatException e) {
			return null;
		}
		StringBuilder sb = new StringBuilder(10);
		sb.append(Base62.encode(Long.parseLong(mid.substring(0, 2))));
		sb.append(Base62.encode(Long.parseLong(mid.substring(2, 9))));
		sb.append(Base62.encode(Long.parseLong(mid.substring(9, 16))));
		return sb.toString();
	}
	
	public static void main(String[] args) {
		System.out.println(mid2Id("3895087044141925"));
	}
}  
