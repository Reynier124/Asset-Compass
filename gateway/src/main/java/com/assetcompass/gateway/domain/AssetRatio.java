package com.assetcompass.gateway.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A AssetRatio.
 */
@Table("asset_ratio")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssetRatio implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("ratio")
    private String ratio;

    @NotNull
    @Column("effective_from")
    private LocalDate effectiveFrom;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    private Asset asset;

    @Column("asset_id")
    private UUID assetId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public AssetRatio id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRatio() {
        return this.ratio;
    }

    public AssetRatio ratio(String ratio) {
        this.setRatio(ratio);
        return this;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public LocalDate getEffectiveFrom() {
        return this.effectiveFrom;
    }

    public AssetRatio effectiveFrom(LocalDate effectiveFrom) {
        this.setEffectiveFrom(effectiveFrom);
        return this;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public AssetRatio setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Asset getAsset() {
        return this.asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        this.assetId = asset != null ? asset.getId() : null;
    }

    public AssetRatio asset(Asset asset) {
        this.setAsset(asset);
        return this;
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
        if (!(o instanceof AssetRatio)) {
            return false;
        }
        return getId() != null && getId().equals(((AssetRatio) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AssetRatio{" +
            "id=" + getId() +
            ", ratio='" + getRatio() + "'" +
            ", effectiveFrom='" + getEffectiveFrom() + "'" +
            "}";
    }
}
