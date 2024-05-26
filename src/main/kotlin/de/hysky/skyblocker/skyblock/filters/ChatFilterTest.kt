package de.hysky.skyblocker.skyblock.filters

import de.hysky.skyblocker.utils.chat.ChatPatternListener
import de.hysky.skyblocker.utils.chat.ChatPatternListenerTest

open class ChatFilterTest<T : ChatPatternListener?>(listener: T) : ChatPatternListenerTest<T>(listener)
