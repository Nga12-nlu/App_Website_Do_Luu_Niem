package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GhnShippingService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    private static final Map<String, BigDecimal> feeCache = new ConcurrentHashMap<>();

    public BigDecimal calculateShippingFee(int toDistrictId, String toWardCode, int weightInGrams, BigDecimal insuranceValue) throws Exception {
        String key = toDistrictId + ":" + (toWardCode != null ? toWardCode.trim() : "") + ":" + weightInGrams + ":" + (insuranceValue != null ? insuranceValue.toPlainString() : "0");
        
        BigDecimal cached = feeCache.get(key);
        if (cached != null) {
            return cached;
        }

        String url = AppConfig.getGhnApiUrl() + "shipping-order/fee";
        int fromDistrict = AppConfig.getGhnFromDistrictId();
        String fromWard = AppConfig.getGhnFromWardCode();
        int serviceTypeId = AppConfig.getGhnServiceTypeId();

        JsonObject body = new JsonObject();
        body.addProperty("from_district_id", fromDistrict);
        if (fromWard != null && !fromWard.isEmpty()) {
            body.addProperty("from_ward_code", fromWard);
        }
        body.addProperty("service_type_id", serviceTypeId);
        body.addProperty("to_district_id", toDistrictId);
        body.addProperty("to_ward_code", toWardCode);
        body.addProperty("weight", weightInGrams > 0 ? weightInGrams : AppConfig.getGhnDefaultWeight());
        // Standard package dimensions in cm
        body.addProperty("length", 15);
        body.addProperty("width", 15);
        body.addProperty("height", 15);
        int insVal = insuranceValue != null ? insuranceValue.intValue() : 0;
        if (insVal > 20000000) {
            insVal = 20000000;
        }
        body.addProperty("insurance_value", insVal);
        body.addProperty("cod_failed_amount", 0);

        String jsonBody = body.toString();

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Token", AppConfig.getGhnApiToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        String shopId = AppConfig.getGhnShopId();
        if (shopId != null && !shopId.isEmpty()) {
            builder.header("ShopId", shopId);
        }

        HttpRequest req = builder.build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            throw new IllegalStateException("GHN Shipping Fee API error HTTP " + res.statusCode() + ": " + res.body());
        }

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        if (root.has("code") && root.get("code").getAsInt() == 200 && root.has("data")) {
            JsonObject data = root.getAsJsonObject("data");
            if (data.has("total")) {
                BigDecimal result = BigDecimal.valueOf(data.get("total").getAsLong());
                if (feeCache.size() > 2000) {
                    feeCache.clear();
                }
                feeCache.put(key, result);
                return result;
            }
        }
        throw new IllegalStateException("Invalid response from GHN API: " + res.body());
    }
}
