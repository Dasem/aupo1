/**
 * Сущность "Поля", соответствует полям из CSV,
 * при редактировании необходимо добавить соответствующие поля в {@link Client}
 */
public enum Field {
    ID("Идентификационный номер клиента", 0),
    CARD_ID("Идентификационный номер карты", 1),
    DATE("Дата операции", 2),
    TIME("Время операции", 3),
    OPERATION_TYPE("Тип операции", 4),
    SUM("Сумма операции", 5),
    OPERATION_ACCEPT("Признак подтверждения операции", 6),

    RECORD("Вся запись", -1);

    private final String title;
    private final int csvOrdinal;

    Field(String title, int csvOrdinal) {
        this.title = title;
        this.csvOrdinal = csvOrdinal;
    }

    public static int fieldsCount() {
        return values().length - 1;
    }

    public String getTitle() {
        return title;
    }

    public int getCsvOrdinal() {
        return csvOrdinal;
    }
}
