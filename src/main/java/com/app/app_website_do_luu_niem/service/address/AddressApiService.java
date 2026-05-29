package com.app.app_website_do_luu_niem.service.address;

import com.app.app_website_do_luu_niem.config.AppConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AddressApiService {

    private final Map<String, AddressDataProvider> providers = new LinkedHashMap<>();

    public AddressApiService() {
        providers.put("open-api", new OpenApiVnProvider());
        providers.put("vnappmob", new VnappmobProvider());
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
        return resolve(providerId).fetchProvinces();
    }

    public List<AddressPlace> getDistricts(String providerId, String provinceCode) throws Exception {
        if (provinceCode == null || provinceCode.isBlank()) {
            return List.of();
        }
        return resolve(providerId).fetchDistricts(provinceCode.trim());
    }

    public List<AddressPlace> getWards(String providerId, String districtCode) throws Exception {
        if (districtCode == null || districtCode.isBlank()) {
            return List.of();
        }
        return resolve(providerId).fetchWards(districtCode.trim());
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
