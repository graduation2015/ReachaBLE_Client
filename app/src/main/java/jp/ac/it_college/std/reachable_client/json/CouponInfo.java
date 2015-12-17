package jp.ac.it_college.std.reachable_client.json;

import java.util.List;

public class CouponInfo {
    private final String key;
    private final String name;
    private final String address;
    private final String description;
    private final List<String> category;

    public static final String NAME = "companyName";
    public static final String ADDRESS = "address";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";

    public CouponInfo(String key, String name, String address, String description, List<String> category) {
        this.key = key;
        this.name = name;
        this.address = address;
        this.category = category;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getCompanyName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() { return description; }

    public List<String> getCategory() {
        return category;
    }
}