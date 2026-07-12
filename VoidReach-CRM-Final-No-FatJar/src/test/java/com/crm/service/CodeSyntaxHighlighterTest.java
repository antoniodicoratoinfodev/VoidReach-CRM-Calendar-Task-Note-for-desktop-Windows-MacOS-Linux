package com.crm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CodeSyntaxHighlighterTest {
    @Test void classifiesCommonCodeElementsWithoutChangingSource() {
        String source = "String name = format(42); // comment";
        var tokens = CodeSyntaxHighlighter.highlight(source);
        String reconstructed = tokens.stream().map(CodeSyntaxHighlighter.Token::text).collect(Collectors.joining());
        Map<String, CodeSyntaxHighlighter.TokenKind> byText = tokens.stream()
                .filter(token -> !token.text().isBlank())
                .collect(Collectors.toMap(CodeSyntaxHighlighter.Token::text,
                        CodeSyntaxHighlighter.Token::kind, (first, ignored) -> first));

        assertEquals(source, reconstructed);
        assertEquals(CodeSyntaxHighlighter.TokenKind.TYPE, byText.get("String"));
        assertEquals(CodeSyntaxHighlighter.TokenKind.VARIABLE, byText.get("name"));
        assertEquals(CodeSyntaxHighlighter.TokenKind.FUNCTION, byText.get("format"));
        assertEquals(CodeSyntaxHighlighter.TokenKind.NUMBER, byText.get("42"));
        assertEquals(CodeSyntaxHighlighter.TokenKind.COMMENT, byText.get("// comment"));
        assertTrue(tokens.stream().anyMatch(token -> token.kind() == CodeSyntaxHighlighter.TokenKind.OPERATOR));
    }

    @Test void convertsLegacyMultilineInlineCodeToAFencedBlock() {
        String legacy = "`package com.crm.app;\npublic class AppLauncher {}\n`";

        String normalized = CodeSyntaxHighlighter.normalizeMultilineBackticks(legacy);

        assertEquals("```\npackage com.crm.app;\npublic class AppLauncher {}\n```", normalized);
    }
}
