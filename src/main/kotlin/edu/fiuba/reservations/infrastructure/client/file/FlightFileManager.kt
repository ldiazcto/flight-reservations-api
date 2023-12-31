package edu.fiuba.reservations.infrastructure.client.file

import edu.fiuba.reservations.application.exception.ExceptionCode.FLIGHT_NOT_FOUND
import edu.fiuba.reservations.domain.entity.Error
import edu.fiuba.reservations.domain.entity.Flight
import edu.fiuba.reservations.domain.entity.FlightCriteria
import edu.fiuba.reservations.domain.entity.FlightSearch
import edu.fiuba.reservations.domain.enums.AirlineCode
import edu.fiuba.reservations.domain.exception.ResourceNotFoundException
import edu.fiuba.reservations.logger
import edu.fiuba.reservations.utils.ifNotNull
import edu.fiuba.reservations.utils.isBetweenDates
import edu.fiuba.reservations.utils.isNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class FlightFileManager(
    private val filePaths: ArrayList<String>
) {
    private val log by logger()

    fun getFlights(flightCriteria: FlightCriteria): List<FlightSearch> {
        var flights: List<FlightSearch> = listOf()

        runBlocking {
            val deferredFlights = filePaths.map { filePath ->
                async {
                    withContext(Dispatchers.IO) {
                        readFile(filePath, FlightSearch::class.java)
                    }
                }
            }

            withContext(Dispatchers.Default) {
                flights = deferredFlights.awaitAll().flatten()
            }
        }

        return flights.filter {
            isSameAirline(flightCriteria.airline, it.airline) &&
                it.originAirport == flightCriteria.origin &&
                it.destinationAirport == flightCriteria.destination &&
                it.plannedDepartureTime.isBetweenDates(flightCriteria.from, flightCriteria.to) &&
                it.plannedArrivalTime.isBetweenDates(flightCriteria.from, flightCriteria.to)
        }
    }

    fun getFlight(id: String): Flight {
        var flights: List<Flight> = listOf()

        runBlocking {
            val deferredFlights = filePaths.map { filePath ->
                async {
                    withContext(Dispatchers.IO) {
                        readFile(filePath, Flight::class.java)
                    }
                }
            }

            withContext(Dispatchers.Default) {
                flights = deferredFlights.awaitAll().flatten()
            }
        }

        val flight = flights.firstOrNull {
            it.id.equals(id, true)
        }

        if (flight.isNotNull()) {
            return flight!!
        }

        throw ResourceNotFoundException(
            message = FLIGHT_NOT_FOUND.getMessage(),
            errors = listOf(
                Error(FLIGHT_NOT_FOUND)
            )
        )
    }

    private fun <T> readFile(filePath: String, entityClass: Class<T>): List<T> {
        var records: List<T> = listOf()
        var inputStream: InputStream? = null
        val classLoader = javaClass.getClassLoader()

        try {
            val file = File(classLoader.getResource(filePath)?.file ?: String())
            inputStream = FileInputStream(file)

            records = processCsv(inputStream, entityClass)
        } catch (e: FileNotFoundException) {
            log.error("Threw a FileNotFoundException in FlightFileManager::readFile", e)
        }

        inputStream.ifNotNull {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                log.error("Threw a IOException in FlightFileManager::readFile", e)
            }
        }

        return records
    }

    private fun <T> processCsv(inputStream: InputStream, entityClass: Class<T>): List<T> {
        val csvParser = CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setHeader()
            setSkipHeaderRecord(true)
        }.build()

        val records = csvParser.parse(inputStream.reader()).map {
            entityClass.getDeclaredConstructor(CSVRecord::class.java).newInstance(it)
        }

        return records
    }

    private fun isSameAirline(expected: AirlineCode, actual: AirlineCode): Boolean {
        if (expected == AirlineCode.ALL) {
            return true
        }

        return expected == actual
    }
}
