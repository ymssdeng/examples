package me.forward.examples.gateway.filter;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.zuul.ZuulFilter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author denghui
 * @create 2018/7/25
 */
@Component
@Slf4j
public class SampleFilter extends ZuulFilter {

    private static final long LIMIT = 10;
    private static LoadingCache<Long, AtomicLong> counter =
        CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build(new CacheLoader<Long, AtomicLong>() {
                @Override
                public AtomicLong load(Long seconds) throws Exception {
                    return new AtomicLong(0);
                }
            });

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1; // run before PreDecoration;
    }

    @Override
    public boolean shouldFilter() {
        return false;
    }

    @Override
    public Object run() {
        long currentSeconds = System.currentTimeMillis() / 1000; //当期秒数
        try {
            if(counter.get(currentSeconds).incrementAndGet() > LIMIT) {
                log.info("rate limit");
                throw new RuntimeException("rate limit");
            }
        } catch (ExecutionException e) {
            throw new RuntimeException("rate limit");
        }
        return null;
    }
}
