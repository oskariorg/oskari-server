package org.oskari.print.loader;

import java.awt.image.BufferedImage;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import fi.nls.oskari.util.PropertyUtil;

public abstract class CommandLoadImageBase extends HystrixCommand<BufferedImage> {

    private static final String GROUP_KEY = "print";

    public CommandLoadImageBase(String commandName) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(AsyncImageLoader.GROUP_KEY))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandName))
                .andThreadPoolPropertiesDefaults(
                        HystrixThreadPoolProperties.Setter()
                        .withCoreSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.size", 10))
                        .withMaxQueueSize(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.limit", 100))
                        .withQueueSizeRejectionThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.pool.queue", 100)))
                .andCommandPropertiesDefaults(
                        HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".job.timeoutms", 15000))
                        .withCircuitBreakerRequestVolumeThreshold(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".failrequests", 5))
                        .withMetricsRollingStatisticalWindowInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".rollingwindow", 100000))
                        .withCircuitBreakerSleepWindowInMilliseconds(PropertyUtil.getOptional("oskari." + GROUP_KEY + ".sleepwindow", 20000)))
        );
    }

}
