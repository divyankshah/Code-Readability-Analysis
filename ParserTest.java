package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.body.BodyDeclaration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ParserTest {

    @Test
    void testParseJavaSnippet_validMethod() {
        String validMethodSnippet = "public void myMethod() { int x = 10; }";
        try {
            BodyDeclaration<?> result = Parser.parseJavaSnippet(validMethodSnippet);
            assertNotNull(result, "Parsed result should not be null for a valid method snippet.");
            assertTrue(result.isMethodDeclaration(), "Result should be a MethodDeclaration.");
            // You can add more assertions here, e.g., check method name, return type, etc.
        } catch (ParseException e) {
            fail("Parsing a valid method snippet should not throw a ParseException: " + e.getMessage());
        }
    }

    @Test
    void testParseJavaSnippet_validField() {
        String validFieldSnippet = "private String name = \"test\";";
        try {
            BodyDeclaration<?> result = Parser.parseJavaSnippet(validFieldSnippet);
            assertNotNull(result, "Parsed result should not be null for a valid field snippet.");
            assertTrue(result.isFieldDeclaration(), "Result should be a FieldDeclaration.");
        } catch (ParseException e) {
            fail("Parsing a valid field snippet should not throw a ParseException: " + e.getMessage());
        }
    }

    @Test
    void testParseJavaSnippet_validConstructor() {
        String validConstructorSnippet = "public MyClass() { this.x = 0; }";
        try {
            BodyDeclaration<?> result = Parser.parseJavaSnippet(validConstructorSnippet);
            assertNotNull(result, "Parsed result should not be null for a valid constructor snippet.");
            assertTrue(result.isConstructorDeclaration(), "Result should be a ConstructorDeclaration.");
        } catch (ParseException e) {
            fail("Parsing a valid constructor snippet should not throw a ParseException: " + e.getMessage());
        }
    }

    @Test
    void testParseJavaSnippet_invalidSnippetSyntaxError() {
        String invalidSnippet = "public void myMethod() { int x = ; }"; // Syntax error
        // Expect a ParseException to be thrown for invalid syntax
        assertThrows(ParseException.class, () -> Parser.parseJavaSnippet(invalidSnippet),
                "Parsing an invalid snippet should throw a ParseException.");
    }

    @Test
    void testParseJavaSnippet_emptyString() {
        String emptySnippet = "";
        // JavaParser might handle empty strings differently depending on ParseStart,
        // but for CLASS_BODY, it's likely to fail.
        assertThrows(ParseException.class, () -> Parser.parseJavaSnippet(emptySnippet),
                "Parsing an empty string should throw a ParseException.");
    }

    @Test
    void testParseJavaSnippet_incompleteSnippet() {
        String incompleteSnippet = "public int calculate(int a"; // Missing closing parenthesis and body
        assertThrows(ParseException.class, () -> Parser.parseJavaSnippet(incompleteSnippet),
                "Parsing an incomplete snippet should throw a ParseException.");
    }

    @Test
    void testParseJavaSnippet_onlyComment() {
        String commentSnippet = "// This is a comment only";
        // An empty class body after removing comments is often considered unparsable for CLASS_BODY
        assertThrows(ParseException.class, () -> Parser.parseJavaSnippet(commentSnippet),
                "Parsing only comments should throw a ParseException for a class body context.");
    }
}