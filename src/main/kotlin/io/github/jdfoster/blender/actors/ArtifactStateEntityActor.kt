package io.github.jdfoster.blender.actors

import akka.actor.typed.ActorRef
import akka.persistence.typed.javadsl.CommandHandler
import akka.persistence.typed.javadsl.EffectFactories
import akka.persistence.typed.javadsl.EventHandler
import io.github.jdfoster.blender.helpers.EventSerializeMarker
import io.github.jdfoster.blender.helpers.EventSourceBehaviorKT
import io.github.jdfoster.blender.helpers.MsgSerializeMarker

typealias ArtifactId = Long
typealias ArtifactInFeed = Boolean
typealias ArtifactRead = Boolean
typealias Mark = String
typealias UserId = String

object ArtifactStateEntityActor :
    EventSourceBehaviorKT<ArtifactStateEntityActor.ArtifactCommand, ArtifactStateEntityActor.ArtifactEvent, ArtifactStateEntityActor.CurrentState>() {
    override val entityKind = "ArtifactState"

    interface BaseId : MsgSerializeMarker {
        val artifactId: ArtifactId
        val userId: UserId
    }

    sealed class ArtifactResponse : MsgSerializeMarker {
        data class Okay(val okay: String = "ok") : ArtifactResponse()
        data class ArtifactReadByUser(val artifactRead: ArtifactRead) : ArtifactResponse()
        data class ArtifactInUserFeed(val artifactInFeed: ArtifactInFeed) : ArtifactResponse()
        data class AllStates(val artifactRead: ArtifactRead, val artifactInFeed: ArtifactInFeed) : ArtifactResponse()
    }

    sealed class ArtifactCommand :
        BaseId {
        data class SetArtifactRead(
            val replyTo: ActorRef<ArtifactResponse.Okay>,
            override val artifactId: ArtifactId,
            override val userId: UserId
        ) : ArtifactCommand()

        data class SetArtifactAddedToUserFeed(
            val replyTo: ActorRef<ArtifactResponse.Okay>,
            override val artifactId: ArtifactId,
            override val userId: UserId
        ) : ArtifactCommand()

        data class SetArtifactRemovedFromUserFeed(
            val replyTo: ActorRef<ArtifactResponse.Okay>,
            override val artifactId: ArtifactId,
            override val userId: UserId
        ) : ArtifactCommand()

        sealed class ArtifactQuery : ArtifactCommand() {
            data class IsArtifactReadByUser(
                val replyTo: ActorRef<ArtifactResponse.ArtifactReadByUser>,
                override val artifactId: ArtifactId,
                override val userId: UserId
            ) : ArtifactQuery()

            data class IsArtifactInUserFeed(
                val replyTo: ActorRef<ArtifactResponse.ArtifactInUserFeed>,
                override val artifactId: ArtifactId,
                override val userId: UserId
            ) : ArtifactQuery()

            data class GetAllStates(
                val replyTo: ActorRef<ArtifactResponse.AllStates>,
                override val artifactId: ArtifactId,
                override val userId: UserId
            ) : ArtifactQuery()
        }
    }

    sealed class ArtifactEvent : EventSerializeMarker {
        data class ArtifactRead(val mark: Mark) : ArtifactEvent()
        object ArtifactAddedToUserFeed : ArtifactEvent()
        object ArtifactRemovedFromUserFeed : ArtifactEvent()
    }

    data class CurrentState(val artifactRead: ArtifactRead = false, val artifactInFeed: ArtifactInFeed = false) :
        MsgSerializeMarker

    override val emptyState = { CurrentState() }

    override val commandHandler = CommandHandler<ArtifactCommand, ArtifactEvent, CurrentState> { state, command ->
        when (command) {
            is ArtifactCommand.SetArtifactRead -> artifactRead(
                command.replyTo,
                state
            )
            is ArtifactCommand.SetArtifactAddedToUserFeed -> artifactAddedToUserFeed(
                command.replyTo,
                state
            )
            is ArtifactCommand.SetArtifactRemovedFromUserFeed -> artifactRemovedFromUserFeed(
                command.replyTo,
                state
            )

            is ArtifactCommand.ArtifactQuery.IsArtifactReadByUser -> getArtifactRead(
                command.replyTo,
                state
            )
            is ArtifactCommand.ArtifactQuery.IsArtifactInUserFeed -> getArtifactInFeed(
                command.replyTo,
                state
            )
            is ArtifactCommand.ArtifactQuery.GetAllStates -> getArtifactState(
                command.replyTo,
                state
            )
        }
    }

    override val eventHandler = EventHandler<CurrentState, ArtifactEvent> { state, event ->
        when (event) {
            is ArtifactEvent.ArtifactRead -> CurrentState(
                true,
                state.artifactInFeed
            )
            is ArtifactEvent.ArtifactAddedToUserFeed -> CurrentState(
                state.artifactRead,
                true
            )
            is ArtifactEvent.ArtifactRemovedFromUserFeed -> CurrentState(
                state.artifactRead,
                false
            )
        }
    }

    private fun artifactRead(replyTo: ActorRef<ArtifactResponse.Okay>, currentState: CurrentState) =
        EffectFactories<ArtifactEvent, CurrentState>()
            .persist(
                ArtifactEvent.ArtifactRead(
                    "blender_blender_blender"
                )
            )
            .thenReply(replyTo) { ArtifactResponse.Okay() }

    private fun artifactAddedToUserFeed(replyTo: ActorRef<ArtifactResponse.Okay>, currentState: CurrentState) =
        EffectFactories<ArtifactEvent, CurrentState>()
            .persist(ArtifactEvent.ArtifactAddedToUserFeed)
            .thenReply(replyTo) { ArtifactResponse.Okay() }

    private fun artifactRemovedFromUserFeed(replyTo: ActorRef<ArtifactResponse.Okay>, currentState: CurrentState) =
        EffectFactories<ArtifactEvent, CurrentState>()
            .persist(ArtifactEvent.ArtifactRemovedFromUserFeed)
            .thenReply(replyTo) { ArtifactResponse.Okay() }

    private fun getArtifactRead(replyTo: ActorRef<ArtifactResponse.ArtifactReadByUser>, currentState: CurrentState) =
        EffectFactories<ArtifactEvent, CurrentState>()
            .reply(
                replyTo,
                ArtifactResponse.ArtifactReadByUser(
                    currentState.artifactRead
                )
            )

    private fun getArtifactInFeed(replyTo: ActorRef<ArtifactResponse.ArtifactInUserFeed>, currentState: CurrentState) =
        EffectFactories<ArtifactEvent, CurrentState>()
            .reply(
                replyTo,
                ArtifactResponse.ArtifactInUserFeed(
                    currentState.artifactInFeed
                )
            )

    private fun getArtifactState(replyTo: ActorRef<ArtifactResponse.AllStates>, currentState: CurrentState) =
        EffectFactories<ArtifactEvent, CurrentState>()
            .reply(
                replyTo,
                ArtifactResponse.AllStates(
                    currentState.artifactRead,
                    currentState.artifactInFeed
                )
            )
}
