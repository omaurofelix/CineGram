package com.cinegram.cine

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Toque curto a cada troca de capa na faixa.
 * Usa o gerador do sistema, sem arquivo de áudio. Pode ser trocado por um som próprio depois.
 */
object SoundTick {
    private var tone: ToneGenerator? = null

    var enabled = true

    fun play() {
        if (!enabled) return
        runCatching {
            val generator = tone ?: ToneGenerator(AudioManager.STREAM_MUSIC, 30).also { tone = it }
            generator.startTone(ToneGenerator.TONE_PROP_BEEP, 26)
        }
    }

    fun release() {
        runCatching { tone?.release() }
        tone = null
    }
}
