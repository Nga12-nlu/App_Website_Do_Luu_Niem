package com.app.app_website_do_luu_niem.controller.api;

import com.app.app_website_do_luu_niem.service.address.AddressApiService;
import com.app.app_website_do_luu_niem.service.address.AddressPlace;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "addressApiServlet", urlPatterns = "/api/address/*")
public class AddressApiServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final AddressApiService addressApi = new AddressApiService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null) {
            path = "/";
        }
        String provider = req.getParameter("provider");
        if (provider == null || provider.isBlank()) {
            provider = addressApi.getDefaultProviderId();
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");

        try {
            switch (path) {
                case "/providers" -> writeJson(resp, Map.of(
                        "defaultProvider", addressApi.getDefaultProviderId(),
                        "providers", addressApi.listProviderIds()
                ));
                case "/provinces" -> writePlaces(resp, addressApi.getProvinces(provider), provider);
                case "/districts" -> {
                    String province = req.getParameter("province");
                    if (province == null || province.isBlank()) {
                        writeError(resp, 400, "Thiếu tham số province");
                        return;
                    }
                    writePlaces(resp, addressApi.getDistricts(provider, province), provider);
                }
                case "/wards" -> {
                    String district = req.getParameter("district");
                    if (district == null || district.isBlank()) {
                        writeError(resp, 400, "Thiếu tham số district");
                        return;
                    }
                    writePlaces(resp, addressApi.getWards(provider, district), provider);
                }
                default -> writeError(resp, 404, "API không tồn tại");
            }
        } catch (IllegalArgumentException e) {
            writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            req.getServletContext().log("Address API error: " + e.getMessage(), e);
            writeError(resp, 502, "Không tải được dữ liệu địa chỉ. Thử đổi provider hoặc thử lại sau.");
        }
    }

    private void writePlaces(HttpServletResponse resp, List<AddressPlace> places, String provider) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("provider", provider);
        body.put("items", places);
        resp.getWriter().write(GSON.toJson(body));
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        resp.getWriter().write(GSON.toJson(body));
    }

    private void writeJson(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        resp.getWriter().write(GSON.toJson(body));
    }
}
