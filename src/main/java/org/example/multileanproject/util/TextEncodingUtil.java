package org.example.multileanproject.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class TextEncodingUtil {

    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile(
            "(?:\\u00C3[\\u0080-\\u024F]|\\u00C2[\\u0080-\\u024F]|\\u00C4[\\u0080-\\u024F]|\\u00C5[\\u0080-\\u024F]|" +
                    "\\u00C6[\\u0080-\\u024F]|\\u00D0[\\u0080-\\u024F]|\\u00F0[\\u0080-\\u024F]|\\u00E1[\\u0080-\\u024F]|" +
                    "\\u00E0[\\u0080-\\u024F]|\\u00E2[\\u0080-\\u024F]|\\u00E3[\\u0080-\\u024F]|\\u00EF\\u00BF\\u00BD|" +
                    "\\uFFFD|[\\u0080-\\u009F])"
    );
    private static final Pattern VIETNAMESE_CHAR_PATTERN = Pattern.compile("[\\u00C0-\\u1EF9\\u0110\\u0111]");
    private static final int MAX_DECODE_PASSES = 4;
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private TextEncodingUtil() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (!looksLikeMojibake(value)) {
            return value;
        }

        String globalBest = pickBest(buildDecodeCandidates(value));
        String segmentedBest = decodeBySegments(value);
        return better(globalBest, segmentedBest);
    }

    private static boolean looksLikeMojibake(String value) {
        return value != null && MOJIBAKE_PATTERN.matcher(value).find();
    }

    private static List<String> buildDecodeCandidates(String value) {
        List<String> candidates = new ArrayList<>();
        candidates.add(value);

        String current = value;
        for (int pass = 0; pass < MAX_DECODE_PASSES; pass++) {
            String decoded = decodeLatin1AsUtf8(current);
            if (decoded.equals(current)) {
                break;
            }
            candidates.add(decoded);
            current = decoded;
            if (!looksLikeMojibake(decoded)) {
                break;
            }
        }
        return candidates;
    }

    private static String decodeBySegments(String value) {
        StringBuilder output = new StringBuilder(value.length());
        int index = 0;

        while (index < value.length()) {
            int start = index;
            while (index < value.length() && !Character.isWhitespace(value.charAt(index))) {
                index++;
            }

            String segment = value.substring(start, index);
            output.append(decodeSegmentSafely(segment));

            while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
                output.append(value.charAt(index));
                index++;
            }
        }

        return output.toString();
    }

    private static String decodeSegmentSafely(String segment) {
        if (segment == null || segment.isBlank() || !looksLikeMojibake(segment)) {
            return segment;
        }

        String decoded = pickBest(buildDecodeCandidates(segment));

        int originalQuestionMarks = questionMarkCount(segment);
        int decodedQuestionMarks = questionMarkCount(decoded);
        if (decodedQuestionMarks > originalQuestionMarks + 1
                && vietnameseScore(decoded) <= vietnameseScore(segment)) {
            return segment;
        }

        return decoded;
    }

    private static String decodeLatin1AsUtf8(String value) {
        String decodedWindows1252 = new String(value.getBytes(WINDOWS_1252), StandardCharsets.UTF_8);
        String decodedLatin1 = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return better(decodedWindows1252, decodedLatin1);
    }

    private static String pickBest(List<String> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        String best = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            best = better(best, candidates.get(i));
        }
        return best;
    }

    private static String better(String currentBest, String candidate) {
        int bestScore = mojibakeScore(currentBest);
        int candidateScore = mojibakeScore(candidate);

        if (candidateScore < bestScore) {
            return candidate;
        }
        if (candidateScore == bestScore && vietnameseScore(candidate) > vietnameseScore(currentBest)) {
            return candidate;
        }
        return currentBest;
    }

    private static int mojibakeScore(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        int replacementChars = (int) value.codePoints().filter(cp -> cp == 0xFFFD).count();
        int replacementSequences = countOccurrences(value, "\uFFFD");
        int replacement = (replacementChars + replacementSequences) * 8;
        int artifacts = (int) MOJIBAKE_PATTERN.matcher(value).results().count();
        int questionPenalty = Math.max(0, questionMarkCount(value) - 1) * 4;
        return replacement + artifacts + questionPenalty;
    }

    private static int vietnameseScore(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return (int) VIETNAMESE_CHAR_PATTERN.matcher(value).results().count();
    }

    private static int countOccurrences(String text, String token) {
        if (text == null || text.isBlank() || token == null || token.isBlank()) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(token, idx)) >= 0) {
            count++;
            idx += token.length();
        }
        return count;
    }

    private static int questionMarkCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return (int) text.chars().filter(ch -> ch == '?').count();
    }
}
