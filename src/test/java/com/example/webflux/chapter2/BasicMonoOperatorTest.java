package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class BasicMonoOperatorTest {

    // Mono로 부터 데이터를 시작할 수 있는 방법으로 just, empty가 있다.
    @Test
    public void startMonoFromData() {
        Mono.just(1).subscribe(data -> System.out.println("data = " + data));

        // ex> 사소한 에러가 발생했을 때 로그를 남기고 empty와 Mono를 전파
        Mono.empty().subscribe(data -> System.out.println("empty data = " + data));
    }

    // Mono는 함수로부터 시작할 수 있다. - fromCallable, defer

    /**
     * fromCallable -> 동기적인 객체를 반환할 때 사용
     * defer -> Mono를 반환하고 싶을 때 사용
     */
    @Test
    public void startMonoFromFunction() {
        Mono<String> monoFromCallable = Mono.fromCallable(() -> {
            // 우리 로직을 실행하고
            return callRestTemplate("안녕!");
        }).subscribeOn(Schedulers.boundedElastic());
        /**
         * 임시 마이그레이션
         * restTemplate, JPA >> 블로킹이 발생하는 라이브러리 Mono로 스레드 분리하여 처리
         */

        /**
         * Mono 객체를 Mono 객체로 반환하고 있다.
         * defer과 just의 차이점은 just는 완성된 데이터가 들어간다.
         * just는 subscribe를 하지 않아도 "안녕!" 이라는 데이터가 완성돼서 들어간다.
         * 스레드가 defer를 만나도 Mono.just 코드에는 도달하지 않는다. 스레드가 Mono.just에 도달하는 순간은 Mono.defer를 subscribe 했을때다.
         * 두 개의 차이점은 Mono.just 코드에 스레드가 언제 도달하는지 차이가 있다. 결과적으로 데이터가 언제 만들어지는지 차이점
         */
        Mono<String> monoFromDefer = Mono.defer(() -> {
            return callWebClient("안녕!");
        });
        monoFromDefer.subscribe();

        Mono<String> monoFromJust = callWebClient("안녕!");
    }

    @Test
    public void testDeferNecessity() {
        // abc를 만드는 로직도 Mono의 흐름 안에서 관리하고 싶다!
        // 하나의 큰 흐름을 하나의 Mono 로직 안에서 관리하고 싶을때 defer를 사용한다.
        Mono<String> stringMono = Mono.defer(() -> {
            String a = "안녕";
            String b = "하세"; // blocking! 만약 발생하면?
            String c = "요";
            return callWebClient(a + b + c);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> callWebClient(String request) {
        return Mono.just(request + "callWebClient");
    }

    public String callRestTemplate(String request) {
        return request + "callRestTemplate 응답";
    }

    /**
     * Mono의 흐름 시작 방법
     * 1. 데이터로부터 시작 -> 일반적인 경우 just / 특이한 상황 empty (Optional.empty())
     * 2. 함수로부터 시작
     * -> 동기적인 객체를 Mono로 반환하고 싶을 때 fromCallable / 코드의 흐름을 Mono 안에서 관리하면서 Mono를 반환하고 싶을 때 defer
     */
    @Test
    public void testBasicFluxMono() {
        Flux.<Integer>just(1, 2, 3, 4, 5)
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Flux가 구독한 data! = " + data));

        Mono.<Integer>just(2)
                .map(data -> data * 2)
                .filter(data -> data % 4 == 0)
                .subscribe(data -> System.out.println("Mono가 구독한 data! = " + data));
    }
    // 흐름 시작 / 데이터 가공 / 구독

    // mono에서 데이터 방출의 개수가 많아져서 Flux로 바꾸소 싶다 -> flatMapMany 사용
    @Test
    public void monoToFlux() {
        Mono<Integer> one = Mono.just(1);
        // 하나의 데이터 흐름에서 여러개의 데이터 흐름으로 바꾸고 싶을때 flatMapMany 사용
        Flux<Integer> integerFlux = one.flatMapMany(data -> {
            return Flux.just(data, data + 1, data + 2);
        });
        integerFlux.subscribe(data -> System.out.println("data = " + data));
    }
}
