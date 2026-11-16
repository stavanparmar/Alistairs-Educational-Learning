package com.alistair.learning

import android.content.Context
import android.content.SharedPreferences

class AndroidChildProfileStore(context: Context) : ChildProfileStore {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(): ChildProfile? {
        val name = preferences.getString(KEY_NAME, null) ?: return null
        val age = preferences.getInt(KEY_AGE, -1)
        if (name.isBlank() || age !in 3..10) return null
        return ChildProfile(name, age)
    }

    override fun save(profile: ChildProfile): Boolean =
        preferences.edit()
            .putString(KEY_NAME, profile.displayName)
            .putInt(KEY_AGE, profile.age)
            .commit()

    private companion object {
        const val PREFERENCES_NAME = "child_profile"
        const val KEY_NAME = "display_name"
        const val KEY_AGE = "age"
    }
}
