package com.assetcompass.gateway.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.gateway.domain.Rebate} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RebateDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private LocalDate rebateDate;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String currency;

    @NotNull
    private BrokerAccountDTO account;

    private OperationDTO operation;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getRebateDate() {
        return rebateDate;
    }

    public void setRebateDate(LocalDate rebateDate) {
        this.rebateDate = rebateDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RebateDTO)) {
            return false;
        }

        RebateDTO rebateDTO = (RebateDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, rebateDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RebateDTO{" +
            "id='" + getId() + "'" +
            ", rebateDate='" + getRebateDate() + "'" +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            ", account=" + getAccount() +
            ", operation=" + getOperation() +
            "}";
    }
}
