package com.legacyvault.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 *
 * @param <T> 列表元素类型
 * @author LegacyVault
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long pageNum;

    /** 每页大小 */
    private Long pageSize;

    /** 总页数 */
    private Long totalPages;

    /** 数据列表 */
    private List<T> records;

    public PageResult() {}

    public PageResult(Long total, Long pageNum, Long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }
}
