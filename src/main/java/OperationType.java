enum OperationType {
    DEBITING("Списание"),
    CREDITING("Зачисление");

    public static OperationType getFromTitle(String title) {
        for (OperationType value: values()) {
            if (value.getTitle().equals(title)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Несуществующий тип операции");
    }

    OperationType(String title) {
        this.title = title;
    }

    private final String title;

    public String getTitle() {
        return title;
    }
}
