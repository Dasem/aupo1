
public class ViolationEntry {
    private Field violationField;
    private int violationRow;
    private String fileName;
    private String description = "";

    public ViolationEntry() {
    }

    public ViolationEntry(Field violationField, int violationRow, String fileName) {
        this.violationField = violationField;
        this.violationRow = violationRow;
        this.fileName = fileName;
    }

    public ViolationEntry(Field violationField, int violationRow, String fileName, String description) {
        this.violationField = violationField;
        this.violationRow = violationRow;
        this.fileName = fileName;
        this.description = description;
    }

    public Field getViolationField() {
        return violationField;
    }

    public void setViolationField(Field violationField) {
        this.violationField = violationField;
    }

    public int getViolationRow() {
        return violationRow;
    }

    public void setViolationRow(int violationRow) {
        this.violationRow = violationRow;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}