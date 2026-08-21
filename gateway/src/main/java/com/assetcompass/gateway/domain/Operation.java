package com.assetcompass.gateway.domain;

import com.assetcompass.gateway.domain.enumeration.OperationType;
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
 * A Operation.
 */
@Table("operation")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Operation implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("type")
    private OperationType type;

    @NotNull
    @Column("operation_date")
    private LocalDate operationDate;

    @NotNull
    @Column("quantity")
    private BigDecimal quantity;

    @NotNull
    @Column("price")
    private BigDecimal price;

    @NotNull
    @Column("amount")
    private BigDecimal amount;

    @NotNull
    @Column("currency")
    private String currency;

    @Column("underlying_price")
    private BigDecimal underlyingPrice;

    @Column("commission")
    private BigDecimal commission;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @org.springframework.data.annotation.Transient
    private Asset asset;

    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "account", "asset", "closesOperation" }, allowSetters = true)
    private Operation closesOperation;

    @Column("account_id")
    private UUID accountId;

    @Column("asset_id")
    private UUID assetId;

    @Column("closes_operation_id")
    private UUID closesOperationId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Operation id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OperationType getType() {
        return this.type;
    }

    public Operation type(OperationType type) {
        this.setType(type);
        return this;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public LocalDate getOperationDate() {
        return this.operationDate;
    }

    public Operation operationDate(LocalDate operationDate) {
        this.setOperationDate(operationDate);
        return this;
    }

    public void setOperationDate(LocalDate operationDate) {
        this.operationDate = operationDate;
    }

    public BigDecimal getQuantity() {
        return this.quantity;
    }

    public Operation quantity(BigDecimal quantity) {
        this.setQuantity(quantity);
        return this;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Operation price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Operation amount(BigDecimal amount) {
        this.setAmount(amount);
        return this;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return this.currency;
    }

    public Operation currency(String currency) {
        this.setCurrency(currency);
        return this;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getUnderlyingPrice() {
        return this.underlyingPrice;
    }

    public Operation underlyingPrice(BigDecimal underlyingPrice) {
        this.setUnderlyingPrice(underlyingPrice);
        return this;
    }

    public void setUnderlyingPrice(BigDecimal underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }

    public BigDecimal getCommission() {
        return this.commission;
    }

    public Operation commission(BigDecimal commission) {
        this.setCommission(commission);
        return this;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Operation setIsPersisted() {
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

    public Operation account(BrokerAccount brokerAccount) {
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

    public Operation asset(Asset asset) {
        this.setAsset(asset);
        return this;
    }

    public Operation getClosesOperation() {
        return this.closesOperation;
    }

    public void setClosesOperation(Operation operation) {
        this.closesOperation = operation;
        this.closesOperationId = operation != null ? operation.getId() : null;
    }

    public Operation closesOperation(Operation operation) {
        this.setClosesOperation(operation);
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

    public UUID getClosesOperationId() {
        return this.closesOperationId;
    }

    public void setClosesOperationId(UUID operation) {
        this.closesOperationId = operation;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Operation)) {
            return false;
        }
        return getId() != null && getId().equals(((Operation) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Operation{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", operationDate='" + getOperationDate() + "'" +
            ", quantity=" + getQuantity() +
            ", price=" + getPrice() +
            ", amount=" + getAmount() +
            ", currency='" + getCurrency() + "'" +
            ", underlyingPrice=" + getUnderlyingPrice() +
            ", commission=" + getCommission() +
            "}";
    }
}
