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
 * Asset Compass — Fase 1 — Modelo de datos unificado + arquitectura de microservicios
 *
 * Decisiones de arquitectura:
 * - Split por bounded context de negocio: portfolioService (Fase 1), notificationService (Fase 2),
 * tradingService (Fase 4). notificationService y tradingService no tienen entidades propias
 * todavía — se modelan cuando se aborden esas fases.
 * - Comunicación: REST (síncrona, vía gateway/Feign) + Kafka (eventos asíncronos entre servicios).
 * - Autenticación: OAuth2/OIDC delegado a Keycloak (corre aparte, no lo genera JHipster).
 * - Base de datos: una instancia PostgreSQL por servicio (sin compartir esquema entre servicios).
 * - Service discovery: nativo de Kubernetes (DNS interno de K8s) — sin Eureka/Consul, porque
 * agregar un registry propio sería redundante sobre el descubrimiento que K8s ya resuelve.
 * - Despliegue: manifiestos de Kubernetes generados a partir del bloque `deployment`.
 */
@Table("broker")
@JsonIgnoreProperties(value = { "new" })
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Broker implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column("id")
    private UUID id;

    @NotNull
    @Column("name")
    private String name;

    @org.springframework.data.annotation.Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public UUID getId() {
        return this.id;
    }

    public Broker id(UUID id) {
        this.setId(id);
        return this;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Broker name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    @org.springframework.data.annotation.Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Broker setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Broker)) {
            return false;
        }
        return getId() != null && getId().equals(((Broker) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Broker{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            "}";
    }
}
