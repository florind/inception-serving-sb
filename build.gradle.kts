import de.undercouch.gradle.tasks.download.Download
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    application
    jacoco
    checkstyle
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.asciidoctor)
    alias(libs.plugins.coveralls.jacoco)
    alias(libs.plugins.lombok)
    alias(libs.plugins.downloading)
}

repositories {
    mavenCentral()
    maven(url = "https://repo.spring.io/release")
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.lombok)
    implementation(libs.tensorflow)
    implementation(libs.commons.io)
    implementation(libs.jmimemagic)

    testImplementation(libs.spring.restdocs.mockmvc)
    testImplementation(libs.spring.boot.starter.test)
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
