package org.example.service;

import org.example.model.ApiResponse;

import java.time.LocalDateTime;
import java.util.Map;

public interface ApiService {
    ApiResponse getData(String url);

    ApiResponse getData(String url, Map<String, String> headers);

    ApiResponse getData(String url, Map<String, String> headers, LocalDateTime evictionTime);
}
