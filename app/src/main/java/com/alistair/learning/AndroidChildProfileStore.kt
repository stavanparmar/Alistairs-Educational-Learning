package com.alistair.learning

import android.content.Context
import android.content.SharedPreferences

class AndroidChildProfileStore(context: Context) :
    ChildProfileStore,
    AudioSettingsStore,
    LearningProgressStore {
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

    override fun loadAudioSettings(): AudioSettings = AudioSettings(
        soundEffectsEnabled = preferences.getBoolean(KEY_SOUND_EFFECTS, true),
        backgroundMusicEnabled = preferences.getBoolean(KEY_BACKGROUND_MUSIC, true)
    )

    override fun saveAudioSettings(settings: AudioSettings): Boolean =
        preferences.edit()
            .putBoolean(KEY_SOUND_EFFECTS, settings.soundEffectsEnabled)
            .putBoolean(KEY_BACKGROUND_MUSIC, settings.backgroundMusicEnabled)
            .commit()

    override fun loadProgress(profile: ChildProfile): LearningProgress {
        val completedStages = LearningModule.values().mapNotNull { module ->
            val stage = preferences.getInt(progressKey(profile, module.name), 0)
            if (stage > 0) module.name to stage else null
        }.toMap()
        return LearningProgress(completedStages)
    }

    override fun saveProgress(profile: ChildProfile, progress: LearningProgress): Boolean {
        val prefix = progressPrefix(profile)
        val editor = preferences.edit()
        preferences.all.keys.filter { it.startsWith(prefix) }.forEach(editor::remove)
        progress.completedStages.forEach { (module, stage) ->
            if (stage > 0) editor.putInt("$prefix$module", stage)
        }
        return editor.commit()
    }

    private fun progressPrefix(profile: ChildProfile): String =
        "progress.${profile.displayName.trim().lowercase()}."

    private fun progressKey(profile: ChildProfile, module: String): String =
        "${progressPrefix(profile)}$module"

    private companion object {
        const val PREFERENCES_NAME = "child_profile"
        const val KEY_NAME = "display_name"
        const val KEY_AGE = "age"
        const val KEY_SOUND_EFFECTS = "audio.sound_effects"
        const val KEY_BACKGROUND_MUSIC = "audio.background_music"
    }
}
