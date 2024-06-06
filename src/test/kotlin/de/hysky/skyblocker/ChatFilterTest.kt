package de.hysky.skyblocker

import de.hysky.skyblocker.utils.chat.ChatPatternListener

open class ChatFilterTest<T : ChatPatternListener>(listener: T) : ChatPatternListenerTest<T>(listener)
