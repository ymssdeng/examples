package me.forward.dts.model;

import com.google.common.collect.Range;
import lombok.Data;

/**
 * @author denghui
 * @create 2018/9/7
 */
@Data
public class QuerySplit {

    private String sourceTable;
    private Range<String> idRange;
}
