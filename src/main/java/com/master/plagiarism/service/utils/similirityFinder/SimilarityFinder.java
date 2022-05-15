package com.master.plagiarism.service.utils.similirityFinder;

import com.master.plagiarism.model.PlagiarismCheckerOutput;
import com.master.plagiarism.model.SourceCodeCompareEntity;
import com.master.plagiarism.model.SupportedLanguage;
import com.master.plagiarism.service.utils.antlr.charp.CSharpLexer;
import com.master.plagiarism.service.utils.antlr.cpp.CPP14Lexer;
import com.master.plagiarism.service.utils.antlr.java.Java9Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimilarityFinder {

    private static class SingletonHelper {
        public static final SimilarityFinder INSTANCE = new SimilarityFinder();
    }

    private SimilarityFinder() {}

    public static SimilarityFinder getInstance() {
        return SingletonHelper.INSTANCE;
    }

    static class LangSpec {
        int intLiteral;
        int floatLiteral;
        int charLiteral;
        int stringLiteral;
        int comment;
        int lineComment;
        int maxId;

        public LangSpec(int intLiteral, int floatLiteral,
                        int charLiteral, int stringLiteral,
                        int maxId, int comment, int lineComment) {
            this.intLiteral = intLiteral;
            this.floatLiteral = floatLiteral;
            this.charLiteral = charLiteral;
            this.stringLiteral = stringLiteral;
            this.maxId = maxId;
            this.comment = comment;
            this.lineComment = lineComment;
        }
    }

    public enum Lang {
        JAVA, CPP, CSHARP,
    }

    private static final LangSpec javaSpec = new LangSpec(62, 63, 65, 66, 120, 117, 118);
    private static final LangSpec cppSpec = new LangSpec(134, 141, 140, 142, 151, 149, 150);
    private static final LangSpec csharpSpec = new LangSpec(114, 116, 117, 118, 195, 4, 5);


    private final Map<String, Integer> stringMap = new HashMap<>();
    private final Map<String, Integer> intMap = new HashMap<>();
    private final Map<String, Integer> floatMap = new HashMap<>();
    private final Map<String, Integer> charMap = new HashMap<>();

    private List<? extends Token> tokens1;
    private List<? extends Token> tokens2;

    private LangSpec getLangSpec(Lang lang) {
        return switch (lang) {
            case JAVA -> javaSpec;
            case CPP -> cppSpec;
            case CSHARP -> csharpSpec;
        };
    }

    private MatchingBlock decode(EqualsBlock block) {
        String first = decodeBlock(block.start1, block.finish1, tokens1);
        String second = decodeBlock(block.start2, block.finish2, tokens2);
        return new MatchingBlock(first, second);
    }

    private String decodeBlock(int start, int end, List<? extends Token> tokens) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i <= end; i++) {
            builder.append(tokens.get(i).getText()).append(" ");
        }
        return builder.toString();
    }


    public PlagiarismCheckerOutput findSimilarity(SourceCodeCompareEntity sourceCodeCompareEntity) {
        PlagiarismCheckerOutput checkResult = new PlagiarismCheckerOutput(
                0.f, new ArrayList<>()
        );
        LangSpec spec = getLangSpec(sourceCodeCompareEntity.getLanguage());

        tokens1 = findTokens(sourceCodeCompareEntity.getFirstCodePiece(), spec, sourceCodeCompareEntity.getLanguage());
        tokens2 = findTokens(sourceCodeCompareEntity.getSecondCodePiece(), spec, sourceCodeCompareEntity.getLanguage());
        Map<String, Integer> encodingMap = fillMaps(tokens1, tokens2, spec);
        List<Integer> encoding1 = getEncoding(tokens1, encodingMap);
        List<Integer> encoding2 = getEncoding(tokens2, encodingMap);

        EncodedSequenceMatcher matcher = new EncodedSequenceMatcher(encoding1, encoding2);
        List<EqualsBlock> blocks = matcher.getRate();
        blocks.stream().map(this::decode).forEach(checkResult.getMatchingBlocks()::add);

        int plagLength = blocks.stream().mapToInt(EqualsBlock::lengthSecond).sum();
        checkResult.setRate((float) ((2.0 * plagLength) / (encoding1.size() + encoding2.size())));
        return checkResult;
    }

    private Map<String, Integer> fillMaps(List<? extends Token> tokens1, List<? extends Token> tokens2, LangSpec spec) {
        int maxId = spec.maxId;
        maxId = fillMap(tokens1, spec.intLiteral, maxId, intMap);
        maxId = fillMap(tokens2, spec.intLiteral, maxId, intMap);
        maxId = fillMap(tokens1, spec.floatLiteral, maxId, floatMap);
        maxId = fillMap(tokens2, spec.floatLiteral, maxId, floatMap);
        maxId = fillMap(tokens1, spec.charLiteral, maxId, charMap);
        maxId = fillMap(tokens2, spec.charLiteral, maxId, charMap);
        maxId = fillMap(tokens1, spec.stringLiteral, maxId, stringMap);
        maxId = fillMap(tokens2, spec.stringLiteral, maxId, stringMap);
        Map<String, Integer> encodingMap = new HashMap<>();
        encodingMap.putAll(intMap);
        encodingMap.putAll(floatMap);
        encodingMap.putAll(charMap);
        encodingMap.putAll(stringMap);
        for (Token token : tokens1) {
            if (token.getType() != spec.intLiteral && token.getType() != spec.floatLiteral && token.getType() != spec.charLiteral && token.getType() != spec.stringLiteral) {
                encodingMap.put(token.getText(), token.getType());
            }
        }
        for (Token token : tokens2) {
            if (token.getType() != spec.intLiteral && token.getType() != spec.floatLiteral && token.getType() != spec.charLiteral && token.getType() != spec.stringLiteral) {
                encodingMap.put(token.getText(), token.getType());
            }
        }
        return encodingMap;
    }

    private int fillMap(List<? extends Token> tokens, int tokenType, int maxId, Map<String, Integer> output) {
        for (Token token : tokens) {
            if (token.getType() == tokenType) {
                if (!output.containsKey(token.getText())) {
                    output.put(token.getText(), ++maxId);
                }
            }
        }
        return maxId;
    }

    private List<? extends Token> findTokens(String code, LangSpec spec, Lang lang) {
        CharStream input = CharStreams.fromString(code);
        Lexer lexer = switch (lang) {
            case JAVA -> new Java9Lexer(input);
            case CPP -> new CPP14Lexer(input);
            case CSHARP -> new CSharpLexer(input);
        };
        List<? extends Token> tokens = lexer.getAllTokens();
        return tokens.stream().filter(t -> t.getType() != spec.lineComment && t.getType() != spec.comment).collect(Collectors.toList());
    }

    private <T, S> void putAll(Map<T, S> source, Map<S, T> target) {
        for (Map.Entry<T, S> entry : source.entrySet()) {
            target.put(entry.getValue(), entry.getKey());
        }
    }

    private List<Integer> getEncoding(List<? extends Token> tokens, Map<String, Integer> encodingMap) {
        List<Integer> result = new ArrayList<>();
        Map<Integer, String> decodingMap = new HashMap<>();
        putAll(encodingMap, decodingMap);

        for (Token token : tokens) {
            String tokenText = token.getText();
            result.add(encodingMap.get(tokenText));
        }
        return result;
    }

}
