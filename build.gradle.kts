/*
 * Copyright 2021 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

///////////////////////////////
//////    PUBLISH TO SONATYPE / MAVEN CENTRAL
////// TESTING : (to local maven repo) <'publish and release' - 'publishToMavenLocal'>
////// RELEASE : (to sonatype/maven central), <'publish and release' - 'publishToSonatypeAndRelease'>
///////////////////////////////
import java.time.Instant

gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS   // always show the stacktrace!

plugins {
    id("com.dorkbox.GradleUtils") version "2.9"
    id("com.dorkbox.Licensing") version "2.9.2"
    id("com.dorkbox.VersionUpdate") version "2.4"
    id("com.dorkbox.GradlePublish") version "1.11"

    kotlin("jvm") version "1.5.21"
}

object Extras {
    // set for the project
    const val description = "Byte manipulation and Unsigned Number Utilities"
    const val group = "com.dorkbox"
    const val version = "1.3"

    // set as project.ext
    const val name = "ByteUtilities"
    const val id = "ByteUtilities"
    const val vendor = "Dorkbox LLC"
    const val vendorUrl = "https://dorkbox.com"
    const val url = "https://git.dorkbox.com/dorkbox/ByteUtilities"

    val buildDate = Instant.now().toString()
}

///////////////////////////////
/////  assign 'Extras'
///////////////////////////////
GradleUtils.load("$projectDir/../../gradle.properties", Extras)
GradleUtils.defaults()
GradleUtils.compileConfiguration(JavaVersion.VERSION_1_8)
GradleUtils.jpms(JavaVersion.VERSION_1_9)

licensing {
    license(License.APACHE_2) {
        description(Extras.description)
        author(Extras.vendor)
        url(Extras.url)

        extra("Byte Utils (UByte, UInteger, ULong, Unsigned, UNumber, UShort)", License.APACHE_2) {
            url("https://github.com/jOOQ/jOOQ/tree/master/jOOQ/src/main/java/org/jooq/types")
            copyright(2017)
            author("Data Geekery GmbH (http://www.datageekery.com)")
            author("Lukas Eder")
            author("Ed Schaller")
            author("Jens Nerche")
            author("Ivan Sokolov")
        }

        extra("Kryo Serialization", License.BSD_3) {
            copyright(2020)
            author("Nathan Sweet")
            url("https://github.com/EsotericSoftware/kryo")
        }

        extra("Kotlin Hex", License.MIT) {
            copyright(2017)
            author("ligi")
            url("https://github.com/komputing/KHex")
        }

        extra("Base58", License.APACHE_2) {
            copyright(2018)
            author("Google Inc")
            author("Andreas Schildbach")
            author("ligi")
            url("https://bitcoinj.github.io")
            url("https://github.com/komputing/KBase58")
        }
    }
}

tasks.jar.get().apply {
    manifest {
        // https://docs.oracle.com/javase/tutorial/deployment/jar/packageman.html
        attributes["Name"] = Extras.name

        attributes["Specification-Title"] = Extras.name
        attributes["Specification-Version"] = Extras.version
        attributes["Specification-Vendor"] = Extras.vendor

        attributes["Implementation-Title"] = "${Extras.group}.${Extras.id}"
        attributes["Implementation-Version"] = Extras.buildDate
        attributes["Implementation-Vendor"] = Extras.vendor

        attributes["Automatic-Module-Name"] = Extras.id
    }
}

dependencies {
    implementation("com.dorkbox:Updates:1.1")

    // listed as compileOnly, since we will be using netty bytebuf utils if we ALREADY are using netty byte buffs. **We don't want a hard dependency.**
    compileOnly("io.netty:netty-buffer:4.1.66.Final")
    compileOnly("com.esotericsoftware:kryo:5.2.0")


    testImplementation("io.netty:netty-buffer:4.1.66.Final")
    testImplementation("com.esotericsoftware:kryo:5.2.0")
    testImplementation("junit:junit:4.13")
}

publishToSonatype {
    groupId = Extras.group
    artifactId = Extras.id
    version = Extras.version

    name = Extras.name
    description = Extras.description
    url = Extras.url

    vendor = Extras.vendor
    vendorUrl = Extras.vendorUrl

    issueManagement {
        url = "${Extras.url}/issues"
        nickname = "Gitea Issues"
    }

    developer {
        id = "dorkbox"
        name = Extras.vendor
        email = "email@dorkbox.com"
    }
}
