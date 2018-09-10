package me.forward.dts;

import java.util.List;
import me.forward.dts.model.Record;

/**
 * @author denghui
 * @create 2018/9/10
 */
public interface SinkSplitRunner {

    void sink(List<Record> records);
}
