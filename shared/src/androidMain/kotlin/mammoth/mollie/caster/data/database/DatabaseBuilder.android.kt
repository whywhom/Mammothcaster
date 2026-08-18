package mammoth.mollie.caster.data.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun databaseBuilder(context: Context): RoomDatabase.Builder<MollieDatabase> = Room.databaseBuilder<MollieDatabase>(
    context = context.applicationContext,
    name = context.applicationContext.getDatabasePath("molliecaster.db").absolutePath,
).setDriver(BundledSQLiteDriver())
