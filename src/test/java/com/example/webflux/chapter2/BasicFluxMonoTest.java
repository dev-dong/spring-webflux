package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
public class BasicFluxMonoTest {

    @Test
    public void testBasicFluxMono() {
        // Flux의 흐름을 시작하는 방법은 2가지
        // 1. 첫 번째는 빈 함수로부터 시작
        // 2. 두 번째는 데이터로부터 시작
        Flux.<Integer>just(1, 2, 3, 4, 5) // 데이터로부터 시작
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Flux가 구독한 data! = " + data));
        // 1. just 데이터로부터 흐름을 시작했습니다.
        // 2. map과 filter 같은 연산자로 데이터를 가공했습니다.
        // 3. subscribe 하면서 데이터를 방출시켰습니다.

        // Mono 0개부터 1개의 데이터만 방출할 수 있는 객체 -> Optional 정도
        // Flux 0개 이상의 데이터를 방출할 수 있는 객체 -> List, Stream 0개 이상의 데이터 방출

        Mono.<Integer>just(2) // 데이터로부터 시작
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Mono가 구독한 data! = " + data));

        // Mono를 왜 쓰는걸까?? Flux를 사용하면 되는데...
        // 1개의 데이터를 다룰때 유지보수하기 용이하다.

    }

    @Test
    public void testFluxMonoBlock() {
        Mono<String> justString = Mono.just("String"); // Mono는 언제 만들어질지 모르는 비동기 객체이다.
        String string = justString.block(); // Mono String을 그냥 String으로 변환하려면?? -> 가장 간단한 방법은 데이터가 완성될때까지 기다린다.
        // block는 웬만하면 사용하지말자!
        System.out.println("string = " + string);
    }
}
