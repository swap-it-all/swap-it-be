package com.example.swapit.util;

public class Constant {
	public static final long ACCESS_TOKEN_VALID_TIME = 1000 * 60 * 30; // 30분
	public static final long REFRESH_TOKEN_VALID_TIME = 1000 * 60 * 60 * 24 * 14; // 14일

	public static final String ACCESS_SECRET_KEY = System.getenv("ACCESS_SECRET_KEY");
	public static final String REFRESH_SECRET_KEY = System.getenv("REFRESH_SECRET_KEY");

	public static final String KAKAO_CLIENT_ID = System.getenv("KAKAO_CLIENT_ID");
	public static final String KAKAO_REDIRECT_URI = "http://localhost:8080/auth/kakao/callback";
}
