package com.example.webflux.chapter2;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class OperatorFlatMapTest {
    /**
     * Mono<Mono<T>> -> Mono<T>
     * Mono<Flux<T>> -> Flux<T>
     * Flux<Mono<T>> -> Flux<T>
     * mono.block()으로 mono 안의 객체를 꺼낼 수 있으나, 이건 mono 안의 객체가 방출 될 때 까지 스레드를 블로킹 시키는 것이기 때문에 절대 사용하면 안됨.
     */

    @Test
    public void monoToFlux() {
        Mono<Integer> one = Mono.just(1);
        // Mono랑 Flux가 같이 붙어있는 구조는 다소 이상하다.
        // Mono와 Flux 비동기 객체가 겹쳐있는게 뭘 의미할까?
        // 비동기 객체란 지금 당장 실행돼서 데이터가 생기는 객체가 아니라, 나중에 subscribe 돼서 언젠가 데이터를 방출하는 객체
        // 언제 만들어질지 모르는 객체 + 언제 만들어질 모르는 객체 = 언제 만들어질지 모르는 객체 -> Mono<Flux<Object>>
        // 비동기(Mono, Flux) + 비동기 = 비동기
        // 이렇게 비동기가 겹쳐진 구조를 비동기 1개로 평탄화 시켜주는 것이 FlatMap이다.
        Flux<Integer> integerFlux = one.flatMapMany(data -> {
            return Flux.just(data, data + 1, data + 2);
        });
        integerFlux.subscribe(data -> System.out.println("data = " + data));
    }

    @Test
    public void testWebClientFlatMap() {
        /**
         * flatMap에는 입력된 순서에 따른 방출순서를 보장하지 않는다.
         * flatMap 내부에서는 이전 흐름에서 데이터 처리가 완료돼서 먼저 방출되는 애를 바로 방출한다.
         * 입력순서가 아닌 데이터 방출순서이다. 처리속도가 빠른 순서대로 응답을 해준다.
         * 순서를 보장하고 싶다면 -> flatMapSequential을 사용하면 된다.
         */
        Flux<String> flatMap = Flux.just(callWebClient("1단계 - 문제 이해하기", 1500),
                        callWebClient("2단계 - 문제 단계별로 들어가기", 1000),
                        callWebClient("3단계 - 최종 응답", 500))
                .flatMap(monoData -> {
                    return monoData.map(data -> data + " 추가 가공!");
                });

        flatMap.subscribe(data -> System.out.println("FlatMapped data = " + data));

        Flux<String> flatMapSequential = Flux.just(callWebClient("1단계 - 문제 이해하기", 1500),
                        callWebClient("2단계 - 문제 단계별로 들어가기", 1000),
                        callWebClient("3단계 - 최종 응답", 500))
                .flatMapSequential(monoData -> {
                    return monoData;
                });

        flatMapSequential.subscribe(data -> System.out.println("FlatMappedSequential data = " + data));

        // 데이터 아무런 가공 안하고 return 할꺼면 flatMap 보다는 merge를 사용하는게 코드가 깔끔하다.
        Flux<String> merge = Flux.merge(callWebClient("1단계 - 문제 이해하기", 1500),
                callWebClient("2단계 - 문제 단계별로 들어가기", 1000),
                callWebClient("3단계 - 최종 응답", 500));
//                .map(~~~~~) 여기서 추가로 가공하면 flatMap이랑 비슷한 구조

        merge.subscribe(data -> System.out.println("merge data = " + data));

        Flux<String> mergeSequential = Flux.mergeSequential(callWebClient("1단계 - 문제 이해하기", 1500),
                callWebClient("2단계 - 문제 단계별로 들어가기", 1000),
                callWebClient("3단계 - 최종 응답", 500));

        mergeSequential.subscribe(data -> System.out.println("mergeSequential data = " + data));

        Mono<String> stringMono = Mono.just(Mono.just("안녕!")).flatMap(monoData -> monoData);

        /**
         * concat도 있다.
         * merge를 만나면 동시에 실행시키지만 concat을 만나면 처리가 완료되면 그다음 실행 순서에 의존해서 비효율적으로 동작한다.
         * mergeSequential, flatMapSequential는 한번에 다 실행한 후 마지막에 순서를 정렬하는 반면, concat는 처리가 완료될때까지 기다린다.
         * 사용안하는걸 추천
         */

        // concat, concatMap 이런건 쓰지말자.

        /**
         * Flux<Mono<T>>
         * Mono<Mono<T>> --> 이 구조 안에 있는 Mono는 flatMap, merge로 벗겨낼 수 있다.
         *               --> flatMap, merge 순서를 보장하지 않으니 순서 보장이 필요하면 sequential을 사용하자.
         * Mono<Flux<T>> --> flatMapMany --> 얘는 Flux<T> 순서가 보장된다.
         * Flux<Flux<T>> ? collectList --> Flux<Mono<List<T>> --> Flux<List<T>>
         */

        /**
         * flatMap을 쓰면 우리가 신경안써도 알아서 블로킹을 회피해주나??
         * 그건 아니다. callWebClient 함수에서 Schedulers를 빼버리면 flatMap을 사용해도 블로킹 회피가 안된다.
         */

        try {
            Thread.sleep(10000);
        } catch (Exception e) {

        }
    }

    public Mono<String> callWebClient(String request, long delay) {
        return Mono.defer(() -> {
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                return Mono.empty();
            }
            return Mono.just(request + " -> 딜레이: " + delay);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
