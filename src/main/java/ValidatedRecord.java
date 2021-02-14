import java.util.*;

/**
 * Провалидированный клиент с результатами валидации
 */
class ValidatedRecord {
    private final List<ViolationEntry> violationEntries;
    private final Client client;

    public ValidatedRecord(List<ViolationEntry> violationEntries, Client client) {
        this.violationEntries = violationEntries;
        this.client = client;
    }

    public List<ViolationEntry> getViolationEntries() {
        return violationEntries;
    }

    public Client getClient() {
        return client;
    }
}
