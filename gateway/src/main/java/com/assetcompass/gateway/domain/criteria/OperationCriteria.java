package com.assetcompass.gateway.domain.criteria;

import com.assetcompass.gateway.domain.enumeration.OperationType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.gateway.domain.Operation} entity. This class is used
 * in {@link com.assetcompass.gateway.web.rest.OperationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /operations?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OperationCriteria implements Serializable, Criteria {

    /**
     * Class for filtering OperationType
     */
    public static class OperationTypeFilter extends Filter<OperationType> {

        public OperationTypeFilter() {}

        public OperationTypeFilter(OperationTypeFilter filter) {
            super(filter);
        }

        @Override
        public OperationTypeFilter copy() {
            return new OperationTypeFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private OperationTypeFilter type;

    private LocalDateFilter operationDate;

    private BigDecimalFilter quantity;

    private BigDecimalFilter price;

    private BigDecimalFilter amount;

    private StringFilter currency;

    private BigDecimalFilter underlyingPrice;

    private BigDecimalFilter commission;

    private UUIDFilter accountId;

    private UUIDFilter assetId;

    private UUIDFilter closesOperationId;

    private Boolean distinct;

    public OperationCriteria() {}

    public OperationCriteria(OperationCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.type = other.optionalType().map(OperationTypeFilter::copy).orElse(null);
        this.operationDate = other.optionalOperationDate().map(LocalDateFilter::copy).orElse(null);
        this.quantity = other.optionalQuantity().map(BigDecimalFilter::copy).orElse(null);
        this.price = other.optionalPrice().map(BigDecimalFilter::copy).orElse(null);
        this.amount = other.optionalAmount().map(BigDecimalFilter::copy).orElse(null);
        this.currency = other.optionalCurrency().map(StringFilter::copy).orElse(null);
        this.underlyingPrice = other.optionalUnderlyingPrice().map(BigDecimalFilter::copy).orElse(null);
        this.commission = other.optionalCommission().map(BigDecimalFilter::copy).orElse(null);
        this.accountId = other.optionalAccountId().map(UUIDFilter::copy).orElse(null);
        this.assetId = other.optionalAssetId().map(UUIDFilter::copy).orElse(null);
        this.closesOperationId = other.optionalClosesOperationId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public OperationCriteria copy() {
        return new OperationCriteria(this);
    }

    public UUIDFilter getId() {
        return id;
    }

    public Optional<UUIDFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public UUIDFilter id() {
        if (id == null) {
            setId(new UUIDFilter());
        }
        return id;
    }

    public void setId(UUIDFilter id) {
        this.id = id;
    }

    public OperationTypeFilter getType() {
        return type;
    }

    public Optional<OperationTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public OperationTypeFilter type() {
        if (type == null) {
            setType(new OperationTypeFilter());
        }
        return type;
    }

    public void setType(OperationTypeFilter type) {
        this.type = type;
    }

    public LocalDateFilter getOperationDate() {
        return operationDate;
    }

    public Optional<LocalDateFilter> optionalOperationDate() {
        return Optional.ofNullable(operationDate);
    }

    public LocalDateFilter operationDate() {
        if (operationDate == null) {
            setOperationDate(new LocalDateFilter());
        }
        return operationDate;
    }

    public void setOperationDate(LocalDateFilter operationDate) {
        this.operationDate = operationDate;
    }

    public BigDecimalFilter getQuantity() {
        return quantity;
    }

    public Optional<BigDecimalFilter> optionalQuantity() {
        return Optional.ofNullable(quantity);
    }

    public BigDecimalFilter quantity() {
        if (quantity == null) {
            setQuantity(new BigDecimalFilter());
        }
        return quantity;
    }

    public void setQuantity(BigDecimalFilter quantity) {
        this.quantity = quantity;
    }

    public BigDecimalFilter getPrice() {
        return price;
    }

    public Optional<BigDecimalFilter> optionalPrice() {
        return Optional.ofNullable(price);
    }

    public BigDecimalFilter price() {
        if (price == null) {
            setPrice(new BigDecimalFilter());
        }
        return price;
    }

    public void setPrice(BigDecimalFilter price) {
        this.price = price;
    }

    public BigDecimalFilter getAmount() {
        return amount;
    }

    public Optional<BigDecimalFilter> optionalAmount() {
        return Optional.ofNullable(amount);
    }

    public BigDecimalFilter amount() {
        if (amount == null) {
            setAmount(new BigDecimalFilter());
        }
        return amount;
    }

    public void setAmount(BigDecimalFilter amount) {
        this.amount = amount;
    }

    public StringFilter getCurrency() {
        return currency;
    }

    public Optional<StringFilter> optionalCurrency() {
        return Optional.ofNullable(currency);
    }

    public StringFilter currency() {
        if (currency == null) {
            setCurrency(new StringFilter());
        }
        return currency;
    }

    public void setCurrency(StringFilter currency) {
        this.currency = currency;
    }

    public BigDecimalFilter getUnderlyingPrice() {
        return underlyingPrice;
    }

    public Optional<BigDecimalFilter> optionalUnderlyingPrice() {
        return Optional.ofNullable(underlyingPrice);
    }

    public BigDecimalFilter underlyingPrice() {
        if (underlyingPrice == null) {
            setUnderlyingPrice(new BigDecimalFilter());
        }
        return underlyingPrice;
    }

    public void setUnderlyingPrice(BigDecimalFilter underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }

    public BigDecimalFilter getCommission() {
        return commission;
    }

    public Optional<BigDecimalFilter> optionalCommission() {
        return Optional.ofNullable(commission);
    }

    public BigDecimalFilter commission() {
        if (commission == null) {
            setCommission(new BigDecimalFilter());
        }
        return commission;
    }

    public void setCommission(BigDecimalFilter commission) {
        this.commission = commission;
    }

    public UUIDFilter getAccountId() {
        return accountId;
    }

    public Optional<UUIDFilter> optionalAccountId() {
        return Optional.ofNullable(accountId);
    }

    public UUIDFilter accountId() {
        if (accountId == null) {
            setAccountId(new UUIDFilter());
        }
        return accountId;
    }

    public void setAccountId(UUIDFilter accountId) {
        this.accountId = accountId;
    }

    public UUIDFilter getAssetId() {
        return assetId;
    }

    public Optional<UUIDFilter> optionalAssetId() {
        return Optional.ofNullable(assetId);
    }

    public UUIDFilter assetId() {
        if (assetId == null) {
            setAssetId(new UUIDFilter());
        }
        return assetId;
    }

    public void setAssetId(UUIDFilter assetId) {
        this.assetId = assetId;
    }

    public UUIDFilter getClosesOperationId() {
        return closesOperationId;
    }

    public Optional<UUIDFilter> optionalClosesOperationId() {
        return Optional.ofNullable(closesOperationId);
    }

    public UUIDFilter closesOperationId() {
        if (closesOperationId == null) {
            setClosesOperationId(new UUIDFilter());
        }
        return closesOperationId;
    }

    public void setClosesOperationId(UUIDFilter closesOperationId) {
        this.closesOperationId = closesOperationId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OperationCriteria that = (OperationCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(type, that.type) &&
            Objects.equals(operationDate, that.operationDate) &&
            Objects.equals(quantity, that.quantity) &&
            Objects.equals(price, that.price) &&
            Objects.equals(amount, that.amount) &&
            Objects.equals(currency, that.currency) &&
            Objects.equals(underlyingPrice, that.underlyingPrice) &&
            Objects.equals(commission, that.commission) &&
            Objects.equals(accountId, that.accountId) &&
            Objects.equals(assetId, that.assetId) &&
            Objects.equals(closesOperationId, that.closesOperationId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            type,
            operationDate,
            quantity,
            price,
            amount,
            currency,
            underlyingPrice,
            commission,
            accountId,
            assetId,
            closesOperationId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OperationCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalOperationDate().map(f -> "operationDate=" + f + ", ").orElse("") +
            optionalQuantity().map(f -> "quantity=" + f + ", ").orElse("") +
            optionalPrice().map(f -> "price=" + f + ", ").orElse("") +
            optionalAmount().map(f -> "amount=" + f + ", ").orElse("") +
            optionalCurrency().map(f -> "currency=" + f + ", ").orElse("") +
            optionalUnderlyingPrice().map(f -> "underlyingPrice=" + f + ", ").orElse("") +
            optionalCommission().map(f -> "commission=" + f + ", ").orElse("") +
            optionalAccountId().map(f -> "accountId=" + f + ", ").orElse("") +
            optionalAssetId().map(f -> "assetId=" + f + ", ").orElse("") +
            optionalClosesOperationId().map(f -> "closesOperationId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
