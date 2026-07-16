package com.app.app_website_do_luu_niem.service.address;

import com.app.app_website_do_luu_niem.config.AppConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddressApiService {

    private final Map<String, AddressDataProvider> providers = new LinkedHashMap<>();

    private static final Map<String, List<AddressPlace>> provinceCache = new ConcurrentHashMap<>();
    private static final Map<String, List<AddressPlace>> districtCache = new ConcurrentHashMap<>();
    private static final Map<String, List<AddressPlace>> wardCache = new ConcurrentHashMap<>();

    public AddressApiService() {
        providers.put("open-api", new OpenApiVnProvider());
        providers.put("vnappmob", new VnappmobProvider());
        if (AppConfig.isGhnEnabled()) {
            providers.put("ghn", new GhnProvider());
        }
    }

    public List<String> listProviderIds() {
        return new ArrayList<>(providers.keySet());
    }

    public String getDefaultProviderId() {
        String configured = AppConfig.getAddressApiProvider();
        if (providers.containsKey(configured)) {
            return configured;
        }
        return "open-api";
    }

    public List<AddressPlace> getProvinces(String providerId) throws Exception {
        String id = providerId != null && !providerId.isBlank() ? providerId.trim() : getDefaultProviderId();
        try {
            return provinceCache.computeIfAbsent(id, key -> {
                try {
                    return resolve(key).fetchProvinces();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    public List<AddressPlace> getDistricts(String providerId, String provinceCode) throws Exception {
        if (provinceCode == null || provinceCode.isBlank()) {
            return List.of();
        }
        String id = providerId != null && !providerId.isBlank() ? providerId.trim() : getDefaultProviderId();
        String cacheKey = id + ":" + provinceCode.trim();
        try {
            return districtCache.computeIfAbsent(cacheKey, key -> {
                try {
                    String[] parts = key.split(":", 2);
                    return resolve(parts[0]).fetchDistricts(parts[1]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    public List<AddressPlace> getWards(String providerId, String districtCode) throws Exception {
        if (districtCode == null || districtCode.isBlank()) {
            return List.of();
        }
        String id = providerId != null && !providerId.isBlank() ? providerId.trim() : getDefaultProviderId();
        String cacheKey = id + ":" + districtCode.trim();
        try {
            return wardCache.computeIfAbsent(cacheKey, key -> {
                try {
                    String[] parts = key.split(":", 2);
                    return resolve(parts[0]).fetchWards(parts[1]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    private AddressDataProvider resolve(String providerId) {
        String id = providerId != null && !providerId.isBlank() ? providerId.trim() : getDefaultProviderId();
        AddressDataProvider p = providers.get(id);
        if (p == null) {
            throw new IllegalArgumentException("Provider không hỗ trợ: " + id);
        }
        return p;
    }
}
