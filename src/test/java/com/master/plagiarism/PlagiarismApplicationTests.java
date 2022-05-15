package com.master.plagiarism;

import com.master.plagiarism.model.PlagiarismCheckerOutput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@FunctionalSpringBootTest("spring.main.web-application-type=reactive")
@AutoConfigureWebTestClient
class PlagiarismApplicationTests {

    @Autowired
    private WebTestClient client;

    @Test
    public void shouldParsePlagiarismInputsCorrectly() {
        final Flux<String> responseBody = client.post().uri("/").body(Mono.just("{ \"firstCodePiece\": " +
                        "\"int a = 1;\", \"secondCodePiece\": " +
                        "\"int b = 1;\", \"language\": \"CPP\" }"), String.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();


        System.out.println(responseBody);
    }

}
