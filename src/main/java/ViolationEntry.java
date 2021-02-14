
public class ViolationEntry {
    private final Field violationField;
    private final int violationRow;
    private final String fileName;
    private String description = "";

    public ViolationEntry(Field violationField, int violationRow, String fileName, String description) {
        this.violationField = violationField;
        this.violationRow = violationRow;
        this.fileName = fileName;
        this.description = description;
    }

    public Field getViolationField() {
        return violationField;
    }

    public int getViolationRow() {
        return violationRow;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDescription() {
        return description;
    }
}