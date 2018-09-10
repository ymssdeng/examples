package me.forward.dts;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.forward.dts.config.DtsProperties;
import me.forward.dts.model.QuerySplit;
import me.forward.dts.model.Record;
import me.forward.dts.model.SinkSplit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@Slf4j
@Component
public class Dts implements ApplicationRunner {

    @Autowired
    protected DtsProperties dtsProperties;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private QuerySplitRunner querySplitRunner;
    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private SinkSplitRunner sinkSplitRunner;

    private ExecutorService queryExecutor;
    private ExecutorService sinkExecutor;

    @PostConstruct
    public void init() {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("query-runner-%d");
        queryExecutor = Executors.newFixedThreadPool(dtsProperties.getQueryThread(), builder.build());
        builder.setNameFormat("sink-runner-%d");
        sinkExecutor = Executors.newFixedThreadPool(dtsProperties.getSinkThread(), builder.build());
    }

    public void start() {
        String minId = getMinId();
        String maxId = getMaxId();
        List<String> ids = splitId(minId, maxId);

        List<CompletableFuture> queryFutures = new ArrayList<>();
        List<CompletableFuture> sinkFutures = new ArrayList<>();
        for (int i = 0; i < ids.size() - 1; i++) {
            final String lower = ids.get(i);
            final String upper = ids.get(i + 1);

            CompletableFuture future = CompletableFuture
                .supplyAsync(() -> {
                    QuerySplit split = new QuerySplit();
                    split.setIdRange(Range.closed(lower, upper));
                    split.setSourceTable(dtsProperties.getSourceTable());
                    return querySplitRunner.query(split);
                    }, queryExecutor)
                .thenApplyAsync(records -> {
                    return Lists.partition(records, dtsProperties.getBatchSize())
                        .stream()
                        .map(partitionRecords -> {
                            List<Record> mappedRecords = partitionRecords.stream()
                                .map(r -> fieldMapper.apply(r))
                                .filter(r -> r != null)
                                .collect(Collectors.toList());
                            if (mappedRecords.isEmpty()) {
                                return null;
                            }
                            final SinkSplit split = new SinkSplit();
                            split.setRecords(mappedRecords);
                            split.setTargetTable(dtsProperties.getTargetTable());
                            return split;
                        })
                        .filter(r -> r != null)
                        .collect(Collectors.toList());
                    }, queryExecutor)
                .thenAcceptAsync(splits -> {
                    for (SinkSplit split : splits) {
                        sinkFutures.add(CompletableFuture.supplyAsync(() -> sinkSplitRunner.sink(split), sinkExecutor));
                    }
                }, queryExecutor);
            queryFutures.add(future);
        }

        CompletableFuture.allOf(queryFutures.toArray(new CompletableFuture[0]))
            .whenComplete((v, t) -> {
                CompletableFuture.allOf(sinkFutures.toArray(new CompletableFuture[0]))
                    .whenComplete((v2, t2) -> log.info("sink done"));
            });
    }

    private List<String> splitId(String minId, String maxId) {
        return Arrays.asList(minId, maxId);
    }

    private String getMinId() {
        DBObject query = new BasicDBObject();
        DBObject projection = new BasicDBObject("_id", 1);
        DBObject sort = new BasicDBObject("_id", 1);
        DBObject min = mongoTemplate.getCollection(dtsProperties.getSourceTable()).findOne(query, projection, sort);
        return min.get("_id").toString();
    }

    private String getMaxId() {
        DBObject query = new BasicDBObject();
        DBObject projection = new BasicDBObject("_id", 1);
        DBObject sort = new BasicDBObject("_id", -1);
        DBObject max = mongoTemplate.getCollection(dtsProperties.getSourceTable()).findOne(query, projection, sort);
        return max.get("_id").toString();
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        start();
    }
}
