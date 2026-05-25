package com.app.app_website_do_luu_niem.model;

/**
 * Dòng danh mục hiển thị trên trang quản trị (kèm số sản phẩm).
 */
public class CategoryAdminRow {
    private int id;
    private String name;
    private String description;
    private long productCount;

    public static CategoryAdminRow from(Category category, long productCount) {
        CategoryAdminRow row = new CategoryAdminRow();
        row.setId(category.getId());
        row.setName(category.getName());
        row.setDescription(category.getDescription());
        row.setProductCount(productCount);
        return row;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }

    public boolean hasProducts() {
        return productCount > 0;
    }
}
