package com.assetcompass.gateway.domain;

import com.assetcompass.gateway.domain.enumeration.IncomeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A IncomeEvent.
 */
@Table("income_event")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IncomeEvent implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("type")
    private IncomeType type;

    @NotNull
    @Column("event_date")
    private LocalDate eventDate;

    @NotNull
    @Column("amount")
    private BigDecimal amount;

    @NotNull
    @Column("currency")
    private String currency;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @org.springframework.data.annotation.Transient
    private Asset asset;

    @Column("account_id")
    private UUID accountId;

    @Column("asset_id")
    private UUID assetId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public IncomeEvent id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IncomeType getType() {
        return this.type;
    }

    public IncomeEvent type(IncomeType type) {
        this.setType(type);
        return this;
    }

    public void setType(IncomeType type) {
        this.type = type;
    }

    public LocalDate getEventDate() {
        return this.eventDate;
    }

    public IncomeEvent eventDate(LocalDate eventDate) {
        this.setEventDate(eventDate);
        return this;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public IncomeEvent amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public IncomeEvent currency(String currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public IncomeEvent setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public BrokerAccount getAccount() {
        return this.account;
    }

    public void setAccount(BrokerAccount brokerAccount) {
        this.account = brokerAccount;
        this.accountId = brokerAccount != null ? brokerAccount.getId() : null;
    }

    public IncomeEvent account(BrokerAccount brokerAccount) {
        this.setAccount(brokerAccount);
        return this;
    }

    public Asset getAsset() {
        return this.asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        this.assetId = asset != null ? asset.getId() : null;
    }

    public IncomeEvent asset(Asset asset) {
        this.setAsset(asset);
        return this;
    }

    public UUID getAccountId() {
        return this.accountId;
    }

    public void setAccountId(UUID brokerAccount) {
        this.accountId = brokerAccount;
    }

    public UUID getAssetId() {
        return this.assetId;
    }

    public void setAssetId(UUID asset) {
        this.assetId = asset;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IncomeEvent)) {
            return false;
        }
        return getId() != null && getId().equals(((IncomeEvent) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IncomeEvent{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", eventDate='" + getEventDate() + "'" +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            "}";
    }
}
