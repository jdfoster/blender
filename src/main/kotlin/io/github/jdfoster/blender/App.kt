package io.github.jdfoster.blender

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.sharding.typed.javadsl.ClusterSharding
import akka.cluster.sharding.typed.javadsl.Entity
import akka.cluster.sharding.typed.javadsl.EntityTypeKey
import akka.cluster.typed.Cluster
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.github.jdfoster.blender.actors.ArtifactStateEntityActor
import io.github.jdfoster.blender.actors.ArtifactStateEntityActor.ArtifactCommand
import io.github.jdfoster.blender.actors.ArtifactStateEntityActor.entityKind
import io.github.jdfoster.blender.actors.ClusterListenerActor

object RootBehavior {
    operator fun invoke(port: Int, defaultPort: Int): Behavior<NotUsed> = Behaviors.setup { context ->
        val cluster = Cluster.get(context.system)
        val typeKey = object : EntityTypeKey<ArtifactCommand>() {
            override fun name() = entityKind
        }

        if (cluster.selfMember().hasRole("shared")) {
            ClusterSharding.get(context.system).init(Entity.of(typeKey) {
                ArtifactStateEntityActor(it.entityId)
            })
        }

        if (port == defaultPort) {
            context.spawn(ClusterListenerActor(), "clusterListenerActor")
            context.log.info("Started clusterListenerActor")
        }

        Behaviors.empty()
    }
}


fun <T : NotUsed> startNode(behavior: Behavior<T>, clusterName: String) {
    val instance: Behavior<T> = Behaviors.setup { behavior }
    ActorSystem.create(instance, clusterName)
}

fun main() {
    val appConfig: Config = ConfigFactory.load()
    val clusterName = appConfig.getString("clustering.cluster.name")
    val clusterPort = appConfig.extract<Int>("clustering.port")
    val defaultPort = appConfig.extract<Int>("clustering.defaultPort")
    startNode(
        RootBehavior(
            clusterPort,
            defaultPort
        ), clusterName
    )
}
