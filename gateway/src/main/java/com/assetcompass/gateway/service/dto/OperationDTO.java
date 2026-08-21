package com.assetcompass.gateway.service.dto;

import com.assetcompass.gateway.domain.enumeration.OperationType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.gateway.domain.Operation} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OperationDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private OperationType type;

    @NotNull
    private LocalDate operationDate;

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal price;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String currency;

    private BigDecimal underlyingPrice;

    private BigDecimal commission;

    @NotNull
    private BrokerAccountDTO account;

    @NotNull
    private AssetDTO asset;

    private OperationDTO closesOperation;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public LocalDate getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(LocalDate operationDate) {
        this.operationDate = operationDate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getUnderlyingPrice() {
        return underlyingPrice;
    }

    public void setUnderlyingPrice(BigDecimal underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BrokerAccountDTO getAccount() {
        return account;
    }

    public void setAccount(BrokerAccountDTO account) {
        this.account = account;
    }

    public AssetDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetDTO asset) {
        this.asset = asset;
    }

    public OperationDTO getClosesOperation() {
        return closesOperation;
    }

    public void setClosesOperation(OperationDTO closesOperation) {
        this.closesOperation = closesOperation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationDTO)) {
            return false;
        }

        OperationDTO operationDTO = (OperationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, operationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OperationDTO{" +
            "id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", operationDate='" + getOperationDate() + "'" +
            ", quantity=" + getQuantity() +
            ", price=" + getPrice() +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            ", underlyingPrice=" + getUnderlyingPrice() +
            ", commission=" + getCommission() +
            ", account=" + getAccount() +
            ", asset=" + getAsset() +
            ", closesOperation=" + getClosesOperation() +
            "}";
    }
}
