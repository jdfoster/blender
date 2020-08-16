import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    application
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.lightbend.akka.management:akka-management-cluster-bootstrap_2.13:1.0.8")
    implementation("com.lightbend.akka.management:akka-management-cluster-http_2.13:1.0.8")
    implementation("com.lightbend.akka.management:akka-management_2.13:1.0.8")
    implementation("com.typesafe.akka:akka-actor-typed_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-cluster-sharding-typed_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-cluster-typed_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-discovery_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-persistence-query_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-persistence-typed_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-remote_2.13:2.6.8")
    implementation("com.typesafe.akka:akka-serialization-jackson_2.13:2.6.8")
    implementation("io.github.config4k:config4k:0.4.2")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

application {
    mainClassName = "blender.AppKt"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}
