package com.alistair.learning

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIViewController

private class IosChildProfileStore :
    ChildProfileStore,
    AudioSettingsStore,
    LearningProgressStore {
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

    override fun loadAudioSettings(): AudioSettings = AudioSettings(
        soundEffectsEnabled = preferences.objectForKey(KEY_SOUND_EFFECTS) == null ||
            preferences.boolForKey(KEY_SOUND_EFFECTS),
        backgroundMusicEnabled = preferences.objectForKey(KEY_BACKGROUND_MUSIC) == null ||
            preferences.boolForKey(KEY_BACKGROUND_MUSIC)
    )

    override fun saveAudioSettings(settings: AudioSettings): Boolean {
        preferences.setBool(settings.soundEffectsEnabled, forKey = KEY_SOUND_EFFECTS)
        preferences.setBool(settings.backgroundMusicEnabled, forKey = KEY_BACKGROUND_MUSIC)
        return true
    }

    override fun loadProgress(profile: ChildProfile): LearningProgress {
        val completedStages = LearningModule.values().mapNotNull { module ->
            val stage = preferences.integerForKey(progressKey(profile, module.name)).toInt()
            if (stage > 0) module.name to stage else null
        }.toMap()
        return LearningProgress(completedStages)
    }

    override fun saveProgress(profile: ChildProfile, progress: LearningProgress): Boolean {
        LearningModule.values().forEach { module ->
            val stage = progress.completedStages[module.name] ?: 0
            preferences.setInteger(stage.toLong(), forKey = progressKey(profile, module.name))
        }
        return true
    }

    private fun progressKey(profile: ChildProfile, module: String): String =
        "progress.${profile.displayName.trim().lowercase()}.$module"

    private companion object {
        const val KEY_NAME = "child_profile.display_name"
        const val KEY_AGE = "child_profile.age"
        const val KEY_SOUND_EFFECTS = "audio.sound_effects"
        const val KEY_BACKGROUND_MUSIC = "audio.background_music"
    }
}

fun MainViewController(): UIViewController {
    val profileStore = IosChildProfileStore()
    return ComposeUIViewController {
        AlistairApp(
            profileStore = profileStore,
            audioSettingsStore = profileStore,
            learningProgressStore = profileStore
        )
    }
}
