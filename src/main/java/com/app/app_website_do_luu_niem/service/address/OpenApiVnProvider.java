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

/** https://provinces.open-api.vn/api/ */
public class OpenApiVnProvider implements AddressDataProvider {

    private static final String BASE = "https://provinces.open-api.vn/api";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build();

    @Override
    public String getId() {
        return "open-api";
    }

    @Override
    public List<AddressPlace> fetchProvinces() throws Exception {
        return parsePlaces(fetchJson(BASE + "/"));
    }

    @Override
    public List<AddressPlace> fetchDistricts(String provinceCode) throws Exception {
        JsonObject root = fetchJson(BASE + "/p/" + provinceCode + "?depth=1");
        if (root.has("districts")) {
            return parsePlaces(root.get("districts"));
        }
        return List.of();
    }

    @Override
    public List<AddressPlace> fetchWards(String districtCode) throws Exception {
        JsonObject root = fetchJson(BASE + "/d/" + districtCode + "?depth=1");
        if (root.has("wards")) {
            return parsePlaces(root.get("wards"));
        }
        return List.of();
    }

    private JsonObject fetchJson(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException("Open API VN HTTP " + res.statusCode());
        }
        JsonElement el = JsonParser.parseString(res.body());
        if (el.isJsonArray()) {
            JsonObject wrap = new JsonObject();
            wrap.add("items", el.getAsJsonArray());
            return wrap;
        }
        return el.getAsJsonObject();
    }

    private List<AddressPlace> parsePlaces(JsonElement el) {
        List<AddressPlace> list = new ArrayList<>();
        JsonArray arr;
        if (el.isJsonObject() && el.getAsJsonObject().has("items")) {
            arr = el.getAsJsonObject().getAsJsonArray("items");
        } else if (el.isJsonArray()) {
            arr = el.getAsJsonArray();
        } else {
            return list;
        }
        for (JsonElement item : arr) {
            JsonObject o = item.getAsJsonObject();
            String code = jsonFieldAsString(o, "code");
            String name = jsonFieldAsString(o, "name");
            if (!code.isBlank() && !name.isBlank()) {
                list.add(new AddressPlace(code, name));
            }
        }
        return list;
    }

    private static String jsonFieldAsString(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).isJsonNull()) {
            return "";
        }
        JsonElement el = o.get(key);
        if (el.isJsonPrimitive()) {
            var p = el.getAsJsonPrimitive();
            if (p.isNumber()) {
                return String.valueOf(p.getAsLong());
            }
            return p.getAsString();
        }
        return "";
    }
}
