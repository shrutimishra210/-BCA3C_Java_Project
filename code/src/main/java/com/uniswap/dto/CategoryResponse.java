package com.uniswap.dto;

public class CategoryResponse {
    private Long id;
    private String name;

    // No-arg constructor required so Jackson can deserialize the
    // GET /api/categories response into this class on the Swing side.
    public CategoryResponse() {
    }

    public CategoryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // JComboBox<CategoryResponse> (in Add/Edit Listing) displays each item
    // using toString() by default - without this override it would show
    // the Java object reference instead of the category name.
    @Override
    public String toString() {
        return name;
    }
}
