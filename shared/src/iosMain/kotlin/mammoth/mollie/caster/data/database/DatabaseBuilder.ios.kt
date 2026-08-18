package mammoth.mollie.caster.data.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun databaseBuilder(): RoomDatabase.Builder<MollieDatabase> {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path ?: error("Application Support directory is unavailable")
    return Room.databaseBuilder<MollieDatabase>(name = "$directory/molliecaster.db").setDriver(BundledSQLiteDriver())
}
