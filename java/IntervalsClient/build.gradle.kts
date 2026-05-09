plugins {
    id("java-library")
}

group = "com.intervals.client"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

/* integration tests */

val integrationTest: SourceSet = sourceSets.create("integrationTest") {
    java {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        srcDir("src/integrationTest/java")
    }
    resources.srcDir("src/integrationTest/resources")
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTestTask = tasks.register<Test>("integrationTest") {
    group = "verification"

    useJUnitPlatform()

    testClassesDirs = integrationTest.output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    shouldRunAfter("test")
}

tasks.check {
    dependsOn(integrationTestTask)
}


dependencies {
    // Kafka
    implementation("org.apache.kafka:kafka-clients:4.2.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")

    /* unit tests */
    // Junit
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // AssertJ
    testImplementation("org.assertj:assertj-core:3.27.7")

    /* integration tests */
    // Awaitility
    integrationTestImplementation("org.awaitility:awaitility:4.3.0")

    // Mockito
    integrationTestImplementation("org.mockito:mockito-core:5.14.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
