import java.math.*;

@FunctionalInterface
public interface ClientAnalyzer {
    BigDecimal analyze(Client client, BigDecimal analyzeResult);
}
