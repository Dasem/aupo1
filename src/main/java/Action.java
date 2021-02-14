public enum Action {
    ADD_FILE("Добавить файл для анализа (Возможен только CSV - файл)"),
    CLEAR_FILES("Очистить список файлов"),
    CHECK_FILES("Просмотреть список файлов"),

    BALANCE_BY_CLIENT("Баланс операций по указанному клиенту", true),
    BALANCE_BY_CARD("Баланс операций по указанному номеру карты", true),
    SUM_BY_UNCONFIRMED("Сумма неподтвержденных операций", true),
    SUM_BY_REJECTED("Сумма отклоненных операций", true),

    EXIT("Выход");

    private final String title;
    private boolean operation = false;

    Action(String title) {
        this.title = title;
    }

    Action(String title, boolean operation) {
        this.title = title;
        this.operation = operation;
    }

    public String getTitle() {
        return title;
    }

    public boolean isOperation() {
        return operation;
    }
}
