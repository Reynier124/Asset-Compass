package com.assetcompass.portfolio.service.dto;

import com.assetcompass.portfolio.domain.enumeration.IncomeType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.portfolio.domain.IncomeEvent} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IncomeEventDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private IncomeType type;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String currency;

    @NotNull
    private BrokerAccountDTO account;

    @NotNull
    private AssetDTO asset;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IncomeType getType() {
        return type;
    }

    public void setType(IncomeType type) {
        this.type = type;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
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

    public AssetDTO getAsset() {
        return asset;
    }

    public void setAsset(AssetDTO asset) {
        this.asset = asset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IncomeEventDTO)) {
            return false;
        }

        IncomeEventDTO incomeEventDTO = (IncomeEventDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, incomeEventDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IncomeEventDTO{" +
            "id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", eventDate='" + getEventDate() + "'" +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            ", account=" + getAccount() +
            ", asset=" + getAsset() +
            "}";
    }
}
