package io.github.lucaspaixaodev.dashboard.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Entity
@Table(name = "garage")
class GarageEntity(

    @Id
    @Column(name = "id")
    val id: UUID,

    @Column(name = "sector")
    val sector: String
)

interface GarageRepository : JpaRepository<GarageEntity, UUID>
