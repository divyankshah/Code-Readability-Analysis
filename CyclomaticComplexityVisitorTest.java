package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CyclomaticComplexityVisitorTest {

    private JavaParser parser;

    @BeforeEach
    public void setUp() {
        parser = new JavaParser();
    }

    /**
     * Parses code, applies CyclomaticComplexityVisitor to the first method found.
     */
    private int computeComplexity(String methodCode) {
        String classWrapper = "class Test { " + methodCode + " }";
        CompilationUnit cu = parser.parse(classWrapper).getResult().orElseThrow();
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();
        method.accept(visitor, null);
        return visitor.getComplexity();
    }

    @Test
    public void testSimpleMethod_NoControlFlow() {
        String code = "void simple() { int x = 0; x++; }";
        assertEquals(1, computeComplexity(code), "Base complexity should be 1");
    }

    @Test
    public void testIfStatement() {
        String code = "void test() { if (true) {} }";
        assertEquals(2, computeComplexity(code));
    }

    @Test
    public void testLoops() {
        String code = """
            void test() {
                for (int i = 0; i < 10; i++) {}
                while (true) {}
                do {} while (false);
                for (String s : list) {}
            }
            """;
        assertEquals(5, computeComplexity(code), "Each loop adds 1 to base 1");
    }

    @Test
    public void testSwitchStatement() {
        String code = """
            void test(int x) {
                switch (x) {
                    case 1: break;
                    case 2: break;
                    default: break;
                }
            }
            """;
        assertEquals(3, computeComplexity(code), "Each case except default increases complexity");
    }

    @Test
    public void testCatchBlock() {
        String code = """
            void test() {
                try {
                    int x = 1 / 0;
                } catch (ArithmeticException e) {
                    e.printStackTrace();
                }
            }
            """;
        assertEquals(2, computeComplexity(code));
    }

    @Test
    public void testTernaryConditional() {
        String code = "void test() { int x = (1 > 0) ? 1 : 2; }";
        assertEquals(2, computeComplexity(code));
    }

    @Test
    public void testLogicalAndOrOperators() {
        String code = "void test() { if (a && b || c) {} }";
        assertEquals(4, computeComplexity(code), "if + && + || => 1 + 1 + 1 + 1");
    }

    @Test
    public void testMixedControlFlow() {
        String code = """
            void test(int x) {
                if (x > 0 && x < 10) {
                    for (int i = 0; i < x; i++) {
                        while (i < x) {
                            x++;
                        }
                    }
                } else {
                    x = (x > 5) ? 1 : 2;
                }
            }
            """;
        assertEquals(6, computeComplexity(code));
    }
}