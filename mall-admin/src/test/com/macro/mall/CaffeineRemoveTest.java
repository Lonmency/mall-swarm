package com.macro.mall;

import com.github.benmanes.caffeine.cache.*;
import io.micrometer.core.lang.NonNull;
import io.micrometer.core.lang.Nullable;
import org.checkerframework.checker.index.qual.NonNegative;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class CaffeineRemoveTest {
    /**
     * 访问后到期
     *
     * @throws InterruptedException
     */
    @Test
    public void testEvictionAfterProcess() throws InterruptedException {
        // 设置访问5秒后数据到期
        Cache<Integer, Integer> cache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.SECONDS).scheduler(Scheduler.systemScheduler())
                .build();
        cache.put(1, 2);
        System.out.println(cache.getIfPresent(1));

        Thread.sleep(6000);

        System.out.println(cache.getIfPresent(1));
    }

    private int index = 1;
    private int getInDB(int key) {
        return ++ index;
    }
    /**
     * 写入后到期
     *
     * @throws InterruptedException
     */
    @Test
    public void testEvictionAfterWrite() throws InterruptedException {
        // 设置写入5秒后数据到期
        LoadingCache<Integer, Integer> cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
//                .scheduler(Scheduler.systemScheduler())
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) {
                        System.out.println("load");
                        return getInDB(key);
                    }
                });
        cache.put(1, index);

        System.out.println("getIfPresent: " + cache.getIfPresent(1));
        System.out.println("get: " + cache.get(1));

        Thread.sleep(5000);

        System.out.println("getIfPresent: " + cache.getIfPresent(1));
        System.out.println("get: " + cache.get(1));

        Thread.sleep(2000);

        System.out.println("getIfPresent: " + cache.getIfPresent(1));
        System.out.println("get: " + cache.get(1));

        Thread.sleep(3000);

        System.out.println("getIfPresent: " + cache.getIfPresent(1));
        System.out.println("get: " + cache.get(1));
    }

    public static void main(String[] args) throws InterruptedException {
        new CaffeineRemoveTest().testEvictionAfterWrite();
    }

    /**
     * 自定义过期时间
     *
     * @throws InterruptedException
     */
    @Test
    public void testEvictionAfter() throws InterruptedException {
        Cache<Integer, Integer> cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<Integer, Integer>() {
                    // 创建1秒后过期，可以看到这里必须要用纳秒
                    @Override
                    public long expireAfterCreate(@NonNull Integer key, @NonNull Integer value, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(1);
                    }

                    // 更新2秒后过期，可以看到这里必须要用纳秒
                    @Override
                    public long expireAfterUpdate(@NonNull Integer key, @NonNull Integer value, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(2);
                    }

                    // 读3秒后过期，可以看到这里必须要用纳秒
                    @Override
                    public long expireAfterRead(@NonNull Integer key, @NonNull Integer value, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(3);
                    }
                }).scheduler(Scheduler.systemScheduler())
                .build();

        cache.put(1, 2);

        System.out.println(cache.getIfPresent(1));

        Thread.sleep(6000);

        System.out.println(cache.getIfPresent(1));
    }

}
