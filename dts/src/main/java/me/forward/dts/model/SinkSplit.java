package me.forward.dts.model;

import java.util.List;
import lombok.Data;

/**
 * @author denghui
 * @create 2018/9/7
 */
@Data
public class SinkSplit {

    private String targetTable;
    private List<Record> records;

}
