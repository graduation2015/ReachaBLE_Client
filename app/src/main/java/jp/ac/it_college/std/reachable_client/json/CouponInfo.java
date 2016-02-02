package jp.ac.it_college.std.reachable_client.json;

import java.io.Serializable;
import java.util.List;

public class CouponInfo implements Serializable{
    private final String filePath;
    private final String key;
    private final String title;
    private final String name;
    private final String address;
    private final String description;
    private final List<String> category;

    public static final String FILE_PATH = "file_path";
    public static final String NAME = "companyName";
    public static final String ADDRESS = "address";
    public static final String CATEGORY = "category";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";

    public CouponInfo(String key, String title, String name, String address,
                      String description, List<String> category, String filePath) {
        this.filePath = filePath;
        this.key = key;
        this.title = title;
        this.name = name;
        this.address = address;
        this.category = category;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() { return title; }

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

    public String getMetaData () {
        String categorys = "";
        for (String hoge : category) {
            categorys += hoge + " ";
        }
        return key + name + address + description + categorys;
    }

    public String getFilePath() {
        return filePath;
    }
}