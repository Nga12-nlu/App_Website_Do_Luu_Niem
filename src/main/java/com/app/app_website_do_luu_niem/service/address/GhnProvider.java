package com.app.app_website_do_luu_niem.service.address;

import com.app.app_website_do_luu_niem.config.AppConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GhnProvider implements AddressDataProvider {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    @Override
    public String getId() {
        return "ghn";
    }

    @Override
    public List<AddressPlace> fetchProvinces() throws Exception {
        String url = AppConfig.getGhnApiUrl() + "master-data/province";
        JsonObject json = getJson(url);
        List<AddressPlace> list = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("data");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String id = String.valueOf(obj.get("ProvinceID").getAsInt());
                String name = obj.get("ProvinceName").getAsString();
                list.add(new AddressPlace(id, name));
            }
        }
        return list;
    }

    @Override
    public List<AddressPlace> fetchDistricts(String provinceCode) throws Exception {
        String url = AppConfig.getGhnApiUrl() + "master-data/district";
        int provId = Integer.parseInt(provinceCode.trim());
        String body = "{\"province_id\":" + provId + "}";
        JsonObject json = postJson(url, body);
        List<AddressPlace> list = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("data");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String id = String.valueOf(obj.get("DistrictID").getAsInt());
                String name = obj.get("DistrictName").getAsString();
                list.add(new AddressPlace(id, name));
            }
        }
        return list;
    }

    @Override
    public List<AddressPlace> fetchWards(String districtCode) throws Exception {
        String url = AppConfig.getGhnApiUrl() + "master-data/ward";
        int distId = Integer.parseInt(districtCode.trim());
        String body = "{\"district_id\":" + distId + "}";
        JsonObject json = postJson(url, body);
        List<AddressPlace> list = new ArrayList<>();
        if (json.has("data") && json.get("data").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("data");
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String code = obj.get("WardCode").getAsString();
                String name = obj.get("WardName").getAsString();
                list.add(new AddressPlace(code, name));
            }
        }
        return list;
    }

    private JsonObject getJson(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .header("Token", AppConfig.getGhnApiToken())
                .header("Accept", "application/json")
                .build();
        return execute(req);
    }

    private JsonObject postJson(String url, String requestBody) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Token", AppConfig.getGhnApiToken())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        return execute(req);
    }

    private JsonObject execute(HttpRequest req) throws Exception {
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("GHN API HTTP " + res.statusCode() + ": " + res.body());
        }
        JsonElement el = JsonParser.parseString(res.body());
        return el.getAsJsonObject();
    }
}
