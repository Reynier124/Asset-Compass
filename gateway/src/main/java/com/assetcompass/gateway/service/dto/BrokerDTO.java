package com.assetcompass.gateway.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the {@link com.assetcompass.gateway.domain.Broker} entity.
 */
@Schema(
    description = "Asset Compass — Fase 1 — Modelo de datos unificado + arquitectura de microservicios\n\nDecisiones de arquitectura:\n- Split por bounded context de negocio: portfolioService (Fase 1), notificationService (Fase 2),\ntradingService (Fase 4). notificationService y tradingService no tienen entidades propias\ntodavía — se modelan cuando se aborden esas fases.\n- Comunicación: REST (síncrona, vía gateway/Feign) + Kafka (eventos asíncronos entre servicios).\n- Autenticación: OAuth2/OIDC delegado a Keycloak (corre aparte, no lo genera JHipster).\n- Base de datos: una instancia PostgreSQL por servicio (sin compartir esquema entre servicios).\n- Service discovery: nativo de Kubernetes (DNS interno de K8s) — sin Eureka/Consul, porque\nagregar un registry propio sería redundante sobre el descubrimiento que K8s ya resuelve.\n- Despliegue: manifiestos de Kubernetes generados a partir del bloque `deployment`."
)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BrokerDTO implements Serializable {

    @NotNull
    private UUID id;

    @NotNull
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokerDTO)) {
            return false;
        }

        BrokerDTO brokerDTO = (BrokerDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, brokerDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BrokerDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            "}";
    }
}
