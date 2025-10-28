package com.mediforme.mediforme.service.impl;

import com.mediforme.mediforme.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @Override
    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
