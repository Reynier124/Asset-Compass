package com.assetcompass.gateway.domain.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.gateway.domain.BrokerAccount} entity. This class is used
 * in {@link com.assetcompass.gateway.web.rest.BrokerAccountResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /broker-accounts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BrokerAccountCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter externalAccountId;

    private StringFilter displayName;

    private UUIDFilter brokerId;

    private Boolean distinct;

    public BrokerAccountCriteria() {}

    public BrokerAccountCriteria(BrokerAccountCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.externalAccountId = other.optionalExternalAccountId().map(StringFilter::copy).orElse(null);
        this.displayName = other.optionalDisplayName().map(StringFilter::copy).orElse(null);
        this.brokerId = other.optionalBrokerId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public BrokerAccountCriteria copy() {
        return new BrokerAccountCriteria(this);
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

    public StringFilter getExternalAccountId() {
        return externalAccountId;
    }

    public Optional<StringFilter> optionalExternalAccountId() {
        return Optional.ofNullable(externalAccountId);
    }

    public StringFilter externalAccountId() {
        if (externalAccountId == null) {
            setExternalAccountId(new StringFilter());
        }
        return externalAccountId;
    }

    public void setExternalAccountId(StringFilter externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public StringFilter getDisplayName() {
        return displayName;
    }

    public Optional<StringFilter> optionalDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public StringFilter displayName() {
        if (displayName == null) {
            setDisplayName(new StringFilter());
        }
        return displayName;
    }

    public void setDisplayName(StringFilter displayName) {
        this.displayName = displayName;
    }

    public UUIDFilter getBrokerId() {
        return brokerId;
    }

    public Optional<UUIDFilter> optionalBrokerId() {
        return Optional.ofNullable(brokerId);
    }

    public UUIDFilter brokerId() {
        if (brokerId == null) {
            setBrokerId(new UUIDFilter());
        }
        return brokerId;
    }

    public void setBrokerId(UUIDFilter brokerId) {
        this.brokerId = brokerId;
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
        final BrokerAccountCriteria that = (BrokerAccountCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(externalAccountId, that.externalAccountId) &&
            Objects.equals(displayName, that.displayName) &&
            Objects.equals(brokerId, that.brokerId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, externalAccountId, displayName, brokerId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BrokerAccountCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalExternalAccountId().map(f -> "externalAccountId=" + f + ", ").orElse("") +
            optionalDisplayName().map(f -> "displayName=" + f + ", ").orElse("") +
            optionalBrokerId().map(f -> "brokerId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
