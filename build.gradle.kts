import de.undercouch.gradle.tasks.download.Download
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm") version "1.5.21"
    id("org.springframework.boot") version "3.1.3"
    id("org.asciidoctor.jvm.convert") version "4.0.0-alpha.1"
    id("com.github.kt3k.coveralls") version "2.12.2"
    id("io.franzbecker.gradle-lombok") version "5.0.0"
    id("com.github.nbaztec.coveralls-jacoco") version "1.2.16"
    application
    id("de.undercouch.download") version "5.5.0"
    jacoco
    checkstyle
}

repositories {
    mavenCentral()
    maven(url = "https://repo.spring.io/release")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.1.3"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("org.tensorflow:tensorflow-core-platform:0.5.0")
    implementation("commons-io:commons-io:2.13.0")
    implementation("jmimemagic:jmimemagic:0.1.2")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    val snippetsDir by extra { file("build/generated-snippets") }

    jacocoTestReport {
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    asciidoctor {
        doLast {
            copy {
                from(layout.buildDirectory.dir("asciidoc/html5"))
                into(layout.buildDirectory.dir("resources/main/static/docs"))
            }
        }
    }

    checkstyle {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    }

    val inceptionFrozenDownload by register<Download>("downloadInceptionFrozen") {
        onlyIfNewer(true)
        src("https://storage.googleapis.com/download.tensorflow.org/models/inception_v3_2016_08_28_frozen.pb.tar.gz")
        dest(layout.buildDirectory.file("inception_v3_2016_08_28_frozen.pb.tar.gz"))
    }

    val fetchInceptionFrozenModel by register<Copy>("fetchInceptionFrozenModel") {
        dependsOn(inceptionFrozenDownload)
        from(tarTree(inceptionFrozenDownload.dest))
        into("src/main/resources/inception-v3")
    }

    processResources {
        dependsOn(fetchInceptionFrozenModel)
    }

    lombok {
        version = "1.18.28"
        sha256 = ""
    }

    named<BootJar>("bootJar") {
        dependsOn(asciidoctor)
        mainClass.set("com.newsplore.Application")
        launchScript()
    }

    val bootRun by named<BootRun>("bootRun") {
        dependsOn(asciidoctor)
    }
}
jacoco {
    toolVersion = "0.8.9"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

configurations {
    val asciidoctorExtensions by creating
}
