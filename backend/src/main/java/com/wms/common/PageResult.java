package com.wms.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setCurrent(page.getCurrent());
        result.setPages(page.getPages());
        return result;
    }
}
