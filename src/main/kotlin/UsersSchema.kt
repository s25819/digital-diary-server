package pl.pjwstk.edu.pl.s25819.server

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedDiaryRecord(val id: Int, val title: String, val content: String, val location: String)

class DiaryService(database: Database) {
    object Diary : Table() {
        val id = integer("id").autoIncrement()
        val title = varchar("title", length = 50)
        val content = text("content")
        val location = varchar("location", length = 50)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Diary)
        }
    }

    suspend fun create(diaryRecord: ExposedDiaryRecord): Int = dbQuery {
        Diary.insert {
            it[title] = diaryRecord.title
            it[content] = diaryRecord.content
            it[location] = diaryRecord.location
        }[Diary.id]
    }

    suspend fun all(): List<ExposedDiaryRecord> {
        return dbQuery {
            Diary.selectAll()
                .map { ExposedDiaryRecord(it[Diary.id], it[Diary.title], it[Diary.content], it[Diary.location]) }
        }
    }

    suspend fun read(id: Int): ExposedDiaryRecord? {
        return dbQuery {
            Diary.selectAll()
                .where { Diary.id eq id }
                .map { ExposedDiaryRecord(it[Diary.id], it[Diary.title], it[Diary.content], it[Diary.location]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, diaryRecord: ExposedDiaryRecord) {
        dbQuery {
            Diary.update({ Diary.id eq id }) {
                it[title] = diaryRecord.title
                it[content] = diaryRecord.content
                it[location] = diaryRecord.location
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Diary.deleteWhere { Diary.id eq id }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

