enum OperationAccept {
    ACCEPTED("Подтверждена"),
    REJECTED("Отклонена"),
    PROCESSED("Обрабатывается");

    OperationAccept(String title) {
        this.title = title;
    }

    public static OperationAccept getFromTitle(String title) {
        for (OperationAccept value: values()) {
            if (value.getTitle().equals(title)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Несуществующий признак подтверждения операции");
    }

    private final String title;

    public String getTitle() {
        return title;
    }
}
