package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicFluxOperatorTest {

    /**
     * Flux
     * 데이터 : just, empty, from-시리즈
     * 함수 : defer, create
     */
    @Test
    public void testFluxFromDate() {
        Flux.just(1, 2, 3, 4)
                .subscribe(data -> System.out.println("data = " + data));

        List<Integer> basicList = List.of(1, 2, 3, 4);
        Flux.fromIterable(basicList)
                .subscribe(data -> System.out.println("data fromIterable = " + data));
    }

    /**
     * Flux defer -> 안에서 Flux 객체를 반환해줘야 한다.
     * Flux create -> 안에서 동기적인 객체를 반환해줘야 한다.
     */
    @Test
    public void testFluxFromFunction() {
        Flux.defer(() -> {
            return Flux.just(1, 2, 3, 4);
        }).subscribe(data -> System.out.println("data from defer = " + data));

        Flux.create(sink -> {
            sink.next(1);
            sink.next(2);
            sink.next(3);
            sink.complete(); // complete를 사용하는 sink가 구독자 입장에서 언제 끝나는지 알 수 없다. sink 사용할 때 마지막에 호출
        }).subscribe(data -> System.out.println("data from sink = " + data));
    }

    @Test
    public void testSinkDetail() {
        Flux.<String>create(sink -> {
                    AtomicInteger counter = new AtomicInteger(0);
                    recursiveFunction(sink);
                }).contextWrite(Context.of("counter", new AtomicInteger(0)))
                .subscribe(data -> System.out.println("data from recursive = " + data));
    }

    // AtomicInteger는 여러 스레드가 접근을 해도 안전하게 연산할 수 있도록 도와주는 클래스
    // 보통 WebFlux에서 count 같은게 필요할 때 즉, 여러스레드에서 count 값을 올리고 내려도 안전하게 값을 연산할 수 있는 객체가 필요할 때
    public void recursiveFunction(FluxSink<String> sink) {
        AtomicInteger counter = sink.contextView().get("counter");
        if (counter.incrementAndGet() < 10) { // ++int
            sink.next("sink count " + counter);
            recursiveFunction(sink);
        } else {
            sink.complete();
        }
    }

    // ThreadLocal -> context

    /**
     * Flux의 흐름 시작 방법
     * 1. 데이터로 부터 : just, empty, from시리즈
     * 2. 함수로 부터 : defer (Flux객체를 return), create (동기적인 객체를 return - next)
     */

    @Test
    public void testFluxCollectionList() {
        Mono<List<Integer>> listMono = Flux.<Integer>just(1, 2, 3, 4, 5) // 데이터로부터 시작
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .collectList();// collectList는 Mono로 감싸진 list 형태로 flux로 변경해주고 있다. 데이터가 다 처리될 때까지 기다렸다가 list로 모아주겠다. 언제 끝날지 모르니, Mono로 감싼다.

        // collectList는 list를 하나로 모아서 처리하고 싶을 때 사용한다. Flux를 Mono로 바꿀 수 있다.
        listMono.subscribe(data -> System.out.println("collectList가 변환한 list data = " + data));
    }

    /**
     * Mono -> Flux 변환은 flatMapMany
     * Flux -> Mono 변환은 collectList
     */
}
