package me.forward.dts;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import me.forward.dts.config.DtsProperties;
import me.forward.dts.model.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/9/6
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Component
public class DtsApplication implements ApplicationRunner {

    @Autowired
    protected DtsProperties dtsProperties;
    @Autowired
    private QuerySplitRunner querySplitRunner;
    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private SinkSplitRunner sinkSplitRunner;
    @Autowired
    private MetricService metricService;

    private ExecutorService queryExecutor;
    private ExecutorService mapExecutor;
    private ExecutorService sinkExecutor;

    @PostConstruct
    public void init() {
        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("query-runner-%d");
        queryExecutor = Executors.newFixedThreadPool(dtsProperties.getQuery().getThreadCount(), builder.build());
        builder.setNameFormat("map-runner-%d");
        mapExecutor = Executors.newFixedThreadPool(dtsProperties.getQuery().getThreadCount(), builder.build());
        builder.setNameFormat("sink-runner-%d");
        sinkExecutor = Executors.newFixedThreadPool(dtsProperties.getSink().getThreadCount(), builder.build());
    }

    public void start() {
        metricService.startQuery();

        Range<String> range = querySplitRunner.getMinMaxId();
        List<String> ids = querySplitRunner.splitId(range);

        List<CompletableFuture> queryFutures = new ArrayList<>();
        List<CompletableFuture> sinkFutures = new ArrayList<>();
        for (int i = 0; i < ids.size() - 1; i++) {
            final String lower = ids.get(i);
            final String upper = ids.get(i + 1);

            CompletableFuture future = CompletableFuture
                .supplyAsync(() -> querySplitRunner.query(Range.closed(lower, upper)), queryExecutor)
                .thenApplyAsync(records -> {
                    return Lists.partition(records, dtsProperties.getSink().getBatchSize())
                        .stream()
                        .map(partitionRecords -> {
                            List<Record> mappedRecords = partitionRecords.stream()
                                .map(r -> fieldMapper.apply(r))
                                .filter(r -> r != null)
                                .collect(Collectors.toList());
                            if (mappedRecords.isEmpty()) {
                                return null;
                            }
                            return mappedRecords;
                        })
                        .filter(r -> r != null)
                        .collect(Collectors.toList());
                }, mapExecutor)
                .thenAcceptAsync(splits -> {
                    metricService.startSink();
                    for (List<Record> split : splits) {
                        sinkFutures.add(CompletableFuture.runAsync(() -> sinkSplitRunner.sink(split), sinkExecutor));
                    }
                }, sinkExecutor);
            queryFutures.add(future);
        }

        CompletableFuture.allOf(queryFutures.toArray(new CompletableFuture[0]))
            .whenComplete((v, t) -> {
                metricService.endQuery();
                CompletableFuture.allOf(sinkFutures.toArray(new CompletableFuture[0]))
                    .whenComplete((v2, t2) -> {
                        metricService.endSink();
                        metricService.print();


                    });
            });

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }

    public static void main(String[] args) {
        SpringApplication.run(DtsApplication.class, args);
    }

}
