package com.alistair.learning

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIViewController

private class IosChildProfileStore : ChildProfileStore {
    private val preferences = NSUserDefaults.standardUserDefaults

    override fun load(): ChildProfile? {
        val name = preferences.stringForKey(KEY_NAME) ?: return null
        val age = preferences.integerForKey(KEY_AGE).toInt()
        if (name.isBlank() || age !in 3..10) return null
        return ChildProfile(name, age)
    }

    override fun save(profile: ChildProfile): Boolean {
        preferences.setObject(profile.displayName, forKey = KEY_NAME)
        preferences.setInteger(profile.age.toLong(), forKey = KEY_AGE)
        return true
    }

    private companion object {
        const val KEY_NAME = "child_profile.display_name"
        const val KEY_AGE = "child_profile.age"
    }
}

fun MainViewController(): UIViewController {
    val profileStore = IosChildProfileStore()
    return ComposeUIViewController { AlistairApp(profileStore) }
}
