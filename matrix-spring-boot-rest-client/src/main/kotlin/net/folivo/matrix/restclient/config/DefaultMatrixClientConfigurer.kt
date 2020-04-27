package net.folivo.matrix.restclient.config

import net.folivo.matrix.common.model.events.m.room.*
import net.folivo.matrix.common.model.events.m.room.message.MessageEvent
import net.folivo.matrix.common.model.events.m.room.message.NoticeMessageEventContent
import net.folivo.matrix.common.model.events.m.room.message.TextMessageEventContent
import net.folivo.matrix.restclient.model.events.m.room.*

class DefaultMatrixClientConfigurer : MatrixClientConfigurer {
    override fun configure(config: MatrixClientConfiguration) {
        // TODO search in package for annotations
        config.configure {
            registerMatrixEvents(
                    AliasesEvent::class.java,
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