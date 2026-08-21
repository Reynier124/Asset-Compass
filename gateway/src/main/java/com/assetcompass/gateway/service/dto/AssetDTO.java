package com.assetcompass.gateway.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.gateway.domain.Asset} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AssetDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private String ticket;

    private String category;

    private String country;

    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssetDTO)) {
            return false;
        }

        AssetDTO assetDTO = (AssetDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, assetDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AssetDTO{" +
            "id='" + getId() + "'" +
            ", ticket='" + getTicket() + "'" +
            ", category='" + getCategory() + "'" +
            ", country='" + getCountry() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
