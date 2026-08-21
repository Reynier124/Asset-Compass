package com.assetcompass.gateway.domain.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.gateway.domain.Asset} entity. This class is used
 * in {@link com.assetcompass.gateway.web.rest.AssetResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /assets?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssetCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter ticket;

    private StringFilter category;

    private StringFilter country;

    private StringFilter description;

    private Boolean distinct;

    public AssetCriteria() {}

    public AssetCriteria(AssetCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.ticket = other.optionalTicket().map(StringFilter::copy).orElse(null);
        this.category = other.optionalCategory().map(StringFilter::copy).orElse(null);
        this.country = other.optionalCountry().map(StringFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AssetCriteria copy() {
        return new AssetCriteria(this);
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

    public StringFilter getTicket() {
        return ticket;
    }

    public Optional<StringFilter> optionalTicket() {
        return Optional.ofNullable(ticket);
    }

    public StringFilter ticket() {
        if (ticket == null) {
            setTicket(new StringFilter());
        }
        return ticket;
    }

    public void setTicket(StringFilter ticket) {
        this.ticket = ticket;
    }

    public StringFilter getCategory() {
        return category;
    }

    public Optional<StringFilter> optionalCategory() {
        return Optional.ofNullable(category);
    }

    public StringFilter category() {
        if (category == null) {
            setCategory(new StringFilter());
        }
        return category;
    }

    public void setCategory(StringFilter category) {
        this.category = category;
    }

    public StringFilter getCountry() {
        return country;
    }

    public Optional<StringFilter> optionalCountry() {
        return Optional.ofNullable(country);
    }

    public StringFilter country() {
        if (country == null) {
            setCountry(new StringFilter());
        }
        return country;
    }

    public void setCountry(StringFilter country) {
        this.country = country;
    }

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
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
        final AssetCriteria that = (AssetCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(ticket, that.ticket) &&
            Objects.equals(category, that.category) &&
            Objects.equals(country, that.country) &&
            Objects.equals(description, that.description) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticket, category, country, description, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AssetCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTicket().map(f -> "ticket=" + f + ", ").orElse("") +
            optionalCategory().map(f -> "category=" + f + ", ").orElse("") +
            optionalCountry().map(f -> "country=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
