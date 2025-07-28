package com.example.webflux.chapter1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootTest
public class SubscriberPublisherAsyncTest {

    // 한글로 구독자 - 발행자
    // Flux를 구독하면 발행이 시작된다.
    // 이 간단한 개념이 Subscriber - Publisher 패턴이다.
    @Test
    public void produceOneToNineFlux() {
        Flux<Integer> intFlux = Flux.<Integer>create(sink -> {
            for (int i = 1; i <= 9; i++) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                sink.next(i);
            }
            sink.complete();
        }).subscribeOn(Schedulers.boundedElastic());

        // 스레드 1개만 사용해서는 절대로 블로킹을 회피할 수 없다.
        // Reactor의 스케쥴러를 사용해서 스레드를 추가 할당해야지만 블로킹 회피가 가능하다.
        // 스케쥴러가 제공하는 스레드는 톰켓의 스레드처럼 어느정도 블로킹 되어도 괜찮다.
        // 이 스레드는 우리가 원하는 곳에 마음대로 사용할 수 있다.
        // 스케쥴러가 제공하는 스레드가 중요한 스레드 (이벤트 루프 스레드) 대신 대기 하는것이 블로킹 회피의 기본적인 전략이다.
        intFlux.subscribe(data -> {
            System.out.println("처리 되고 있는 스레드 이름 : " + Thread.currentThread().getName());
            System.out.println("WebFlux가 구독 중!! : " + data);
        });
        System.out.println("Netty 이벤트 루프로 스레드 복귀 !!");

        // 테스트 환경이라 메인 스레드는 자기 할 일을 다하면 죽어버린다.
        // 메인 스레드가 살아 있어야지만 다른 스레드들도 일을 하고 있을 수 있다.
        // 그래서 테스트를 위해 메인 스레드를 잠시 잡아준다.
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
    }

    // 정리
    /**
     * 1. Flux에는 데이터가 들어 있는게 아니라 함수 코드 덩어리가 들어있다.
     * 그래서 함수를 실행시키는 순간 부터 데이터가 발행된다.
     * 2. 스레드가 1개라면 Flux만으로는 어떻게 해도 블로킹을 회피할 수 없다.
     * 스케쥴러로 추가 스레드를 할당하여 대신 작업시켜야 한다.
     */

}
