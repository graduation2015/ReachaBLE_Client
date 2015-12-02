package jp.ac.it_college.std.reachable_client;

import java.util.List;

public class CouponInfo {
    private final String key;
    private final String name;
    private final String address;
    private final List<String> category;

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String CATEGORY = "category";

    public CouponInfo(String key, String name, String address, List<String> category) {
        this.key = key;
        this.name = name;
        this.address = address;
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getCategory() {
        return category;
    }
}