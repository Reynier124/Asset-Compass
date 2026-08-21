package com.assetcompass.gateway.domain.criteria;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.assetcompass.gateway.domain.AssetRatio} entity. This class is used
 * in {@link com.assetcompass.gateway.web.rest.AssetRatioResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /asset-ratios?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssetRatioCriteria implements Serializable, Criteria {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUIDFilter id;

    private StringFilter ratio;

    private LocalDateFilter effectiveFrom;

    private UUIDFilter assetId;

    private Boolean distinct;

    public AssetRatioCriteria() {}

    public AssetRatioCriteria(AssetRatioCriteria other) {
        this.id = other.optionalId().map(UUIDFilter::copy).orElse(null);
        this.ratio = other.optionalRatio().map(StringFilter::copy).orElse(null);
        this.effectiveFrom = other.optionalEffectiveFrom().map(LocalDateFilter::copy).orElse(null);
        this.assetId = other.optionalAssetId().map(UUIDFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public AssetRatioCriteria copy() {
        return new AssetRatioCriteria(this);
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

    public StringFilter getRatio() {
        return ratio;
    }

    public Optional<StringFilter> optionalRatio() {
        return Optional.ofNullable(ratio);
    }

    public StringFilter ratio() {
        if (ratio == null) {
            setRatio(new StringFilter());
        }
        return ratio;
    }

    public void setRatio(StringFilter ratio) {
        this.ratio = ratio;
    }

    public LocalDateFilter getEffectiveFrom() {
        return effectiveFrom;
    }

    public Optional<LocalDateFilter> optionalEffectiveFrom() {
        return Optional.ofNullable(effectiveFrom);
    }

    public LocalDateFilter effectiveFrom() {
        if (effectiveFrom == null) {
            setEffectiveFrom(new LocalDateFilter());
        }
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateFilter effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
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
        final AssetRatioCriteria that = (AssetRatioCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(ratio, that.ratio) &&
            Objects.equals(effectiveFrom, that.effectiveFrom) &&
            Objects.equals(assetId, that.assetId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ratio, effectiveFrom, assetId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AssetRatioCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalRatio().map(f -> "ratio=" + f + ", ").orElse("") +
            optionalEffectiveFrom().map(f -> "effectiveFrom=" + f + ", ").orElse("") +
            optionalAssetId().map(f -> "assetId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
