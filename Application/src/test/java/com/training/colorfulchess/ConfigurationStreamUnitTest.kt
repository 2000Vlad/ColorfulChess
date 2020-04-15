package com.training.colorfulchess

import com.training.colorfulchess.game.getDefaultConfigurationStream
import com.training.colorfulchess.game.modelvm2.GameConfiguration2
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigurationStreamUnitTest {

    @Test
    fun configurationInputStreamTest() {
        val configuration = GameConfiguration2()
        configuration.deserialize(getDefaultConfigurationStream(), true)
        assert(true)
    }

}