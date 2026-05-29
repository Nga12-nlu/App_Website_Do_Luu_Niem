package com.app.app_website_do_luu_niem.service;

import com.app.app_website_do_luu_niem.dao.StaticContentDao;
import com.app.app_website_do_luu_niem.dao.impl.StaticContentDaoImpl;

import java.util.LinkedHashMap;
import java.util.Map;

public class StaticContentService {

    private static final long CACHE_TTL_MS = 60_000L;

    private final StaticContentDao staticContentDao = new StaticContentDaoImpl();
    private volatile Map<String, String> cachedMap;
    private volatile long cacheLoadedAt;

    public Map<String, String> resolveContentMap() {
        long now = System.currentTimeMillis();
        Map<String, String> hit = cachedMap;
        if (hit != null && now - cacheLoadedAt < CACHE_TTL_MS) {
            return hit;
        }
        Map<String, String> merged = defaultContentMap();
        try {
            merged.putAll(staticContentDao.findActiveMap());
        } catch (Exception ignored) {
            // DB chưa sẵn sàng hoặc lỗi tạm thời: giữ fallback mặc định để site vẫn chạy.
        }
        cachedMap = merged;
        cacheLoadedAt = now;
        return merged;
    }

    public void invalidateCache() {
        cachedMap = null;
        cacheLoadedAt = 0;
    }

    public static Map<String, String> defaultContentMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("home.hero.title", "Chào mừng đến với Souvenir Shop");
        map.put("home.hero.subtitle", "Khám phá những món quà lưu niệm độc đáo, mang đậm dấu ấn văn hóa Việt Nam");
        map.put("home.hero.badge1", "Giao hàng toàn quốc");
        map.put("home.hero.badge2", "Thanh toán an toàn");
        map.put("home.hero.badge3", "Nhiều mẫu và biến thể");
        map.put("home.latest.title", "Sản phẩm mới nhất");
        map.put("home.latest.cta", "Xem tất cả sản phẩm");
        map.put("footer.brand.title", "Souvenir Shop");
        map.put("footer.brand.description", "Cửa hàng đồ lưu niệm Việt Nam với sản phẩm tinh tế, phù hợp làm quà tặng và lưu giữ kỷ niệm.");
        map.put("footer.contact.address", "Hà Nội, Việt Nam");
        map.put("footer.contact.phone", "Hotline: 09xx xxx xxx");
        map.put("footer.contact.email", "support@souvenirshop.vn");
        map.put("footer.copyright", "Souvenir Shop - Website bán đồ lưu niệm Việt Nam");
        return map;
    }
}
