package com.master.plagiarism;

import com.master.plagiarism.model.PlagiarismCheckerOutput;
import com.master.plagiarism.model.SourceCodeCompareEntity;
import com.master.plagiarism.service.utils.similirityFinder.SimilarityFinder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

@FunctionalSpringBootTest("spring.main.web-application-type=reactive")
@AutoConfigureWebTestClient
class PlagiarismApplicationTests {

    @Autowired
    private WebTestClient client;

    @Test
    public void shouldParsePlagiarismInputsCorrectly() {
        final SourceCodeCompareEntity sourceCodeCompareEntity = new SourceCodeCompareEntity(
                "int a = 1;",
                "int b = 1",
                SimilarityFinder.Lang.CPP
        );

        client.post().uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(sourceCodeCompareEntity))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PlagiarismCheckerOutput.class);

    }

    @Test
    public void shouldParseMultipleEntitiesCorrectly() {
        final List<SourceCodeCompareEntity> list = List.of(
                new SourceCodeCompareEntity("a", "b", SimilarityFinder.Lang.JAVA),
                new SourceCodeCompareEntity("int a =b;", "int c = b;", SimilarityFinder.Lang.JAVA),
                new SourceCodeCompareEntity("int a =b;", "int c = b;", SimilarityFinder.Lang.CPP)
        );

        client.post().uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(list))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PlagiarismCheckerOutput.class)
                .hasSize(3);
    }

}
