package com.assetcompass.portfolio.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.portfolio.domain.BrokerAccount} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BrokerAccountDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private String externalAccountId;

    private String displayName;

    @NotNull
    private BrokerDTO broker;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public void setExternalAccountId(String externalAccountId) {
        this.externalAccountId = externalAccountId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BrokerDTO getBroker() {
        return broker;
    }

    public void setBroker(BrokerDTO broker) {
        this.broker = broker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokerAccountDTO)) {
            return false;
        }

        BrokerAccountDTO brokerAccountDTO = (BrokerAccountDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, brokerAccountDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BrokerAccountDTO{" +
            "id='" + getId() + "'" +
            ", externalAccountId='" + getExternalAccountId() + "'" +
            ", displayName='" + getDisplayName() + "'" +
            ", broker=" + getBroker() +
            "}";
    }
}
