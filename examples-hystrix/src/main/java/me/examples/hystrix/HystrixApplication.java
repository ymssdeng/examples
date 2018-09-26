package me.examples.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @author denghui
 * @create 2018/9/26
 */
public class HystrixApplication {

    static class TestCommand extends HystrixCommand<String> {

        public TestCommand() {
            super(HystrixCommandGroupKey.Factory.asKey("key"));
        }

        @Override
        protected String run() throws Exception {
            return "hello";
        }
    }

    public static void main(String[] args) {
        System.out.println(new TestCommand().isExecutedInThread());
    }
}