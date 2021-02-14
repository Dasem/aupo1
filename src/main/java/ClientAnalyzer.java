import java.math.*;

/**
 * Интерфейс для добавления результата рассчёта информации по клиенту в общую сумму
 */
@FunctionalInterface
public interface ClientAnalyzer {
    BigDecimal analyze(Client client, BigDecimal analyzeResult);
}
