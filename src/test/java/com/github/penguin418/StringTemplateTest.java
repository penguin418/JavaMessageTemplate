package com.github.penguin418;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StringTemplateTest {

    @Nested
    public class FunctionalTest{
        @Test
        @DisplayName("다른 값으로 매핑하더라도, 원본 템플릿은 변하면 안된다")
        void processTest() {
            StringTemplate st = new StringTemplate.Builder()
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
    }


    @Nested
    public class PerformanceTest {
        private static final int WARM_UP_ITERATIONS = 100_000;
        private static final int ITERATIONS = 1_000_000;
        private static final int STRING_LENGTH = 10; // 랜덤 문자열 길이

        @Test
        @DisplayName("짧은 템플릿 처리 시 최소한 가장 느리지는 않을 것")
        public void shortTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // StringTemplate
            StringTemplate stringTemplate = new StringTemplate.Builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", generateRandomString(random))
                    .append(" amet, ")
                    .reserve("consectetur", generateRandomString(random))
                    .append(" elit.")
                    .build();
            long durationStringTemplate = measureTime(()->{
                String result = stringTemplate.process(Map.of("dolor", generateRandomString(random), "consectetur", generateRandomString(random)));
            });
            results.put("durationStringTemplate", durationStringTemplate);

            // StringBuilder
            long durationStringBuilder = measureTime(()->{
                String result = new StringBuilder().append("Lorem ipsum ")
                        .append(generateRandomString(random))
                        .append(" amet, ")
                        .append(generateRandomString(random))
                        .append(" elit.").toString();
            });
            results.put("durationStringBuilder", durationStringBuilder);

            // StringFormat
            long durationStringFormat = measureTime(()->{
                String result = String.format("Lorem ipsum %s amet, %s elit.", generateRandomString(random), generateRandomString(random));
            });
            results.put("durationStringFormat", durationStringFormat);


            // 출력
            results.forEach((key, value) -> {
                System.out.println(key + ": " + value + " ms");
            });

            assertNotEquals(results.entrySet().stream().max(Map.Entry.comparingByValue()), durationStringTemplate);
        }
        @Test
        @DisplayName("중간 템플릿 처리 시 최소한 가장 느리지는 않을 것")
        public void midTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // StringTemplate
            StringTemplate stringTemplate = new StringTemplate.Builder()
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
            long durationStringTemplate = measureTime(() -> {
                String result = stringTemplate.process(Map.of(
                        "dolor", generateRandomString(random),
                        "consectetur", generateRandomString(random),
                        "sed", generateRandomString(random),
                        "eiusmod", generateRandomString(random)
                ));
            });
            results.put("durationStringTemplate", durationStringTemplate);

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

            assertNotEquals(results.entrySet().stream().max(Map.Entry.comparingByValue()), durationStringTemplate);
        }

        @Test
        @DisplayName("긴 템플릿 처리 시 최소한 가장 느리지는 않을 것")
        public void longTemplateComparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // StringTemplate
            StringTemplate stringTemplate = new StringTemplate.Builder()
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
            long durationStringTemplate = measureTime(() -> {
                String result = stringTemplate.process(Map.of(
                        "dolor", generateRandomString(random),
                        "consectetur", generateRandomString(random),
                        "sed", generateRandomString(random),
                        "eiusmod", generateRandomString(random),
                        "incididunt", generateRandomString(random),
                        "labore", generateRandomString(random),
                        "dolore", generateRandomString(random)
                ));
            });
            results.put("durationStringTemplate", durationStringTemplate);

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

            assertNotEquals(results.entrySet().stream().max(Map.Entry.comparingByValue()), durationStringTemplate);
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
            for(int i=0;i<WARM_UP_ITERATIONS;i++){
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
