package com.master.plagiarism.model;

import com.master.plagiarism.service.utils.similirityFinder.MatchingBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlagiarismCheckerOutput {

    private Float rate;
    private List<MatchingBlock> matchingBlocks;

}

