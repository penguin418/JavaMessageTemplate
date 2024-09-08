# MessageTemplate

A template where placeholders can be added and later replaced with actual values.

You can reserve positions in the template with string keys where the values will be replaced later.

In comparison to `String.format` or `StringBuilder`, the performance is not significantly slower, so it is more suitable
for scenarios where performance is not the primary concern, such as email templates, where readability and separation
from core logic are more important.

Unlike `StringBuilder`, where you add values while building the template, `MessageTemplate` allows you to define
placeholders in advance, which can be filled later.

### Performance Comparison

The performance measurements for the `MessageTemplate` class compared to `StringBuilder` and `String.format` are as
follows:

| Number of Parameters | MessageTemplate | StringBuilder | String.format |
|----------------------|-----------------|---------------|---------------|
| 2                    | 330 ms          | 216 ms        | 390 ms        |
| 4                    | 570 ms          | 444 ms        | 708 ms        |
| 7                    | 930 ms          | 767 ms        | 1297 ms       |
| 9                    | 1271 ms         | 1054 ms       | 1535 ms       |

* Performance was measured by repeating the operation 1,000,000 times (version 0.0.1).
* The `MessageTemplate` class is slower than `StringBuilder`, but faster than `String.format`.

### Usage

1. Creating a template: Use the `Builder` class to create a string template.

    ```java
    MessageTemplate template = new MessageTemplate.Builder()
        .append("Hello, ")
        .reserve("name", "{name}")
        .append("! Welcome to ")
        .reserve("place", "{place}")
        .append(".")
        .build();
    ```

2. Setting template values: You can fill in the placeholders.

    ```java
    String result = template.process(Map.of("name", "John"));
    System.out.println(result); // Output: Hello, John! Welcome to {place}.
    ```
    * Unfilled placeholders will remain with their default values.


3. You can also use the static constructor of:
   ```java
   MessageTemplate template = MessageTemplate.of("Lorem ${ipsum} sit ${amet}, adipiscing elit.");
   String result = template.process(Map.of("ipsum", "IPSUM", "amet", "AMET"));
   System.out.println(result); // Output: Lorem IPSUM sit AMET, adipiscing elit.
   ```