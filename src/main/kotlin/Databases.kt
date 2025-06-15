package pl.pjwstk.edu.pl.s25819.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    val diaryService = DiaryService(database)

    routing {

        get("/diary/samples") {

            (1..10).forEach() {
                diaryService.create(
                    ExposedDiaryRecord(
                        0, "Diary Record #${it}", "Jakiś tekst", "Warszawa"
                    )
                )
            }

            call.respond(HttpStatusCode.Created, "Generated")
        }

        // Get all records
        get("/diary") {
            val all = diaryService.all()
            call.respond(HttpStatusCode.OK, all)
        }

        // Create a diary record
        post("/diary") {
            val user = call.receive<ExposedDiaryRecord>()
            val id = diaryService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read one diary record
        get("/diary/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val diaryRecord = diaryService.read(id)
            if (diaryRecord != null) {
                call.respond(HttpStatusCode.OK, diaryRecord)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update diary record
        put("/diary/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val diaryRecord = call.receive<ExposedDiaryRecord>()
            diaryService.update(id, diaryRecord)
            call.respond(HttpStatusCode.OK)
        }

        // Delete a diary record
        delete("/diary/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            diaryService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
