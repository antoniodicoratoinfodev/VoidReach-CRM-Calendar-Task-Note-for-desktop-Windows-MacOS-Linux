package com.crm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lightweight, language-agnostic lexer used for live note and preview highlighting. */
public final class CodeSyntaxHighlighter {
    private static final Set<String> KEYWORDS = Set.of(
            "abstract", "async", "await", "break", "case", "catch", "class", "const", "continue",
            "def", "default", "delete", "do", "else", "enum", "export", "extends", "false", "final",
            "finally", "for", "from", "function", "if", "implements", "import", "in", "instanceof",
            "interface", "lambda", "let", "match", "new", "null", "package", "pass", "private",
            "protected", "public", "raise", "return", "select", "static", "super", "switch", "this",
            "throw", "throws", "true", "try", "typeof", "var", "void", "while", "with", "yield",
            "and", "or", "not", "as", "is", "where", "join", "insert", "update", "into", "values");
    private static final Set<String> TYPES = Set.of(
            "boolean", "bool", "byte", "char", "decimal", "double", "float", "int", "integer", "long",
            "number", "object", "short", "string", "str", "uint", "ulong", "ushort", "list", "map",
            "set", "dict", "array", "date", "datetime", "record", "optional", "any", "unknown");
    private static final Pattern TOKEN = Pattern.compile(
            "(?<SPACE>\\s+)|(?<COMMENT>//.*|/\\*.*?\\*/|#.*)|"
                    + "(?<STRING>\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*')|"
                    + "(?<NUMBER>\\b(?:0[xX][0-9a-fA-F]+|\\d+(?:\\.\\d+)?)\\b)|"
                    + "(?<ANNOTATION>@[A-Za-z_$][\\w$]*)|(?<IDENTIFIER>[A-Za-z_$][\\w$]*)|"
                    + "(?<OPERATOR>=>|==={0,1}|!==|!=|<=|>=|\\+\\+|--|&&|\\|\\||[+\\-*/%=<>!&|?:.])|"
                    + "(?<OTHER>.)");
    private static final Pattern MULTILINE_SINGLE_BACKTICK = Pattern.compile(
            "(?s)(?<!`)`([^`]*\\R[^`]*)`(?!`)");

    private CodeSyntaxHighlighter() { }

    public static List<Token> highlight(String source) {
        String value = source == null ? "" : source;
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(value);
        TokenKind previousSignificant = TokenKind.PLAIN;
        while (matcher.find()) {
            String text = matcher.group();
            TokenKind kind;
            if (matcher.group("SPACE") != null) kind = TokenKind.PLAIN;
            else if (matcher.group("COMMENT") != null) kind = TokenKind.COMMENT;
            else if (matcher.group("STRING") != null) kind = TokenKind.STRING;
            else if (matcher.group("NUMBER") != null) kind = TokenKind.NUMBER;
            else if (matcher.group("ANNOTATION") != null) kind = TokenKind.ANNOTATION;
            else if (matcher.group("OPERATOR") != null) kind = TokenKind.OPERATOR;
            else if (matcher.group("IDENTIFIER") != null) {
                String normalized = text.toLowerCase(Locale.ROOT);
                if (KEYWORDS.contains(normalized)) kind = TokenKind.KEYWORD;
                else if (TYPES.contains(normalized) || Character.isUpperCase(text.charAt(0))) kind = TokenKind.TYPE;
                else if (nextNonSpace(value, matcher.end()) == '(') kind = TokenKind.FUNCTION;
                else kind = previousSignificant == TokenKind.TYPE ? TokenKind.VARIABLE : TokenKind.VARIABLE;
            } else kind = TokenKind.PLAIN;
            tokens.add(new Token(text, kind));
            if (!text.isBlank()) previousSignificant = kind;
        }
        return List.copyOf(tokens);
    }

    /** Treats legacy single-backtick spans containing line breaks as fenced Markdown code blocks. */
    public static String normalizeMultilineBackticks(String markdown) {
        String source = markdown == null ? "" : markdown;
        Matcher matcher = MULTILINE_SINGLE_BACKTICK.matcher(source);
        StringBuffer normalized = new StringBuffer();
        while (matcher.find()) {
            String code = matcher.group(1);
            String opening = startsWithLineBreak(code) ? "```" : "```\n";
            String closing = endsWithLineBreak(code) ? "```" : "\n```";
            matcher.appendReplacement(normalized, Matcher.quoteReplacement(opening + code + closing));
        }
        matcher.appendTail(normalized);
        return normalized.toString();
    }

    private static boolean startsWithLineBreak(String value) {
        return value.startsWith("\n") || value.startsWith("\r");
    }

    private static boolean endsWithLineBreak(String value) {
        return value.endsWith("\n") || value.endsWith("\r");
    }

    private static char nextNonSpace(String value, int from) {
        for (int index = from; index < value.length(); index++) {
            if (!Character.isWhitespace(value.charAt(index))) return value.charAt(index);
        }
        return '\0';
    }

    public record Token(String text, TokenKind kind) { }

    public enum TokenKind {
        PLAIN, COMMENT, STRING, NUMBER, KEYWORD, TYPE, VARIABLE, FUNCTION, ANNOTATION, OPERATOR
    }
}
