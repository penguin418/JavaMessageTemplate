package io.github.penguin418;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Templated Messages with placeholders.
 * Placeholders can be replaced with actual values at runtime.
 */
public class MessageTemplate {
    private final String[] segmentArray;
    private final Map<String, int[]> keywordToPositionArray;


    private MessageTemplate(String[] segmentArray, Map<String, int[]> keywordToPositionArray) {
        this.segmentArray = segmentArray;
        this.keywordToPositionArray = keywordToPositionArray;
    }

    /**
     * Processes the template by replacing placeholders with the provided values.
     *
     * @param replacements A map containing placeholder keywords and their corresponding replacement values.
     * @return The processed template as a String with placeholders replaced.
     */
    public String process(Map<String, String> replacements) {
        String[] resultArray = new String[segmentArray.length];
        System.arraycopy(segmentArray, 0, resultArray, 0, segmentArray.length);
        replacements.forEach((key, value) -> {
            if (keywordToPositionArray.containsKey(key)) {
                for (int position : keywordToPositionArray.get(key))
                    resultArray[position] = value;
            }
        });
        return String.join("", resultArray);
    }

    /**
     * Retrieves the original template with placeholders.
     *
     * @return The template as a String with placeholders in the format ${keyword} or ${keyword:defaultValue}.
     */
    public String getTemplate() {
        StringBuilder templateStringBuilder = new StringBuilder();
        Map<Integer, String> indexKeywordMap = new HashMap<>();
        for (Map.Entry<String, int[]> entry : keywordToPositionArray.entrySet()) {
            String keyword = entry.getKey();
            for (int position : entry.getValue()) {
                indexKeywordMap.put(position, keyword);
            }
        }
        for (int i = 0; i < segmentArray.length; i++) {
            String templateItem = segmentArray[i];
            if (indexKeywordMap.containsKey(i)) {
                String keyword = indexKeywordMap.get(i);
                if (templateItem == null) {
                    templateStringBuilder.append("${").append(keyword).append("}");
                } else {
                    templateStringBuilder.append("${").append(keyword).append(":").append(templateItem).append("}");
                }
            } else {
                // Process templateItem to reconstruct escaped placeholders
                Matcher matcher = Builder.CURLY_BRACE_RESERVED_POSITION_PATTERN.matcher(templateItem);
                int lastIndex = 0;
                while (matcher.find()) {
                    templateStringBuilder.append(templateItem, lastIndex, matcher.start());
                    String placeholderContent = matcher.group(2);
                    templateStringBuilder.append("\\${").append(placeholderContent).append("}");
                    lastIndex = matcher.end();
                }
                templateStringBuilder.append(templateItem.substring(lastIndex));
            }
        }
        return templateStringBuilder.toString();
    }

    /**
     * Creates a new Builder instance for constructing a MessageTemplate.
     *
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing a MessageTemplate instance.
     */
    public static class Builder {

        /**
         * Pattern to identify placeholders in the template in the format ${keyword} or ${keyword:defaultValue}.
         * - (\\\\*) backslash
         * - \\$\\{ opening brace
         * - ((?:[^\\\\}]|\\.)*?) anything except closing brace
         * - \\} closing brace
         */
        static final Pattern CURLY_BRACE_RESERVED_POSITION_PATTERN = Pattern.compile("(\\\\*)\\$\\{((?:[^\\\\}]|\\\\.)*?)\\}");

        private static Function<String, Placeholder> CURLY_BRACE_RESERVED_POSITION_PARSER() {
            return (s) -> {
                if (s.contains(":")) {
                    final String[] parted = s.split(":");
                    String keyword = parted[0].substring(2);
                    String defaultValue = parted[1].substring(0, parted[1].length() - 1);
                    return new Placeholder(keyword, defaultValue);
                }
                return new Placeholder(s.substring(2, s.length() - 1), null);
            };
        }

        private final List<String> segmentList = new ArrayList<>();
        private final Map<String, List<Integer>> keywordToPositionList = new HashMap<>();
        private String lastAppended = null;

        /**
         * Appends a string to the template.
         *
         * @param raw The string to append.
         * @return The Builder instance for method chaining.
         */
        public Builder append(String raw) {
            if (lastAppended != null) {
                lastAppended = lastAppended + raw;
                segmentList.set(segmentList.size() - 1, lastAppended);
            } else {
                lastAppended = raw;
                segmentList.add(lastAppended);
            }
            return this;
        }

        /**
         * Reserves a placeholder in the template with a placeholder and default value.
         *
         * @param keyword      The reserved keyword for placeholder.
         * @param defaultValue The default value for the placeholder.
         * @return The Builder instance for method chaining.
         */
        public Builder reserve(String keyword, String defaultValue) {
            lastAppended = null;
            segmentList.add(defaultValue);
            keywordToPositionList.computeIfAbsent(keyword, (v) -> new ArrayList<>()).add(segmentList.size() - 1);
            return this;
        }

        /**
         * Appends a template string containing placeholders to the builder.
         *
         * @param template The template string to append.
         * @return The Builder instance for method chaining.
         */
        public Builder appendTemplate(String template) {
            return format(template, CURLY_BRACE_RESERVED_POSITION_PATTERN, CURLY_BRACE_RESERVED_POSITION_PARSER());
        }

        public Builder appendTemplate(MessageTemplate template) {
            Map<Integer, String> reserved = template.keywordToPositionArray.entrySet().stream().flatMap(kv -> Arrays.stream(kv.getValue()).mapToObj(v -> Map.entry(v, kv.getKey()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            for (int i = 0; i < template.segmentArray.length; i++) {
                if (reserved.containsKey(i)) {
                    reserve(reserved.get(i), template.segmentArray[i]);
                } else {
                    appendTemplate(template.segmentArray[i]);
                }
            }
            return this;
        }

        /**
         * Formats the template string containing placeholders.
         *
         * @param template The template string to format.
         * @return The Builder instance for method chaining.
         * @deprecated Use {@link #appendTemplate(String)} instead.
         */
        @Deprecated
        public Builder format(String template) {
            return format(template, CURLY_BRACE_RESERVED_POSITION_PATTERN, CURLY_BRACE_RESERVED_POSITION_PARSER());
        }

        private Builder format(String template, Pattern reservedPattern, Function<String, Placeholder> reservedPosition) {
            Matcher matcher = reservedPattern.matcher(template);
            int lastIndex = 0;
            while (matcher.find()) {
                append(template.substring(lastIndex, matcher.start()));

                String backslashes = matcher.group(1);
                String placeholderContent = matcher.group(2);
                int backslashCount = backslashes.length();

                if (backslashCount % 2 == 0) {
                    append(backslashes);
                    Placeholder reserved = reservedPosition.apply("${" + placeholderContent + "}");
                    reserve(reserved.keyword, reserved.defaultValue);
                } else {
                    // Combine backslashes and placeholder into a single string
                    append(backslashes.substring(0, backslashCount - 1) + "${" + placeholderContent + "}");
                }

                lastIndex = matcher.end();
            }
            append(template.substring(lastIndex));
            return this;
        }

        /**
         * Builds and returns a MessageTemplate instance based on the current state of the builder.
         *
         * @return A new MessageTemplate instance.
         */
        public MessageTemplate build() {
            Map<String, int[]> reservedPositions = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : keywordToPositionList.entrySet()) {
                int[] positions = new int[entry.getValue().size()];
                for (int i = 0; i < entry.getValue().size(); i++) {
                    positions[i] = entry.getValue().get(i);
                }
                reservedPositions.put(entry.getKey(), positions);
            }

            return new MessageTemplate(segmentList.toArray(new String[0]), reservedPositions);
        }

        private static class Placeholder {
            String keyword;
            String defaultValue;

            private Placeholder(String keyword, String defaultValue) {
                this.keyword = keyword;
                this.defaultValue = defaultValue;
            }
        }
    }
}