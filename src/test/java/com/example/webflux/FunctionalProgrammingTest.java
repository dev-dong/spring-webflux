package com.example.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

@SpringBootTest
public class FunctionalProgrammingTest {

    // test는 항상 void를 반환해야한다.
    @Test
    public void produceOneToNineFlux() {
        List<Integer> sink = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            sink.add(i);
        }

        // *2를 전부 해주고 싶다.
        sink = map(sink, (data) -> data * 4);
        // 4의 배수들만 남겨두고 싶다.
        sink = filter(sink, (data) -> data % 4 == 0);
        forEach(sink, (data) -> System.out.println(data));
    }

    @Test
    public void produceOneToNineStream() {
        IntStream.rangeClosed(1, 9).boxed()
                .map((data) -> data * 4)
                .filter((data) -> data % 4 == 0)
                .forEach((data) -> System.out.println(data)); // 최종연산 필수 : collect, foreach, min, max
    }

    // Flux 안에 함수를 실행시킬려면, Flux를 구독해야 안의 코드가 실행된다. Stream 종결함수가 필요한거 처럼
    @Test
    public void produceOneToNineFluxSubscribe() {
        Flux<Integer> intFlux = Flux.create(sink -> {
            for (int i = 1; i <= 9; i++) {
                sink.next(i);
            }
            sink.complete();
        });

        intFlux.subscribe(data -> System.out.println("WebFlux가 구독 중!! : " + data));
        System.out.println("Netty 이벤트 루프로 스레드 복귀 !!");
    }

    @Test
    public void produceOneToNineFluxOperator() {
        Flux.fromIterable(IntStream.rangeClosed(1, 9).boxed().toList())
                .map((data) -> data * 4) // Flux의 operator는 대부분이 stream과 유사하게 동작
                .filter((data) -> data % 4 == 0)
                .subscribe((data) -> System.out.println(data));
    }

    private void forEach(List<Integer> sink, Consumer<Integer> consumer) {
        for (Integer integer : sink) {
            consumer.accept(integer);
        }
    }

    private List<Integer> filter(List<Integer> sink, Function<Integer, Boolean> predicate) {
        List<Integer> newSink2 = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (predicate.apply(sink.get(i))) { // 4의 배수 일 때만 리스트에 추가
                newSink2.add(sink.get(i));
            }
        }
        sink = newSink2;
        return sink;
    }

    private List<Integer> map(List<Integer> sink, Function<Integer, Integer> mapper) {
        List<Integer> newSink = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            newSink.add(mapper.apply(sink.get(i)));
        }
        sink = newSink;
        return sink;
    }
}
