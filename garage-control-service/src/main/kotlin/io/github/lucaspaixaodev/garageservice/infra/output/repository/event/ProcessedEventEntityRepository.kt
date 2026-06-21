package io.github.lucaspaixaodev.garageservice.infra.output.repository.event

import org.springframework.data.jpa.repository.JpaRepository

interface ProcessedEventEntityRepository : JpaRepository<ProcessedEventEntity, String>
