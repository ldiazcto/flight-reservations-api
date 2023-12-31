package edu.fiuba.reservations.delivery.controller

import edu.fiuba.reservations.application.exception.ExceptionCode.INVALID_RESERVATION_ID
import edu.fiuba.reservations.delivery.dto.request.CreateReservationDTO
import edu.fiuba.reservations.delivery.dto.request.UpdateReservationDTO
import edu.fiuba.reservations.delivery.dto.request.builder.CreateReservationDTOBuilder
import edu.fiuba.reservations.delivery.dto.request.builder.UpdateReservationDTOBuilder
import edu.fiuba.reservations.delivery.dto.response.ReservationDTO
import edu.fiuba.reservations.delivery.validator.CreateReservationDTOValidator
import edu.fiuba.reservations.delivery.validator.UpdateReservationDTOValidator
import edu.fiuba.reservations.domain.exception.BadRequestException
import edu.fiuba.reservations.infrastructure.service.ReservationService
import edu.fiuba.reservations.utils.Constants.RESERVATION_ID_LENGTH

class ReservationController(
    private val reservationService: ReservationService
) {
    fun getReservation(id: String): ReservationDTO {
        validateReservationId(id)

        return ReservationDTO(reservationService.getReservation(id).second)
    }

    fun createReservation(body: CreateReservationDTO): ReservationDTO {
        val validatedCreateReservation = CreateReservationDTOBuilder(CreateReservationDTOValidator()).build(body)

        return ReservationDTO(reservationService.createReservation(validatedCreateReservation))
    }

    fun updateReservation(id: String, body: UpdateReservationDTO): ReservationDTO {
        validateReservationId(id)

        val validatedUpdateReservation = UpdateReservationDTOBuilder(UpdateReservationDTOValidator()).build(body)

        return ReservationDTO(reservationService.updateReservation(id, validatedUpdateReservation))
    }

    fun deleteReservation(id: String) {
        validateReservationId(id)

        reservationService.deleteReservation(id)
    }

    private fun validateReservationId(id: String) {
        if (id.length != RESERVATION_ID_LENGTH) {
            throw BadRequestException(
                INVALID_RESERVATION_ID.getMessage(),
                INVALID_RESERVATION_ID.getCode(),
                listOf()
            )
        }
    }
}
