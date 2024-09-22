package io.github.penguin418;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MessageTemplateTest {

    @Nested
    public class FunctionalTest {
        @Test
        @DisplayName("다른 값으로 매핑하더라도, 원본 템플릿은 변하면 안된다")
        void processTest() {
            MessageTemplate st = MessageTemplate.builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", "sit")
                    .append(" amet, ")
                    .reserve("consectetur", "adipiscing")
                    .append(" elit.")
                    .build();
            assertEquals("Lorem ipsum sit amet, adipiscing elit.", st.process(Map.of()));
            assertEquals("Lorem ipsum dolor amet, elit elit.", st.process(Map.of("dolor", "dolor", "consectetur", "elit")));
            assertEquals("Lorem ipsum sit amet, adipiscing elit.", st.process(Map.of()));
        }

        @Test
        void getTemplateStringTest() {
            MessageTemplate st = MessageTemplate.builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", "sit")
                    .append(" amet, ")
                    .reserve("consectetur", "adipiscing")
                    .append(" elit.")
                    .build();
            assertEquals("Lorem ipsum ${dolor:sit} amet, ${consectetur:adipiscing} elit.", st.getTemplate());
        }


        @Test
        @DisplayName("Empty template should return an empty string")
        void staticConstructorOfTest0() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("").build();
            assertEquals("", template.process(Map.of()));
        }


        @Test
        @DisplayName("Template with two keywords should reflect their respective values")
        void staticConstructorOfTest1() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("Lorem ${ipsum} sit ${amet}, adipiscing elit.").build();
            assertEquals("Lorem IPSUM sit AMET, adipiscing elit.", template.process(Map.of("ipsum", "IPSUM", "amet", "AMET")));
        }

        @Test
        @DisplayName("Unmatched curly braces should be ignored")
        void staticConstructorOfTest2() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("Lorem {${ipsum} sit ${amet}}, adipiscing elit.").build();
            assertEquals("Lorem {IPSUM sit AMET}, adipiscing elit.", template.process(Map.of("ipsum", "IPSUM", "amet", "AMET")));
        }

        @Test
        @DisplayName("Nested curly brace should be captured as reserved keyword")
        void staticConstructorOfTest3() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("Lorem {${ipsum}} sit ${ipsum}, adipiscing elit.").build();
            assertEquals("Lorem {IPSUM} sit IPSUM, adipiscing elit.", template.process(Map.of("ipsum", "IPSUM")));
        }


        @Test
        @DisplayName("Curly braces should capture all character before closing")
        void staticConstructorOfTest4() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("Lorem ${{ipsum} sit ${amet}, adipiscing elit.").build();
            assertEquals("Lorem IPSUM sit null, adipiscing elit.", template.process(Map.of("{ipsum", "IPSUM")));
        }

        @Test
        @DisplayName("Escaped special character should be ignored")
        void staticConstructorOfTestEscapeSpecialCharacters() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("${Lorem} \\${ipsum} sit $\\${amet}, adipiscing \\\\${elit}.").build();
            assertEquals("null ${ipsum} sit $${amet}, adipiscing \\\\ELIT.", template.process(Map.of("elit", "ELIT")));
        }

        @Test
        @DisplayName("Ignored escaped special character should be restored by getTemplate()")
        void staticConstructorOfTestEscapeSpecialCharacters2() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("${Lorem} \\${ipsum} sit $\\${amet}, adipiscing \\\\${elit}.").build();
            String templateString = template.getTemplate();
            MessageTemplate template2 = MessageTemplate.builder().appendTemplate(templateString).build();

            assertEquals("null ${ipsum} sit $${amet}, adipiscing \\\\ELIT.", template2.process(Map.of("elit", "ELIT")));
        }

        @Test
        @DisplayName("Default value should be used")
        void staticConstructorOfTestDefaultValue() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("Lorem ${ipsum:DEFAULT1} sit ${amet:DEFAULT2}, adipiscing elit.").build();
            assertEquals("Lorem DEFAULT1 sit DEFAULT2, adipiscing elit.", template.process(Map.of()));
        }

        @Test
        @DisplayName("Duplicated keyword should hold respective default value")
        void staticConstructorOfTestDuplicateKeyword() {
            MessageTemplate template = MessageTemplate.builder().appendTemplate("Lorem ${ipsum:DEFAULT1} sit ${ipsum:DEFAULT2}, adipiscing elit.").build();
            assertEquals("Lorem DEFAULT1 sit DEFAULT2, adipiscing elit.", template.process(Map.of()));
            assertEquals("Lorem IPSUM sit IPSUM, adipiscing elit.", template.process(Map.of("ipsum", "IPSUM")));
        }

        @Test
        @DisplayName("Pattern should capture placeHolder inside brace start with $")
        public void patternTest(){
            String placeHolder = "PLACEHOLDER";
            String text = "hello ${PLACEHOLDER}";
            Matcher matcher = MessageTemplate.Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(text);

            assertTrue(matcher.find());
            assertEquals(placeHolder, matcher.group(2));
        }

        @Test
        @DisplayName("Pattern should capture keyword and default value inside braces")
        public void patternTest2(){
            String placeHolder = "KEYWORD:DEFAULT";
            String text = "hello ${KEYWORD:DEFAULT}";
            Matcher matcher = MessageTemplate.Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(text);

            assertTrue(matcher.find());
            assertEquals(placeHolder, matcher.group(2));
        }

        @Test
        @DisplayName("Pattern should capture escaped closing braces in placeholder")
        public void patternTest3(){
            String placeHolder = "KEYWORD:DEFAULT\\}";
            String text = "hello ${KEYWORD:DEFAULT\\}}";
            Matcher matcher = MessageTemplate.Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(text);

            assertTrue(matcher.find());
            assertEquals(placeHolder, matcher.group(2));
        }

        @Test
        @DisplayName("Pattern should capture escaped backslashes in placeholder")
        public void patternTest4(){
            String placeHolder = "KEYWORD:DEFAULT\\\\";
            String text = "hello ${KEYWORD:DEFAULT\\\\}}";
            Matcher matcher = MessageTemplate.Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(text);

            assertTrue(matcher.find());
            assertEquals(placeHolder, matcher.group(2));
        }

        @Test
        @DisplayName("Escaped dollar signs should not be processed as placeholders")
        public void patternTest5(){
            String placeHolder = "KEYWORD:DEFAULT";
            String text = "hello \\${KEYWORD:DEFAULT}";
            Matcher matcher = MessageTemplate.Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(text);

            assertTrue(matcher.find());
            assertEquals("\\", matcher.group(1));
            assertEquals(placeHolder, matcher.group(2));
        }

        @Test
        @DisplayName("Double backslashes should be ignored")
        public void patternTest6(){
            String placeHolder = "KEYWORD:DEFAULT";
            String text = "hello \\\\${KEYWORD:DEFAULT}";
            Matcher matcher = MessageTemplate.Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(text);

            assertTrue(matcher.find());
            assertEquals("\\\\", matcher.group(1));
            assertEquals(placeHolder, matcher.group(2));
        }
    }

    @Nested
    public class PerformanceTest {
        private static final int WARM_UP_ITERATIONS = 100_000;
        private static final int ITERATIONS = 1_000_000;
        private static final int STRING_LENGTH = 10; // 랜덤 문자열 길이

        @Test
        @DisplayName("Short template processing should not take the longest time")
        public void shortTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // MessageTemplate
            MessageTemplate messageTemplate = new MessageTemplate.Builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", generateRandomString(random))
                    .append(" amet, ")
                    .reserve("consectetur", generateRandomString(random))
                    .append(" elit.")
                    .build();
            long durationMessageTemplate = measureTime(() -> {
                String result = messageTemplate.process(Map.of("dolor", generateRandomString(random), "consectetur", generateRandomString(random)));
            });
            results.put("durationMessageTemplate", durationMessageTemplate);

            // StringBuilder
            long durationStringBuilder = measureTime(() -> {
                String result = new StringBuilder().append("Lorem ipsum ")
                        .append(generateRandomString(random))
                        .append(" amet, ")
                        .append(generateRandomString(random))
                        .append(" elit.").toString();
            });
            results.put("durationStringBuilder", durationStringBuilder);

            // StringFormat
            long durationStringFormat = measureTime(() -> {
                String result = String.format("Lorem ipsum %s amet, %s elit.", generateRandomString(random), generateRandomString(random));
            });
            results.put("durationStringFormat", durationStringFormat);


            // 출력
            results.forEach((key, value) -> {
                System.out.println(key + ": " + value + " ms");
            });

            long maxDuration = results.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getValue).get();
            assertTrue(maxDuration >durationMessageTemplate);
        }

        @Test
        @DisplayName("Mid template processing should not take the longest time")
        public void midTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // MessageTemplate
            MessageTemplate messageTemplate = new MessageTemplate.Builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", generateRandomString(random))
                    .append(" amet, ")
                    .reserve("consectetur", generateRandomString(random))
                    .append(" elit, ")
                    .reserve("sed", generateRandomString(random))
                    .append(" do ")
                    .reserve("eiusmod", generateRandomString(random))
                    .append(" tempor.")
                    .build();
            long durationMessageTemplate = measureTime(() -> {
                String result = messageTemplate.process(Map.of(
                        "dolor", generateRandomString(random),
                        "consectetur", generateRandomString(random),
                        "sed", generateRandomString(random),
                        "eiusmod", generateRandomString(random)
                ));
            });
            results.put("durationMessageTemplate", durationMessageTemplate);

            // StringBuilder
            long durationStringBuilder = measureTime(() -> {
                String result = new StringBuilder().append("Lorem ipsum ")
                        .append(generateRandomString(random))
                        .append(" amet, ")
                        .append(generateRandomString(random))
                        .append(" elit, ")
                        .append(generateRandomString(random))
                        .append(" do ")
                        .append(generateRandomString(random))
                        .append(" tempor.").toString();
            });
            results.put("durationStringBuilder", durationStringBuilder);

            // StringFormat
            long durationStringFormat = measureTime(() -> {
                String result = String.format("Lorem ipsum %s amet, %s elit, %s do %s tempor.",
                        generateRandomString(random), generateRandomString(random), generateRandomString(random), generateRandomString(random));
            });
            results.put("durationStringFormat", durationStringFormat);

            // 출력
            results.forEach((key, value) -> {
                System.out.println(key + ": " + value + " ms");
            });

            long maxDuration = results.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getValue).get();
            assertTrue(maxDuration >durationMessageTemplate);
        }

        @Test
        @DisplayName("Long template processing should not take the longest time")
        public void longTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // MessageTemplate
            MessageTemplate messageTemplate = new MessageTemplate.Builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", generateRandomString(random))
                    .append(" amet, ")
                    .reserve("consectetur", generateRandomString(random))
                    .append(" elit, ")
                    .reserve("sed", generateRandomString(random))
                    .append(" do ")
                    .reserve("eiusmod", generateRandomString(random))
                    .append(" tempor ")
                    .reserve("incididunt", generateRandomString(random))
                    .append(" ut ")
                    .reserve("labore", generateRandomString(random))
                    .append(" et ")
                    .reserve("dolore", generateRandomString(random))
                    .append(" magna aliqua.")
                    .build();
            long durationMessageTemplate = measureTime(() -> {
                String result = messageTemplate.process(Map.of(
                        "dolor", generateRandomString(random),
                        "consectetur", generateRandomString(random),
                        "sed", generateRandomString(random),
                        "eiusmod", generateRandomString(random),
                        "incididunt", generateRandomString(random),
                        "labore", generateRandomString(random),
                        "dolore", generateRandomString(random)
                ));
            });
            results.put("durationMessageTemplate", durationMessageTemplate);

            // StringBuilder
            long durationStringBuilder = measureTime(() -> {
                String result = new StringBuilder().append("Lorem ipsum ")
                        .append(generateRandomString(random))
                        .append(" amet, ")
                        .append(generateRandomString(random))
                        .append(" elit, ")
                        .append(generateRandomString(random))
                        .append(" do ")
                        .append(generateRandomString(random))
                        .append(" tempor ")
                        .append(generateRandomString(random))
                        .append(" ut ")
                        .append(generateRandomString(random))
                        .append(" et ")
                        .append(generateRandomString(random))
                        .append(" magna aliqua.").toString();
            });
            results.put("durationStringBuilder", durationStringBuilder);

            // StringFormat
            long durationStringFormat = measureTime(() -> {
                String result = String.format("Lorem ipsum %s amet, %s elit, %s do %s tempor %s ut %s et %s magna aliqua.",
                        generateRandomString(random), generateRandomString(random), generateRandomString(random),
                        generateRandomString(random), generateRandomString(random), generateRandomString(random), generateRandomString(random));
            });
            results.put("durationStringFormat", durationStringFormat);

            // 출력
            results.forEach((key, value) -> {
                System.out.println(key + ": " + value + " ms");
            });

            long maxDuration = results.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getValue).get();
            assertTrue(maxDuration >durationMessageTemplate);
        }

        @Test
        @DisplayName("Even longer template processing should not take the longest time")
        public void longerTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // MessageTemplate
            MessageTemplate messageTemplate = new MessageTemplate.Builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", generateRandomString(random))
                    .append(" amet, ")
                    .reserve("consectetur", generateRandomString(random))
                    .append(" elit, ")
                    .reserve("sed", generateRandomString(random))
                    .append(" do ")
                    .reserve("eiusmod", generateRandomString(random))
                    .append(" tempor ")
                    .reserve("incididunt", generateRandomString(random))
                    .append(" ut ")
                    .reserve("labore", generateRandomString(random))
                    .append(" et ")
                    .reserve("dolore", generateRandomString(random))
                    .append(" et ")
                    .reserve("dolore1", generateRandomString(random))
                    .append(" et ")
                    .reserve("dolore2", generateRandomString(random))
                    .append(" magna aliqua.")
                    .build();
            long durationMessageTemplate = measureTime(() -> {
                String result = messageTemplate.process(Map.of(
                        "dolor", generateRandomString(random),
                        "consectetur", generateRandomString(random),
                        "sed", generateRandomString(random),
                        "eiusmod", generateRandomString(random),
                        "incididunt", generateRandomString(random),
                        "labore", generateRandomString(random),
                        "dolore", generateRandomString(random),
                        "dolore1", generateRandomString(random),
                        "dolore2", generateRandomString(random)
                ));
            });
            results.put("durationMessageTemplate", durationMessageTemplate);

            // StringBuilder
            long durationStringBuilder = measureTime(() -> {
                String result = new StringBuilder().append("Lorem ipsum ")
                        .append(generateRandomString(random))
                        .append(" amet, ")
                        .append(generateRandomString(random))
                        .append(" elit, ")
                        .append(generateRandomString(random))
                        .append(" do ")
                        .append(generateRandomString(random))
                        .append(" tempor ")
                        .append(generateRandomString(random))
                        .append(" ut ")
                        .append(generateRandomString(random))
                        .append(" et ")
                        .append(generateRandomString(random))
                        .append(" et ")
                        .append(generateRandomString(random))
                        .append(" et ")
                        .append(generateRandomString(random))
                        .append(" magna aliqua.").toString();
            });
            results.put("durationStringBuilder", durationStringBuilder);

            // StringFormat
            long durationStringFormat = measureTime(() -> {
                String result = String.format("Lorem ipsum %s amet, %s elit, %s do %s tempor %s ut %s et %s et %s et %s magna aliqua.",
                        generateRandomString(random), generateRandomString(random), generateRandomString(random),
                        generateRandomString(random), generateRandomString(random), generateRandomString(random), generateRandomString(random), generateRandomString(random), generateRandomString(random));
            });
            results.put("durationStringFormat", durationStringFormat);

            // 출력
            results.forEach((key, value) -> {
                System.out.println(key + ": " + value + " ms");
            });

            long maxDuration = results.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getValue).get();
            assertTrue(maxDuration >durationMessageTemplate);
        }


        private static String generateRandomString(Random random) {
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder(STRING_LENGTH);
            for (int i = 0; i < STRING_LENGTH; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            return sb.toString();
        }

        private static long measureTime(Runnable task) {
            // JIT 오버헤드 제거
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                task.run();
            }
            // GC 영향 제거
            System.gc();
            // 성능 측정
            long startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                task.run();
            }
            long endTime = System.nanoTime();
            return (endTime - startTime) / ITERATIONS;
        }
    }

}
