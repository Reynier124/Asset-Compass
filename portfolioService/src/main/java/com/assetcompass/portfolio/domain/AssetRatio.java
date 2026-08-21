package com.assetcompass.portfolio.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A AssetRatio.
 */
@Entity
@Table(name = "asset_ratio")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssetRatio implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "ratio", nullable = false)
    private String ratio;

    @NotNull
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @ManyToOne(optional = false)
    @NotNull
    private Asset asset;

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

    public Asset getAsset() {
        return this.asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public AssetRatio asset(Asset asset) {
        this.setAsset(asset);
        return this;
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
