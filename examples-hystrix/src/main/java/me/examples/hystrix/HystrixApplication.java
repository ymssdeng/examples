package me.examples.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import java.util.concurrent.TimeUnit;

/**
 * @author denghui
 * @create 2018/9/26
 */
public class HystrixApplication {

    static class TestCommand extends HystrixCommand<String> {

        public TestCommand() {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("TestCommand"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("hello"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.defaultSetter()
                    .withCoreSize(2)
                    .withMaxQueueSize(2))
                .andCommandPropertiesDefaults(HystrixCommandProperties.defaultSetter()
                    .withExecutionTimeoutInMilliseconds(200)
                    .withCircuitBreakerRequestVolumeThreshold(2)
                    .withCircuitBreakerErrorThresholdPercentage(100)));
        }

        @Override
        protected String run() throws Exception {
            throw new RuntimeException("xx");
            //return "hello";
        }

        @Override
        protected String getFallback() {
            return "this is fallback";
        }
    }

    public static void main(String[] args) {
        System.out.println(new TestCommand().execute());
    }
}