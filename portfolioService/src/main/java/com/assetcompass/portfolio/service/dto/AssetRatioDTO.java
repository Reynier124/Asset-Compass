package com.assetcompass.portfolio.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.portfolio.domain.AssetRatio} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssetRatioDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private String ratio;

    @NotNull
    private LocalDate effectiveFrom;

    @NotNull
    private AssetDTO asset;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
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
        if (!(o instanceof AssetRatioDTO)) {
            return false;
        }

        AssetRatioDTO assetRatioDTO = (AssetRatioDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, assetRatioDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AssetRatioDTO{" +
            "id='" + getId() + "'" +
            ", ratio='" + getRatio() + "'" +
            ", effectiveFrom='" + getEffectiveFrom() + "'" +
            ", asset=" + getAsset() +
            "}";
    }
}
