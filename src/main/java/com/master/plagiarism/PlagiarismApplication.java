package com.master.plagiarism;

import com.master.plagiarism.model.PlagiarismCheckerOutput;
import com.master.plagiarism.model.SourceCodeCompareEntity;
import com.master.plagiarism.service.utils.similirityFinder.SimilarityFinder;
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
	public Function<Flux<SourceCodeCompareEntity>, Flux<PlagiarismCheckerOutput>> checkPlagiarism(){
		return sourceCodeCompareEntityFlux -> sourceCodeCompareEntityFlux.map(SimilarityFinder.getInstance()::findSimilarity);
	}

}
