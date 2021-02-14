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
    private static final List<String> csvFileNames = new LinkedList<>();

    public static void main(String[] args) {
        Action action;
        while ((action = menu()) != Action.EXIT) {
            if (action.isOperation() && csvFileNames.isEmpty()) {
                System.out.println(MessageFormat.format("Список файлов пуст. Добавьте файлы для анализа с помощью пункта меню ''{0}''", Action.ADD_FILE.getTitle()));
                continue;
            }
            switch (action) {
                case ADD_FILE:
                    System.out.print("Введите путь до файла: ");
                    String path = sc.next();
                    File file = new File(path);
                    if (!file.exists()) {
                        System.out.println(MessageFormat.format("Файла ''{0}'' не существует", path));
                    } else if (file.isDirectory()) {
                        System.out.println(MessageFormat.format("''{0}'' является директорией, необходимо указание CSV-файла", path));
                    } else {
                        csvFileNames.add(path);
                        System.out.println(MessageFormat.format("Файл ''{0}'' успешно добавлен в список анализируемых файлов", file.getName()));
                    }
                    break;
                case CLEAR_FILES:
                    csvFileNames.clear();
                    System.out.println("Список файлов очищен");
                    break;
                case CHECK_FILES:
                    if (csvFileNames.isEmpty()) {
                        System.out.println(MessageFormat.format("Список файлов пуст. Добавьте файлы для анализа с помощью пункта меню ''{0}''", Action.ADD_FILE.getTitle()));
                    } else {
                        System.out.println("Список файлов, готовых к анализу:");
                        for (String filename : csvFileNames) {
                            System.out.println("- " + filename);
                        }
                    }
                    break;
                case BALANCE_BY_CARD:
                    System.out.print("Введите карту клиента: ");
                    String card = CharMatcher.inRange('0', '9').retainFrom(sc.next());
                    printAnalyzeResult(analyzeClients((client, analyzeResult) -> {
                        if (client.getOperationAccept() == OperationAccept.ACCEPTED && card.equals(client.getCardId())) {
                            if (client.getOperationType() == OperationType.CREDITING) {
                                analyzeResult = analyzeResult.add(client.getSum());
                            } else {
                                analyzeResult = analyzeResult.subtract(client.getSum());
                            }
                        }
                        return analyzeResult;
                    }), action);
                    break;
                case BALANCE_BY_CLIENT:
                    System.out.print("Введите ID клиента: ");
                    String clientId = CharMatcher.inRange('0', '9').retainFrom(sc.next());
                    printAnalyzeResult(analyzeClients((client, analyzeResult) -> {
                        if (client.getOperationAccept() == OperationAccept.ACCEPTED && clientId.equals(client.getId())) {
                            if (client.getOperationType() == OperationType.CREDITING) {
                                analyzeResult = analyzeResult.add(client.getSum());
                            } else {
                                analyzeResult = analyzeResult.subtract(client.getSum());
                            }
                        }
                        return analyzeResult;
                    }), action);
                    break;
                case SUM_BY_REJECTED:
                    printAnalyzeResult(analyzeClients((client, analyzeResult) -> {
                        if (client.getOperationAccept() == OperationAccept.REJECTED) {
                            analyzeResult = analyzeResult.add(client.getSum());
                        }
                        return analyzeResult;
                    }), action);
                    break;
                case SUM_BY_UNCONFIRMED:
                    printAnalyzeResult(analyzeClients((client, analyzeResult) -> {
                        if (client.getOperationAccept() != OperationAccept.ACCEPTED) {
                            analyzeResult = analyzeResult.add(client.getSum());
                        }
                        return analyzeResult;
                    }), action);
                    break;
                default:
                    throw new RuntimeException(MessageFormat.format("Ошибка в коде: необходимо добавить действие для пункта меню ''{0}''", action.getTitle()));
            }
            System.out.println("--------------------------------------");
        }
    }

    private static void printAnalyzeResult(AnalyzeResult analyzeResult, Action action) {
        System.out.println(MessageFormat.format("Результат выполнения операции ''{0}'': {1};\n" +
                (analyzeResult.getViolations().isEmpty()
                        ? "Ошибок в анализируемых файлах не обнаружено"
                        : "Ошибки при обработке CSV-файлов (записи с ошибками были проигнорированы):\n" +
                        analyzeResult.formatViolations()), action.getTitle(), analyzeResult.getResult()));
    }
    private static AnalyzeResult analyzeClients(ClientAnalyzer analyzer) {
        List<ViolationEntry> entries = new ArrayList<>();
        BigDecimal analyzeResult = BigDecimal.ZERO;
        for (String fileName : csvFileNames) {
            try (HugeCSVReaderAndValidator reader = new HugeCSVReaderAndValidator(fileName)) {
                ValidatedRecord validatedRecord;
                while ((validatedRecord = reader.next()) != null) {
                    List<ViolationEntry> violationsByRecord = validatedRecord.getViolationEntries();
                    if (violationsByRecord.isEmpty()) {
                        analyzeResult = analyzer.analyze(validatedRecord.getClient(), analyzeResult);
                    }
                    entries.addAll(violationsByRecord);
                }
            } catch (IOException e) {
                System.out.println(MessageFormat.format("Ошибка при чтении файла ''{0}'': {1}", fileName, e.getLocalizedMessage()));
            } catch (CsvException e) {
                System.out.println(MessageFormat.format("Ошибка в CSV-формате файла ''{0}'': {1}", fileName, e.getLocalizedMessage()));
            }
        }
        return new AnalyzeResult(analyzeResult, entries);
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

    static class AnalyzeResult {
        private final BigDecimal result;
        private final List<ViolationEntry> violations;

        private String formatViolations() {
            StringBuilder result = new StringBuilder();
            for (ViolationEntry entry : violations) {
                result.append("В файле ''").append(entry.getFileName())
                        .append("'' в строке ").append(entry.getViolationRow())
                        .append(" для поля ''").append(entry.getViolationField().getTitle())
                        .append("'' была допущена ошибка: ").append(entry.getDescription())
                        .append("\n");
            }
            return result.toString();
        }

        public AnalyzeResult(BigDecimal result, List<ViolationEntry> violations) {
            this.result = result;
            this.violations = violations;
        }

        public BigDecimal getResult() {
            return result;
        }

        public List<ViolationEntry> getViolations() {
            return violations;
        }
    }
}

