package com.assetcompass.portfolio.domain;

import com.assetcompass.portfolio.domain.enumeration.OperationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Operation.
 */
@Entity
@Table(name = "operation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Operation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private OperationType type;

    @NotNull
    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;

    @NotNull
    @Column(name = "quantity", precision = 21, scale = 2, nullable = false)
    private BigDecimal quantity;

    @NotNull
    @Column(name = "price", precision = 21, scale = 2, nullable = false)
    private BigDecimal price;

    @NotNull
    @Column(name = "amount", precision = 21, scale = 2, nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "underlying_price", precision = 21, scale = 2)
    private BigDecimal underlyingPrice;

    @Column(name = "commission", precision = 21, scale = 2)
    private BigDecimal commission;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "broker" }, allowSetters = true)
    private BrokerAccount account;

    @ManyToOne(optional = false)
    @NotNull
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "account", "asset", "closesOperation" }, allowSetters = true)
    private Operation closesOperation;

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

    public BrokerAccount getAccount() {
        return this.account;
    }

    public void setAccount(BrokerAccount brokerAccount) {
        this.account = brokerAccount;
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
    }

    public Operation closesOperation(Operation operation) {
        this.setClosesOperation(operation);
        return this;
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
