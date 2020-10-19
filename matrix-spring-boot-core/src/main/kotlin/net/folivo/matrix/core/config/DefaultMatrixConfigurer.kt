package net.folivo.matrix.core.config

import net.folivo.matrix.core.model.events.m.room.*
import net.folivo.matrix.core.model.events.m.room.message.MessageEvent
import net.folivo.matrix.core.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.core.model.events.m.room.message.TextMessageEventContent

class DefaultMatrixConfigurer : MatrixConfigurer {
    override fun configure(config: MatrixConfiguration) {
        // TODO search in package for annotations
        config.configure {
            registerMatrixEvents(
                    CanonicalAliasEvent::class.java,
                    CreateEvent::class.java,
                    JoinRulesEvent::class.java,
                    MemberEvent::class.java,
                    PowerLevelsEvent::class.java,
                    RedactionEvent::class.java,
                    MessageEvent::class.java,
                    NameEvent::class.java,
                    TopicEvent::class.java,
                    AvatarEvent::class.java,
                    PinnedEventsEvent::class.java
            )
            registerMessageEventContents(
                    TextMessageEventContent::class.java,
                    NoticeMessageEventContent::class.java
            )
        }
    }
}