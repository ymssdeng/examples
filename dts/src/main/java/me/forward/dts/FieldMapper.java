package me.forward.dts;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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
    private Map<String, Function> converters = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        byte[] bytes = Files.readAllBytes(new ClassPathResource(MAPPER_FILE).getFile().toPath());
        innerMap = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8)).getInnerMap();

        addValueConverter("_id", ValueConverters.TO_STRING_CONVERTER);
    }

    public void addValueConverter(String field, Function converter) {
        converters.put(field, converter);
    }

    @Override
    public Record apply(Record record) {
        Record mappedRecord = new Record();
        for (SimpleEntry<String, Object> field : record) {
            Object mappedKey = innerMap.get(field.getKey());
            if (mappedKey != null) {
                Object value = field.getValue();
                if (converters.containsKey(field.getKey())) {
                    value = converters.get(field.getKey()).apply(field.getValue());
                }
                if (value != null) {
                    mappedRecord.add(new SimpleEntry<>(mappedKey.toString(), value));
                }
            }
        }
        return mappedRecord.isEmpty() ? null : mappedRecord;
    }
}
