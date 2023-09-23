package exoticatechnologies.util

import com.fs.starfarer.api.Global

object MusicController {
    fun startMusic() {
        Global.getSoundPlayer().setSuspendDefaultMusicPlayback(true)
        Global.getSoundPlayer().playCustomMusic(0, 0, "exotica_shop_music", true)
    }

    fun stopMusic() {
        Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false)
        Global.getSoundPlayer().playCustomMusic(1, 0, null, false)
    }
}