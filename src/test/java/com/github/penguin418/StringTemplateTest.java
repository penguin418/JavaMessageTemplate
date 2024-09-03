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
        private static final int ITERATIONS = 1_000_000;
        private static final int STRING_LENGTH = 10; // 랜덤 문자열 길이

        @Test
        @DisplayName("최소한 가장 느리지는 않을 것")
        public void comparison() {
            Map<String, Long> results = new HashMap<>();
            Random random = new Random();

            // StringTemplate
            System.gc();
            long startTime = System.nanoTime();
            StringTemplate stringTemplate = new StringTemplate.Builder()
                    .append("Lorem ipsum ")
                    .reserve("dolor", generateRandomString(random))
                    .append(" amet, ")
                    .reserve("consectetur", generateRandomString(random))
                    .append(" elit.")
                    .build();
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < ITERATIONS; i++) {
                map.put("dolor", generateRandomString(random));
                map.put("consectetur", generateRandomString(random));
                String result = stringTemplate.process(map);
            }
            long endTime = System.nanoTime();
            long durationStringTemplate = (endTime - startTime) / ITERATIONS;
            results.put("durationStringTemplate", durationStringTemplate);

            // StringBuilder
            System.gc();
            startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                String result = new StringBuilder().append("Lorem ipsum ")
                        .append(generateRandomString(random))
                        .append(" amet, ")
                        .append(generateRandomString(random))
                        .append(" elit.").toString();
            }
            endTime = System.nanoTime();
            results.put("durationStringBuilder", (endTime - startTime) / ITERATIONS);

            // StringFormat
            System.gc();
            startTime = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                String result = String.format("Lorem ipsum %s amet, %s elit.", generateRandomString(random), generateRandomString(random));
            }
            endTime = System.nanoTime();
            results.put("durationStringFormat", (endTime - startTime) / ITERATIONS);


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
    }

}
