package com.app.app_website_do_luu_niem.service.address;

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

/** https://vapi.vnappmob.com/ */
public class VnappmobProvider implements AddressDataProvider {

    private static final String BASE = "https://vapi.vnappmob.com/api/province";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build();

    @Override
    public String getId() {
        return "vnappmob";
    }

    @Override
    public List<AddressPlace> fetchProvinces() throws Exception {
        return parseResults(fetchBody(BASE + "/"));
    }

    @Override
    public List<AddressPlace> fetchDistricts(String provinceCode) throws Exception {
        return parseResults(fetchBody(BASE + "/district/" + provinceCode));
    }

    @Override
    public List<AddressPlace> fetchWards(String districtCode) throws Exception {
        return parseResults(fetchBody(BASE + "/ward/" + districtCode));
    }

    private String fetchBody(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("VNAppMob HTTP " + res.statusCode());
        }
        return res.body();
    }

    private List<AddressPlace> parseResults(String json) {
        List<AddressPlace> list = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!root.has("results")) {
            return list;
        }
        JsonArray arr = root.getAsJsonArray("results");
        for (JsonElement item : arr) {
            JsonObject o = item.getAsJsonObject();
            String code = firstField(o, "province_id", "district_id", "ward_id");
            String name = firstField(o, "province_name", "district_name", "ward_name");
            if (!code.isBlank() && !name.isBlank()) {
                list.add(new AddressPlace(code, name));
            }
        }
        return list;
    }

    private static String firstField(JsonObject o, String... keys) {
        for (String key : keys) {
            if (!o.has(key) || o.get(key).isJsonNull()) {
                continue;
            }
            JsonElement el = o.get(key);
            if (el.isJsonPrimitive()) {
                var p = el.getAsJsonPrimitive();
                if (p.isNumber()) {
                    return String.valueOf(p.getAsLong());
                }
                return p.getAsString();
            }
        }
        return "";
    }
}
