package com.bishe.dormitory.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageResult<T> {
    private List<T> rows;
    private long total;
    private int page;
    private int size;

    public PageResult(List<T> rows, long total, int page, int size) {
        this.rows = rows;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("rows", rows);
        map.put("total", total);
        map.put("page", page);
        map.put("size", size);
        map.put("pages", (total + size - 1) / size);
        return map;
    }

    public List<T> getRows() { return rows; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
