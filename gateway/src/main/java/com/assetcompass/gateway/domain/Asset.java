package com.assetcompass.gateway.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A Asset.
 */
@Table("asset")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Asset implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("ticket")
    private String ticket;

    @Column("category")
    private String category;

    @Column("country")
    private String country;

    @Column("description")
    private String description;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Asset id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTicket() {
        return this.ticket;
    }

    public Asset ticket(String ticket) {
        this.setTicket(ticket);
        return this;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getCategory() {
        return this.category;
    }

    public Asset category(String category) {
        this.setCategory(category);
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCountry() {
        return this.country;
    }

    public Asset country(String country) {
        this.setCountry(country);
        return this;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return this.description;
    }

    public Asset description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Asset setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Asset)) {
            return false;
        }
        return getId() != null && getId().equals(((Asset) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Asset{" +
            "id=" + getId() +
            ", ticket='" + getTicket() + "'" +
            ", category='" + getCategory() + "'" +
            ", country='" + getCountry() + "'" +
            ", description='" + getDescription() + "'" +
            "}";
    }
}
