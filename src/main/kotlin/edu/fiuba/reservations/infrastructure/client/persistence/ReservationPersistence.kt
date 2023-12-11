package edu.fiuba.reservations.infrastructure.client.persistence

import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.Firestore
import edu.fiuba.reservations.application.exception.ExceptionCode.RESERVATION_NOT_FOUND
import edu.fiuba.reservations.domain.entity.Error
import edu.fiuba.reservations.domain.entity.Reservation
import edu.fiuba.reservations.domain.exception.ReservationException
import edu.fiuba.reservations.domain.exception.ResourceNotFoundException
import edu.fiuba.reservations.infrastructure.client.persistence.entity.ReservationEntity
import edu.fiuba.reservations.infrastructure.client.persistence.mapper.ReservationEntityMapper
import edu.fiuba.reservations.infrastructure.client.persistence.repository.GenericRepositoryImpl
import edu.fiuba.reservations.infrastructure.client.persistence.repository.ReservationRepository

class ReservationPersistence(
    private val collectionName: String,
    private val firestore: Firestore
) : ReservationRepository, GenericRepositoryImpl<ReservationEntity>(ReservationEntity::class.java) {

    fun getReservation(id: String): Reservation {
        return try {
            ReservationEntityMapper.toReservationDomain(
                get(id)
            )
        } catch (e: ReservationException) {
            throw ResourceNotFoundException(
                message = RESERVATION_NOT_FOUND.getMessage(),
                errors = listOf(
                    Error(RESERVATION_NOT_FOUND)
                )
            )
        }
    }

    fun createReservation(reservation: Reservation): Reservation {
        return ReservationEntityMapper.toReservationDomain(
            save(
                ReservationEntityMapper.toReservationEntity(reservation)
            )
        )
    }

    fun deleteReservation(id: String) {
        delete(id)
    }

    override fun getCollection(): CollectionReference {
        return firestore.collection(collectionName)
    }
}
