package mammoth.mollie.caster.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun databaseBuilder(): RoomDatabase.Builder<MollieDatabase> {
    val directory = File(System.getProperty("user.home"), ".molliecaster").apply { mkdirs() }
    return Room.databaseBuilder<MollieDatabase>(name = File(directory, "molliecaster.db").absolutePath).setDriver(BundledSQLiteDriver())
}
