package com.bishe.dormitory.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, Map<String, Object>> tokenMap = new ConcurrentHashMap<>();

    public String createToken(Map<String, Object> user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenMap.put(token, user);
        return token;
    }

    public Map<String, Object> getUser(String token) {
        return tokenMap.get(token);
    }

    public void removeToken(String token) {
        tokenMap.remove(token);
    }
}
