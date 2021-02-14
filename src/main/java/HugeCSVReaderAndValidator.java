import com.google.common.base.*;
import com.opencsv.*;
import com.opencsv.exceptions.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

public class HugeCSVReaderAndValidator implements AutoCloseable {
    private int currentRow = 0;
    private final String fileName;
    private final CSVReader reader;

    public HugeCSVReaderAndValidator(String fileName) throws FileNotFoundException {
        if (fileName == null) {
            throw new IllegalArgumentException("filename is null");
        }
        this.fileName = fileName;
        this.reader = new CSVReader(new FileReader(fileName));
    }

    public ValidatedRecord next() throws IOException, CsvValidationException {
        ++currentRow;
        String[] record = reader.readNext();
        if (record == null) {
            return null;
        }
        return validateRecord(record);
    }

    private ValidatedRecord validateRecord(String[] record) {
        ArrayList<ViolationEntry> violatedRecords = new ArrayList<>();
        if (record.length != Field.fieldsCount()) {
            ViolationEntry entry = new ViolationEntry(Field.RECORD, currentRow, fileName,
                    "Количество полей в записи не соответствует необходимому: " + record.length + "/" + Field.fieldsCount());
            violatedRecords.add(entry);
            return new ValidatedRecord(violatedRecords, null);
        }
        Client validClient = new Client();

        String id = CharMatcher.inRange('0', '9').retainFrom(record[Field.ID.getCsvOrdinal()]);
        if (id.isEmpty()) {
            violatedRecords.add(new ViolationEntry(Field.ID, currentRow, fileName, "Идентификационный номер клиента не корректен"));
        }
        validClient.setId(id);

        String cardId = CharMatcher.inRange('0', '9').retainFrom(record[Field.CARD_ID.getCsvOrdinal()]);
        if (cardId.isEmpty()) {
            violatedRecords.add(new ViolationEntry(Field.CARD_ID, currentRow, fileName, "Идентификационный номер карты не корректен"));
        }
        validClient.setCardId(cardId);

        SimpleDateFormat formatterDate = new SimpleDateFormat("dd-M-yyyy", Locale.getDefault());
        try {
            Date date = formatterDate.parse(record[Field.DATE.getCsvOrdinal()]);
            if (date.after(new Date())) {
                violatedRecords.add(new ViolationEntry(Field.DATE, currentRow, fileName, "Дата операции не может быть в будущем"));
            } else {
                validClient.setDate(date);
            }
        } catch (ParseException e) {
            violatedRecords.add(new ViolationEntry(Field.DATE, currentRow, fileName, "Дата операции не соответствует формату: 'ДД-ММ-ГГГГ'"));
        }

        SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date time = formatterTime.parse(record[Field.TIME.getCsvOrdinal()]);
            if (validClient.getDate() != null) {
                Calendar dateCalendar = Calendar.getInstance();
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(time);
                dateCalendar.setTime(validClient.getDate());
                dateCalendar.set(Calendar.HOUR, timeCalendar.get(Calendar.HOUR));
                dateCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
                if (dateCalendar.after(new Date())) {
                    violatedRecords.add(new ViolationEntry(Field.TIME, currentRow, fileName, "Дата и время операции не могут быть в будущем"));
                } else {
                    validClient.setTime(time);
                }
            }
        } catch (ParseException e) {
            violatedRecords.add(new ViolationEntry(Field.TIME, currentRow, fileName, "Время операции не соответствует формату: 'ЧЧ:ММ'"));
        }

        try {
            validClient.setOperationType(OperationType.valueOf(record[Field.OPERATION_TYPE.getCsvOrdinal()]));
        } catch (IllegalArgumentException ex) {
            violatedRecords.add(new ViolationEntry(Field.OPERATION_TYPE, currentRow, fileName, "Тип операции должен быть один из перечисленных: 'Списание', 'Зачисление'"));
        }

        try {
            BigDecimal sum = new BigDecimal(record[Field.SUM.getCsvOrdinal()]);
            if (sum.compareTo(BigDecimal.ZERO) < 0) {
                violatedRecords.add(new ViolationEntry(Field.SUM, currentRow, fileName, "Сумма операции отрицательная"));
            } else {
                validClient.setSum(sum);
            }
        } catch (NumberFormatException ex) {
            violatedRecords.add(new ViolationEntry(Field.SUM, currentRow, fileName, "Сумма операции некорректна"));
        }

        try {
            validClient.setOperationAccept(OperationAccept.valueOf(record[Field.OPERATION_ACCEPT.getCsvOrdinal()]));
        } catch (IllegalArgumentException ex) {
            violatedRecords.add(new ViolationEntry(Field.OPERATION_ACCEPT, currentRow, fileName, "Признак подтверждения операции должен быть один из перечисленных: 'Подтверждена', 'Отклонена', 'Обрабатывается'"));
        }

        return new ValidatedRecord(violatedRecords, validClient);
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(MessageFormat.format("Невозможно закрытие файла ''{0}''", fileName));
        }
    }
}
