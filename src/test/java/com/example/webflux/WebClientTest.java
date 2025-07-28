package com.example.webflux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootTest
public class WebClientTest {

    private WebClient webClient = WebClient.builder().build();

    @Test
    public void testWebClient() {
        Flux<Integer> intFlux = webClient.get()
                .uri("http://localhost:8080/reactive/onenine/flux")
                .retrieve()
                .bodyToFlux(Integer.class);

        intFlux.subscribe(data -> {
            System.out.println("처리 되고 있는 스레드 이름 : " + Thread.currentThread().getName());
            System.out.println("WebFlux가 구독 중!! : " + data);
        });
        /**
         * WebFlux로는 스케쥴러를 사용하는 게 아닌 이상 무슨 짓을 해도 스레드의 흐름을 조작할 수 없는데 retrieve라는 거는 요청을 보내는 거니까 시간이 걸리는 작업이다.
         * 스레드가 어떻게 자기 마음대로 Flux는 실행안시키고 바로 System.out.println("Netty 이벤트 루프로 스레드 복귀 !!"); 실행시킨걸까?
         * WebClient가 반환하는 Flux는 FluxReceive라는 클래스를 사용한다.
         * FluxReceive를 사용하고 웹 클라이언트의 내부적인 코드 구현이 합쳐지면 이 요청을 받아오는 Retrieve라는 블로킹 동작을 할 때 이 블로킹 대기는 OS한테 위임을 한다.
         * Retrieve 이벤트가 완료되면 subscribe 뒤에 붙은 Callback 함수를 Netty가 Event Loop Thread를 할당해서 처리하는 구조이다.
         * 즉, WebClient를 사용하면 스케쥴러 스레드의 지원 없이도 완벽하게 비동기로 블로킹 회피가 가능하다.
         * WebClient를 사용하면 스케쥴러 스레드의 지원 없이도 완벽하게 비동기로 블로킹 회피가 가능하다.
         * 이걸 사용하면 스케쥴러를 따로 할당해야하는 문제도 해결된다.
         * 그러면 스케쥴러는 언제 사용??
         * 1. 어쩔 수 없이 블로킹이 발생하는 요소 ex> Thread.sleep()
         * 2. 마땅한 라이브러리가 없는 I/O 작업 ex> DB쪽의 R2DBC, JPA를 사용 시 스케쥴러(boundedElastic)를 사용하여 스레드를 분리
         *      - 중요한 작업(회원 가입, 로그인 등) 시 발생하는 모든 작업 -> 스레드 분리하여 JPA 활용
         *      - 실시간으로 저장할 필요는 없는 데이터 -> MQ 활용
         *      - 신뢰성이 떨어져도 되는 조회 -> R2DBC 활용
         * 3. 병렬처리를 하고 싶을 때
         * WebFlux의 블로킹 처리 원칙!
         * 1. R2DBC, WebClient 등을 이용해 I/O 블로킹을 가능한한 최소화 해야 한다.
         * 2. 어쩔 수 없이 블로킹이 발생 하는 요소, 병렬 처리, 오래 걸리는 작업은 Scheduler을 이용해 스레드를 분리한다.
         *      - 오래 걸린는 작업은 응답까지 오랜 시간이 아니라 이벤트루프 스레드가 직접 CPU를 사용해서 해야 할 일이 너무 많은 상황 - CPU 점유를 오래 하는 작업
         * 3. 리액티브 프로그래밍의 기본 원칙은 스레드 변경 최소화다. 불필요한 스레드 분리는 삼가해야한다.
         */
        System.out.println("Netty 이벤트 루프로 스레드 복귀 !! " + Thread.currentThread().getName());

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
    }
}
