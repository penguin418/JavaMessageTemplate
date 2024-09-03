# StringTemplate

예약어를 추가하고, 나중에 실제 값으로 대체할 수 있는 템플릿.

미리 값으로 대치할 위치를 문자열 키로 예약할 수 있습니다.

String.format 또는 StringBuilder과 비교해서 너무 느리지는 않은 수준이므로, 성능이 필요한 곳 보다는, 

이메일 템플릿처럼, 주요 로직과 분리되어 실행되는 코드 중에, 가독성이 필요한 경우 사용하면 좋을 것 같습니다.

StringBuilder는 builder를 작성 시에 실제 값을 추가해야 하지만, StringTemplate는 미리 정의된 위치에 값을 추가할 수 있게 해줍니다.

### 성능 비교

StringTemplate 클래스를 사용하여 StringBuilder 및 String.format과 비교한 성능 측정치는 다음과 같습니다

| 파라미터 개수 | StringTemplate  | StringBuilder  |  String.format |
|---------|---|---|---|
| 2개      | 330 ms  | 216 ms  |  390 ms |
| 4개      | 570 ms  | 444 ms  |  708 ms |
| 7개      | 930 ms  | 767 ms  |  1297 ms |
| 9개      |  1271 ms | 1054 ms  |  1535 ms |

* 성능 측정은 1,000,000 번 반복하여 측정하였습니다.
* StringTemplate 클래스가 StringBuilder보다는 느리지만, String.format보다는 빠릅니다.

### 사용법

1. 템플릿 생성: 문자열 템플릿을 생성하려면 Builder 클래스를 사용합니다. 

    ```java
    StringTemplate template = new StringTemplate.Builder()
        .append("Hello, ")
        .reserve("name", "{name}")
        .append("! Welcome to ")
        .reserve("place", "{place}")
        .append(".")
        .build();
    ```

2. 템플릿 값 설정: 예약어를 채울 수 있습니다.

    ```java
    String result = template.process(replacements, Map.of("name", "John"));
    System.out.println(result); // 출력: Hello, John! Welcome to {place}.
    ```
   * 채우지 않은 예약어는 기본값으로 남습니다.

