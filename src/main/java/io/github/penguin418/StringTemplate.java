package io.github.penguin418;

import java.util.*;

public class StringTemplate {
    private final String[] templateArray;
    private final Map<String, int[]> reservedPositions;

    private StringTemplate(String[] templateArray, Map<String, int[]> reservedPositions) {
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

    public static class Builder {
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

        public StringTemplate build() {
            Map<String, int[]> reservedPositions = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : reservedKeywords.entrySet()) {
                int[] positions = new int[entry.getValue().size()];
                for (int i = 0; i < entry.getValue().size(); i++) {
                    positions[i] = entry.getValue().get(i);
                }
                reservedPositions.put(entry.getKey(), positions);
            }

            return new StringTemplate(templateList.toArray(new String[0]), reservedPositions);
        }
    }
}
