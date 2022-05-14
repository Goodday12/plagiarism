package com.master.plagiarism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@SpringBootApplication
public class PlagiarismApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlagiarismApplication.class, args);
	}

	@Bean
	public Function<Flux<String>, Flux<String>> uppercase() {
		return flux -> flux.map(String::toUpperCase);
	}git

}
