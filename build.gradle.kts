plugins {
    id("java")
    id("application")
}

application {
    mainClass.set("org.example.Main")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
// https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
// testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0-RC1")

    // testImplementation(platform("org.junit:junit-bom:5.10.0"))
    // testImplementation("org.junit.jupiter:junit-jupiter")
    // testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    // testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.10.0")

}
tasks.test {
    useJUnitPlatform()
}