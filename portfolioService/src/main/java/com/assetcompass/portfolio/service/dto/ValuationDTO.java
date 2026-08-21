package com.assetcompass.portfolio.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.portfolio.domain.Valuation} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ValuationDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private LocalDate snapshotDate;

    @NotNull
    private BigDecimal totalValue;

    @NotNull
    private String currency;

    private BrokerAccountDTO account;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValuationDTO)) {
            return false;
        }

        ValuationDTO valuationDTO = (ValuationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, valuationDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ValuationDTO{" +
            "id='" + getId() + "'" +
            ", snapshotDate='" + getSnapshotDate() + "'" +
            ", totalValue=" + getTotalValue() +
            ", currency='" + getCurrency() + "'" +
            ", account=" + getAccount() +
            "}";
    }
}
