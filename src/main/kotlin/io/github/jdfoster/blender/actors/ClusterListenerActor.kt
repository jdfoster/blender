package io.github.jdfoster.blender.actors

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.typed.Cluster
import akka.cluster.typed.Subscribe
import io.github.jdfoster.blender.helpers.AbstractBehaviorKT

class ClusterListenerActor(context: ActorContext<ClusterDomainEvent>) :
    AbstractBehaviorKT<ClusterDomainEvent>(context) {
    init {
        val cluster = Cluster.get(context.system)
        cluster.subscriptions().tell(Subscribe(context.self, ClusterDomainEvent::class.java))
        context.log.info("Started actor ${context.self.path()} [${ClusterListenerActor::class.qualifiedName}]")
    }

    override fun onMessage(msg: ClusterDomainEvent): Behavior<ClusterDomainEvent> {
        return when (msg) {
            is ClusterEvent.MemberUp -> {
                context.log.info("Member is up: ${msg.member().address()}")
                Behaviors.same()
            }
            is ClusterEvent.UnreachableMember -> {
                context.log.info("Member detected as unreachable: ${msg.member()}")
                Behaviors.same()
            }
            is ClusterEvent.MemberRemoved -> {
                context.log.info("Member is removed: ${msg.member().address()} after ${msg.previousStatus()}")
                Behaviors.same()
            }
            else -> Behaviors.same()
        }
    }

    companion object {
        operator fun invoke(): Behavior<ClusterDomainEvent> = Behaviors.setup {
            ClusterListenerActor(it)
        }
    }
}
