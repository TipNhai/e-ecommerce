package com.example.demo.service;

import com.example.demo.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlashSaleService {

    private int stock = 10;

    private final ApplicationContext applicationContext;

    public void resetStock(int newStock){
        this.stock = newStock;
    }

    public void buyWithoutLock(String productId, String userId){
        processPurchase(productId,userId,"NoLock");
    }

    @DistributedLock(keyPrefix = "flshsale:product:", key = "#productId", waitTime = 5, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    public void buyWithLock(String productId, String userId){
        processPurchase(productId,userId,"Lock");
    }

    //CÓ SỬ DỤNG LOCK với định tuyến SpEL
    private void processPurchase(String productId, String userId, String mode){
        if(stock > 0){
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored){
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            }
            stock--;
            log.info("["+mode+"] User: "+userId+" mua Thành Công. Kho còn: "+stock);
        } else {
            log.info("[" + mode + "] User: " + userId + " mua Thất Bại. Hết hàng!");
        }
    }

    public void runConcurrentTest(String productId, boolean useLock, int totalRequest) throws InterruptedException{
        log.info("\n=== TEST BẮT ĐẦU | useLock: " + useLock + " | totalRequest: " + totalRequest + " ===");
        resetStock(10);
        ExecutorService executorService = Executors.newFixedThreadPool(totalRequest);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalRequest);

        AtomicInteger failCount = new AtomicInteger(0);
        FlashSaleService selfProxy = applicationContext.getBean(FlashSaleService.class);

        for(int i = 0; i < totalRequest; i++){
            final String userId = "User_"+i;
            executorService.submit(()->{
                try {
                    startLatch.await();

                    if(useLock){
                        selfProxy.buyWithLock(productId,userId);
                    } else {
                        selfProxy.buyWithoutLock(productId,userId);
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        long startTime =  System.currentTimeMillis();

        //Thả tất cả thread chạy cùng lúc
        startLatch.countDown();
        //Chờ toàn bộ tiến trình hoàn tất
        endLatch.await();

        long endTime = System.currentTimeMillis();
        executorService.shutdown();
        log.info("\n=== KẾT QUẢ ===");
        log.info("Tồn kho còn lại: " + stock + " (Kỳ vọng: " + Math.max(0, 10 - totalRequest) + ")");
        log.info("Số lỗi lock timeout: " + failCount.get());
        log.info("Thời gian thực thi: " + (endTime - startTime) + " ms\n");
    }
}
