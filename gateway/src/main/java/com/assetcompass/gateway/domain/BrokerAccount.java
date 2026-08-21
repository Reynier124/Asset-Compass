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
 * A BrokerAccount.
 */
@Table("broker_account")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BrokerAccount implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("external_account_id")
    private String externalAccountId;

    @Column("display_name")
    private String displayName;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    private Broker broker;

    @Column("broker_id")
    private UUID brokerId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public BrokerAccount id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalAccountId() {
        return this.externalAccountId;
    }

    public BrokerAccount externalAccountId(String externalAccountId) {
        this.setExternalAccountId(externalAccountId);
        return this;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public BrokerAccount displayName(String displayName) {
        this.setDisplayName(displayName);
        return this;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public BrokerAccount setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    public Broker getBroker() {
        return this.broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
        this.brokerId = broker != null ? broker.getId() : null;
    }

    public BrokerAccount broker(Broker broker) {
        this.setBroker(broker);
        return this;
    }

    public UUID getBrokerId() {
        return this.brokerId;
    }

    public void setBrokerId(UUID broker) {
        this.brokerId = broker;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokerAccount)) {
            return false;
        }
        return getId() != null && getId().equals(((BrokerAccount) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BrokerAccount{" +
            "id=" + getId() +
            ", externalAccountId='" + getExternalAccountId() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            "}";
    }
}
