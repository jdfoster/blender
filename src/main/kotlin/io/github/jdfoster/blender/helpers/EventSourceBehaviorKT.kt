package io.github.jdfoster.blender.helpers

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.javadsl.CommandHandler
import akka.persistence.typed.javadsl.EventHandler
import akka.persistence.typed.javadsl.EventSourcedBehavior

abstract class EventSourceBehaviorKT<T, U, V> {
    abstract val entityKind: String

    abstract val emptyState: () -> V

    abstract val commandHandler: CommandHandler<T, U, V>

    abstract val eventHandler: EventHandler<V, U>

    operator fun invoke(entityId: String): Behavior<T> =
        object : EventSourcedBehavior<T, U, V>(PersistenceId.of(entityKind, entityId)) {
            override fun emptyState(): V = this@EventSourceBehaviorKT.emptyState()
            override fun commandHandler(): CommandHandler<T, U, V> = this@EventSourceBehaviorKT.commandHandler
            override fun eventHandler(): EventHandler<V, U> = this@EventSourceBehaviorKT.eventHandler
        }
}
