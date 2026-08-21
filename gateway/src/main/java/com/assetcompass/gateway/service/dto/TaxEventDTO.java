package com.assetcompass.gateway.service.dto;

import com.assetcompass.gateway.domain.enumeration.TaxType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.gateway.domain.TaxEvent} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaxEventDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private TaxType type;

    @NotNull
    private LocalDate taxDate;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String currency;

    @NotNull
    private BrokerAccountDTO account;

    private OperationDTO operation;

    private IncomeEventDTO incomeEvent;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TaxType getType() {
        return type;
    }

    public void setType(TaxType type) {
        this.type = type;
    }

    public LocalDate getTaxDate() {
        return taxDate;
    }

    public void setTaxDate(LocalDate taxDate) {
        this.taxDate = taxDate;
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

    public BrokerAccountDTO getAccount() {
        return account;
    }

    public void setAccount(BrokerAccountDTO account) {
        this.account = account;
    }

    public OperationDTO getOperation() {
        return operation;
    }

    public void setOperation(OperationDTO operation) {
        this.operation = operation;
    }

    public IncomeEventDTO getIncomeEvent() {
        return incomeEvent;
    }

    public void setIncomeEvent(IncomeEventDTO incomeEvent) {
        this.incomeEvent = incomeEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaxEventDTO)) {
            return false;
        }

        TaxEventDTO taxEventDTO = (TaxEventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taxEventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaxEventDTO{" +
            "id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", taxDate='" + getTaxDate() + "'" +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            ", account=" + getAccount() +
            ", operation=" + getOperation() +
            ", incomeEvent=" + getIncomeEvent() +
            "}";
    }
}
