/*
 * Copyright 2023 dorkbox, llc
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
import org.gradle.kotlin.dsl.GradleUtils

gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS   // always show the stacktrace!

plugins {
    id("com.dorkbox.GradleUtils") version "3.17"
    id("com.dorkbox.Licensing") version "2.25"
    id("com.dorkbox.VersionUpdate") version "2.8"
    id("com.dorkbox.GradlePublish") version "1.18"

    kotlin("jvm") version "1.8.0"
}

object Extras {
    // set for the project
    const val description = "Byte manipulation and SHA/xxHash utilities"
    const val group = "com.dorkbox"
    const val version = "1.13"

    // set as project.ext
    const val name = "ByteUtilities"
    const val id = "ByteUtilities"
    const val vendor = "Dorkbox LLC"
    const val vendorUrl = "https://dorkbox.com"
    const val url = "https://git.dorkbox.com/dorkbox/ByteUtilities"
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
        attributes["Implementation-Version"] = GradleUtils.now()
        attributes["Implementation-Vendor"] = Extras.vendor
    }
}

dependencies {
    api("com.dorkbox:Updates:1.1")

    // listed as compileOnly, since we will be using netty bytebuf utils if we ALREADY are using netty byte buffs. **We don't want a hard dependency.**
    compileOnly("io.netty:netty-buffer:4.1.96.Final")
    compileOnly("com.esotericsoftware:kryo:5.5.0")

    // https://github.com/lz4/lz4-java
    compileOnly("org.lz4:lz4-java:1.8.0")  // for xxHash, optional

    compileOnly("org.tukaani:xz:1.9") // LZMA support, optional

    testImplementation("io.netty:netty-buffer:4.1.96.Final")
    testImplementation("com.esotericsoftware:kryo:5.5.0")
    testImplementation("org.lz4:lz4-java:1.8.0")
    testImplementation("junit:junit:4.13.2")
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
