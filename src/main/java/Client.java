import java.math.*;
import java.util.*;

/**
 * Сущность "Клиент",
 * при редактировании необходимо добавить соответствующие поля в {@link Field}
 */
class Client {
    private String id;
    private String cardId;
    private Date date;
    private Date time;
    private OperationType operationType;
    private BigDecimal sum;
    private OperationAccept operationAccept;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public OperationAccept getOperationAccept() {
        return operationAccept;
    }

    public void setOperationAccept(OperationAccept operationAccept) {
        this.operationAccept = operationAccept;
    }
}
