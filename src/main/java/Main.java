import com.google.common.base.*;
import com.opencsv.*;
import com.opencsv.exceptions.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

/**
 * Иммеется попытка исправления ошибок форматирования.
 * Т.к. нет исходных данных по формату номеров (клиента и карты)
 */
public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        List<ViolationEntry> entries = new ArrayList<>();
        List<Client> validClients = new ArrayList<>();
        for (String fileName : args) {
            try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
                String[] record;
                int row = 1;
                while ((record = reader.readNext()) != null) {
                    ValidatedRecord validatedRecord = validateRecord(record, row++, fileName);
                    List<ViolationEntry> violationsByRecord = validatedRecord.getViolationEntries();
                    if (violationsByRecord.isEmpty()) {
                        validClients.add(validatedRecord.getClient());
                    }
                    entries.addAll(violationsByRecord);
                }
            } catch (IOException e) {
                System.out.println("Error while file reading: " + e.getLocalizedMessage());
            } catch (CsvException e) {
                System.out.println("Error in CSV: " + e.getLocalizedMessage());
            }
        }
        Action action;
        while ((action = menu()) != Action.EXIT) {
            BigDecimal sum = BigDecimal.ZERO;
            switch (action) {
                case BALANCE_BY_CARD:
                    System.out.println("Введите карту клиента");
                    String card = CharMatcher.inRange('0', '9').retainFrom(sc.next());
                    for (Client client : validClients) {
                        if (client.getOperationAccept() == OperationAccept.ACCEPTED && card.equals(client.getCardId())) {
                            if (client.getOperationType() == OperationType.CREDITING) {
                                sum = sum.add(client.getSum());
                            } else {
                                sum = sum.subtract(client.getSum());
                            }
                        }
                    }
                    break;
                case BALANCE_BY_CLIENT:
                    System.out.println("Введите ID клиента");
                    String clientId = CharMatcher.inRange('0', '9').retainFrom(sc.next());
                    for (Client client : validClients) {
                        if (client.getOperationAccept() == OperationAccept.ACCEPTED && clientId.equals(client.getId())) {
                            if (client.getOperationType() == OperationType.CREDITING) {
                                sum = sum.add(client.getSum());
                            } else {
                                sum = sum.subtract(client.getSum());
                            }
                        }
                    }
                    break;
                case SUM_BY_REJECTED:
                    for (Client client : validClients) {
                        if (client.getOperationAccept() == OperationAccept.REJECTED) {
                            sum = sum.add(client.getSum());
                        }
                    }
                    break;
                case SUM_BY_UNCONFIRMED:
                    for (Client client : validClients) {
                        if (client.getOperationAccept() != OperationAccept.ACCEPTED) {
                            sum = sum.add(client.getSum());
                        }
                    }
                    break;
            }
            System.out.println(sum);
        }

    }

    private static Action menu() {
        String incorrectChoose = "Пожалуйста, выберите число от 1 до " + Action.values().length;
        while (true) {
            System.out.println("Выберите операцию, которую необходимо выполнить");
            for (Action action : Action.values()) {
                System.out.println((action.ordinal() + 1) + ". " + action.getTitle());
            }
            String str = sc.next();
            try {
                int choose = Integer.parseInt(str);
                if (choose < 1 || choose > Action.values().length) {
                    throw new NumberFormatException("Число должно быть из диапазона");
                }
                return Action.values()[choose - 1];
            } catch (NumberFormatException ex) {
                System.out.println(incorrectChoose);
            }
        }
    }

    private static ValidatedRecord validateRecord(String[] record, int row, String fileName) {
        ArrayList<ViolationEntry> violatedRecords = new ArrayList<>();
        if (record.length != Field.fieldsCount()) {
            ViolationEntry entry = new ViolationEntry(Field.RECORD, row, fileName,
                    "Количество полей в записи не соответствует необходимому: " + record.length + "/" + Field.fieldsCount());
            violatedRecords.add(entry);
            return new ValidatedRecord(violatedRecords, null);
        }
        Client validClient = new Client();

        String id = CharMatcher.inRange('0', '9').retainFrom(record[Field.ID.getCsvOrdinal()]);
        if (id.isEmpty()) {
            violatedRecords.add(new ViolationEntry(Field.ID, row, fileName, "Идентификационный номер клиента не корректен"));
        }
        validClient.setId(id);

        String cardId = CharMatcher.inRange('0', '9').retainFrom(record[Field.CARD_ID.getCsvOrdinal()]);
        if (cardId.isEmpty()) {
            violatedRecords.add(new ViolationEntry(Field.CARD_ID, row, fileName, "Идентификационный номер карты не корректен"));
        }
        validClient.setCardId(cardId);

        SimpleDateFormat formatterDate = new SimpleDateFormat("dd-M-yyyy", Locale.getDefault());
        try {
            Date date = formatterDate.parse(record[Field.DATE.getCsvOrdinal()]);
            if (date.after(new Date())) {
                violatedRecords.add(new ViolationEntry(Field.DATE, row, fileName, "Дата операции не может быть в будущем"));
            } else {
                validClient.setDate(date);
            }
        } catch (ParseException e) {
            violatedRecords.add(new ViolationEntry(Field.DATE, row, fileName, "Дата операции не соответствует формату: 'ДД-ММ-ГГГГ'"));
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
                    violatedRecords.add(new ViolationEntry(Field.TIME, row, fileName, "Дата и время операции не могут быть в будущем"));
                } else {
                    validClient.setTime(time);
                }
            }
        } catch (ParseException e) {
            violatedRecords.add(new ViolationEntry(Field.TIME, row, fileName, "Время операции не соответствует формату: 'ЧЧ:ММ'"));
        }

        try {
            validClient.setOperationType(OperationType.valueOf(record[Field.OPERATION_TYPE.getCsvOrdinal()]));
        } catch (IllegalArgumentException ex) {
            violatedRecords.add(new ViolationEntry(Field.OPERATION_TYPE, row, fileName, "Тип операции должен быть один из перечисленных: 'Списание', 'Зачисление'"));
        }

        try {
            BigDecimal sum = new BigDecimal(record[Field.SUM.getCsvOrdinal()]);
            if (sum.compareTo(BigDecimal.ZERO) < 0) {
                violatedRecords.add(new ViolationEntry(Field.SUM, row, fileName, "Сумма операции отрицательная"));
            } else {
                validClient.setSum(sum);
            }
        } catch (NumberFormatException ex) {
            violatedRecords.add(new ViolationEntry(Field.SUM, row, fileName, "Сумма операции некорректна"));
        }

        try {
            validClient.setOperationAccept(OperationAccept.valueOf(record[Field.OPERATION_ACCEPT.getCsvOrdinal()]));
        } catch (IllegalArgumentException ex) {
            violatedRecords.add(new ViolationEntry(Field.OPERATION_ACCEPT, row, fileName, "Признак подтверждения операции должен быть один из перечисленных: 'Подтверждена', 'Отклонена', 'Обрабатывается'"));
        }

        return new ValidatedRecord(violatedRecords, validClient);
    }
}

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