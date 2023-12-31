package edu.fiuba.reservations.application.controller

import edu.fiuba.reservations.delivery.controller.ReservationController
import edu.fiuba.reservations.delivery.dto.request.CreateReservationDTO
import edu.fiuba.reservations.delivery.dto.request.UpdateReservationDTO
import edu.fiuba.reservations.delivery.dto.response.ReservationDTO
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("reservations")
class ReservationResource(
    private val reservationController: ReservationController
) {
    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getReservation(
        @PathVariable id: String
    ): ReservationDTO {
        return reservationController.getReservation(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReservation(
        @RequestBody body: CreateReservationDTO
    ): ReservationDTO {
        return reservationController.createReservation(body)
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateReservation(
        @PathVariable id: String,
        @RequestBody body: UpdateReservationDTO
    ): ReservationDTO {
        return reservationController.updateReservation(id, body)
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReservation(
        @PathVariable id: String
    ) {
        reservationController.deleteReservation(id)
    }
}
