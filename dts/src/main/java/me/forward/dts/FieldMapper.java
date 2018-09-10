package me.forward.dts;

import com.alibaba.fastjson.JSON;
import com.mongodb.Function;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import me.forward.dts.model.Record;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Component
public class FieldMapper implements Function<Record, Record> {

    private static final String MAPPER_FILE = "mapper.json";
    private Map<String, Object> innerMap;

    @PostConstruct
    public void init() throws IOException {
        byte[] bytes = Files.readAllBytes(new ClassPathResource(MAPPER_FILE).getFile().toPath());
        innerMap = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8)).getInnerMap();
    }

    @Override
    public Record apply(Record record) {
        Record mappedRecord = new Record();
        for (SimpleEntry<String, Object> field : record) {
            Object mappedKey = innerMap.get(field.getKey());
            if (mappedKey != null && field.getValue() != null) {
                //!!ObjectId->String
                mappedRecord.add(new SimpleEntry<>(mappedKey.toString(), field.getValue().toString()));
            }
        }
        return mappedRecord.isEmpty() ? null : mappedRecord;
    }
}
