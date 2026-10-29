package com.example.demo.aspect;

import com.example.demo.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Slf4j
@RequiredArgsConstructor
@Component
public class DistributedLockAspect {
    private final RedissonClient redissonClient;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object handleDistributedLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String dynamicKey = parseSpelKey(distributedLock.key(), method,args);

        String lockKey = distributedLock.keyPrefix() + dynamicKey;

        //Lấy đối tượng RLock tu Redis
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try{
            isLocked = lock.tryLock(distributedLock.waitTime(),distributedLock.leaseTime(),distributedLock.timeUnit());
            if(isLocked){
                //Tiếp tục chạy hàm gốc
                return joinPoint.proceed();
            }else {
                throw new RuntimeException("Hệ thống đang bận. Timeout khi đợi lock: " + lockKey);
            }
        } finally{
            if(isLocked && lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }
    // Hàm biên dịch SpEL (VD: param có "#productId", args có giá trị "PROD-001" -> trả về "PROD-001")
    private String parseSpelKey(String spel, Method method,Object[] args){
        if(spel == null || spel.trim().isEmpty())
            //Fallback nếu không truyền key
            return method.getName();
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();

        if(params != null){
            for(int i=0; i< params.length; i++){
                context.setVariable(params[i],args[i]);
            }
        }
        try{
            return parser.parseExpression(spel).getValue(context,String.class);
        } catch (Exception e){
            // Nếu parse lỗi (thực chất nó không phải SpEL hợp lệ), trả về string nguyên gốc
            return spel;
        }
    }
}
