package com.master.plagiarism.model;

import com.master.plagiarism.service.utils.similirityFinder.SimilarityFinder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceCodeCompareEntity {

    private String firstCodePiece;
    private String secondCodePiece;
    private SimilarityFinder.Lang language;

}
