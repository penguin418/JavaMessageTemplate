package io.github.penguin418;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageTemplate {
    private final String[] templateArray;
    private final Map<String, int[]> reservedPositions;


    private MessageTemplate(String[] templateArray, Map<String, int[]> reservedPositions) {
        this.templateArray = templateArray;
        this.reservedPositions = reservedPositions;
    }

    public String process(Map<String, String> replacements) {
        String[] resultArray = new String[templateArray.length];
        System.arraycopy(templateArray, 0, resultArray, 0, templateArray.length);
        replacements.forEach((key, value) -> {
            if (reservedPositions.containsKey(key)) for (int position : reservedPositions.get(key))
                resultArray[position] = value;
        });
        return String.join("", resultArray);
    }

    public String getTemplate() {
        StringBuilder templateStringBuilder = new StringBuilder();
        Map<Integer, String> indexKeywordMap = new HashMap<>();
        for (Map.Entry<String, int[]> entry : reservedPositions.entrySet()) {
            String keyword = entry.getKey();
            for (int position : entry.getValue()) {
                indexKeywordMap.put(position, keyword);
            }
        }
        for (int i = 0; i < templateArray.length; i++) {
            if (indexKeywordMap.containsKey(i)){
                String keyword = indexKeywordMap.get(i);
                String defaultValue = templateArray[i];
                if (defaultValue == null) {
                    templateStringBuilder.append("${").append(keyword).append("}");
                } else {
                    templateStringBuilder.append("${").append(keyword).append(":").append(defaultValue).append("}");
                }
            } else {
                templateStringBuilder.append(templateArray[i]);
            }
        }
        return templateStringBuilder.toString();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public static Pattern CURLY_BRACE_RESERVED_POSITION_PATTERN = Pattern.compile("(?<!\\\\)\\$\\{([^}]*)}");

        private static Function<String, ReservedPosition> CURLY_BRACE_RESERVED_POSITION_PARSER() {
            return (s) -> {
                if (s.contains(":")) {
                    final String[] parted = s.split(":");
                    String keyword = parted[0].substring(2, parted[0].length());
                    String defaultValue = parted[1].substring(0, parted[1].length() - 1);
                    return new ReservedPosition(keyword, defaultValue);
                }
                return new ReservedPosition(s.substring(2, s.length() - 1), null);
            };
        }

        private final List<String> templateList = new ArrayList<>();
        private final Map<String, List<Integer>> reservedKeywords = new HashMap<>();
        private String lastAppended = null;

        public Builder append(String str) {
            if (lastAppended != null) {
                lastAppended = lastAppended + str;
                templateList.set(templateList.size() - 1, lastAppended);
            } else {
                lastAppended = str;
                templateList.add(lastAppended);
            }
            return this;
        }

        public Builder reserve(String keyword, String defaultValue) {
            lastAppended = null;
            templateList.add(defaultValue);
            reservedKeywords.computeIfAbsent(keyword, (v) -> new ArrayList<>()).add(templateList.size() - 1);
            return this;
        }

        public Builder appendTemplate(String template) {
            return format(template, CURLY_BRACE_RESERVED_POSITION_PATTERN, CURLY_BRACE_RESERVED_POSITION_PARSER());
        }

        @Deprecated
        public Builder format(String template) {
            return format(template, CURLY_BRACE_RESERVED_POSITION_PATTERN, CURLY_BRACE_RESERVED_POSITION_PARSER());
        }

        private Builder format(String template, Pattern reservedPattern, Function<String, ReservedPosition> reservedPosition) {
            Matcher matcher = reservedPattern.matcher(template);
            int lastIndex = 0;
            while (matcher.find()) {
                append(template.substring(lastIndex, matcher.start()));
                ReservedPosition reserved = reservedPosition.apply(template.substring(matcher.start(), matcher.end()));
                reserve(reserved.keyword, reserved.defaultValue);
                lastIndex = matcher.end();
            }
            append(template.substring(lastIndex));
            return this;
        }

        public MessageTemplate build() {
            Map<String, int[]> reservedPositions = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : reservedKeywords.entrySet()) {
                int[] positions = new int[entry.getValue().size()];
                for (int i = 0; i < entry.getValue().size(); i++) {
                    positions[i] = entry.getValue().get(i);
                }
                reservedPositions.put(entry.getKey(), positions);
            }

            return new MessageTemplate(templateList.toArray(new String[0]), reservedPositions);
        }

        private static class ReservedPosition {
            String keyword;
            String defaultValue;

            private ReservedPosition(String keyword, String defaultValue) {
                this.keyword = keyword;
                this.defaultValue = defaultValue;
            }
        }
    }
}
