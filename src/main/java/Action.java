public enum Action {
    ADD_FILE("Добавить файл для анализа (Возможен только CSV - файл)"),
    CLEAR_FILES("Очистить список файлов"),
    BALANCE_BY_CLIENT("Баланс операций по указанному клиенту"),
    BALANCE_BY_CARD("баланс операций по указанному номеру карты"),
    SUM_BY_UNCONFIRMED("Сумма неподтвержденных операций"),
    SUM_BY_REJECTED("Сумма отклоненных операций"),
    EXIT("Выход");

    private final String title;

    Action(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
