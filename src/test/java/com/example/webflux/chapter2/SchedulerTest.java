package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SchedulerTest {

    /**
     * Scheduler를 할당할 수 있는 구간
     * subscribe, publish
     */
    @Test
    public void testBasicFluxMono() {
        Mono.<Integer>just(Integer.valueOf(2))
                .map(data -> {
                    System.out.println("map Thread Name = " + Thread.currentThread().getName());
                    return (Integer) (data * 2);
                })
                .publishOn(Schedulers.parallel())
                .filter(data -> {
                    System.out.println("filter Thread Name = " + Thread.currentThread().getName());
                    return data % 4 == 0;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(data -> System.out.println("Mono가 구독한 data! = " + data));
    }
}
