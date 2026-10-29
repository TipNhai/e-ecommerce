package com.example.demo.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String keyPrefix() default "lock:";
    String key();
    long waitTime() default 5;
    long leaseTime() default 10;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
