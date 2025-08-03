package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class FluxMonoErrorAndSignalTest {

    @Test
    public void testBasicSignal() {
        Flux.just(1, 2, 3, 4)
                .doOnNext(publishedData -> System.out.println("publishedData = " + publishedData))
                .doOnComplete(() -> System.out.println("doOnComplete"))
                .doOnError(ex -> {
                    System.out.println("ex 에러상황 발생! = " + ex);
                })
                .subscribe(data -> System.out.println("data = " + data));
    }

    @Test
    public void testFluxMonoError() {
        // try catch를 실행시키는건 외부 스레드
        // 외부 스레드는 자신이 가지고 있지 않은 호출 스텍의 예외를 찾아서 처리할 수 없다.
        try {
            /**
             * 에러가 왜 catch에 안잡히지??? 그러면 어떻게 에러를 처리해야하지??
             * 리액티브 스트림 안에서 발생하는 에러는 리액티브 스트림 operator 안에서 잡고 밖으로 던지지 않는다.
             * 왜 밖으로 던질 수 없는가?
             * 자바에서 예외를 찾을 때 코드를 실행중인 스레드의 호출스택을 거슬러 올라가면서 예외를 찾는다.
             * 호출 스텍이란?
             * 스레드에서 관리되는 메모리로서 스레드의 함수가 호출 될 때 마다 함수에 대한 정보를 스텍형태로 저장해두고 있다.
             * 호출 스텍은 각 스레드에서 독립적으로 관리된다.
             * 그러면 예외처리를 어떻게 하냐?
             * 1. try - catch를 operator 안에서 잡아 버린다.
             * 2. try - catch를 사용하지 않고 전파되는 에러 signal 사용 - onErrorMap, onErrorReturn, onErrorComplete
             */
            Flux.just(1, 2, 3, 4)
                    .map(data -> {
                        try {
                            if (data == 3) {
                                throw new RuntimeException();
                            }
                            return data * 2;
                        } catch (Exception e) {
                            return data * 999;
                        }
                    })
                    .onErrorMap(ex -> new IllegalArgumentException())
                    .onErrorReturn(999)
                    .onErrorComplete()
//                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(data -> System.out.println("data = " + data));
        } catch (Exception e) {
            System.out.println("에러가 발생했어요!");
        }
    }

    /**
     * Flux.Mono.error()
     */
    @Test
    public void testFluxMonoDotError() {
        Flux.just(1, 2, 3, 4)
                .flatMap(data -> {
                    if (data != 3) {
                        return Mono.just(data);
                    } else {
                        return Mono.error(new RuntimeException());
                    }
                }).subscribe(data -> System.out.println("data = " + data));
    }
}
