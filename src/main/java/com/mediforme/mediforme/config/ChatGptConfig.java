package com.mediforme.mediforme.config;

public class ChatGptConfig {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String API_KEY = System.getenv("OPENAI_API_KEY");
    public static final String MODEL = "gpt-3.5-turbo"; // 최신 모델 사용
    public static final String MEDIA_TYPE = "application/json; charset=UTF-8";
    public static final String URL = "https://api.openai.com/v1/chat/completions"; // 올바른 URL
}
