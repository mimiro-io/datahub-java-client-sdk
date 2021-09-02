plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    api("org.apache.commons:commons-math3:3.6.1")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.5")
    implementation("commons-io:commons-io:2.11.0")
}
