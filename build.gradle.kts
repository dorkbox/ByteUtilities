/*
 * Copyright 2026 dorkbox, llc
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

gradle.startParameter.showStacktrace = ShowStacktrace.ALWAYS   // always show the stacktrace!

plugins {
    id("com.dorkbox.GradleUtils") version "4.5"
    id("com.dorkbox.Licensing") version "3.1"
    id("com.dorkbox.VersionUpdate") version "3.1"
    id("com.dorkbox.GradlePublish") version "2.0"

    kotlin("jvm") version "2.3.0"
}

object Extras {
    // set for the project
    const val description = "Byte manipulation and SHA/xxHash utilities"
    const val group = "com.dorkbox"
    const val version = "2.2"

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
GradleUtils.compileConfiguration(JavaVersion.VERSION_25)

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
    api("com.dorkbox:Updates:1.3")

    val nettyVer = "4.2.9.Final"
    val kryoVer = "5.6.2"


    // listed as compileOnly, since we will be using netty bytebuf utils if we ALREADY are using netty byte buffs. **We don't want a hard dependency.**
    compileOnly("io.netty:netty-buffer:$nettyVer")
    compileOnly("com.esotericsoftware:kryo:$kryoVer")

    // https://github.com/lz4/lz4-java
    // Source: https://mvnrepository.com/artifact/at.yawk.lz4/lz4-java (fork of lz4 to fix CVE)
    compileOnly("at.yawk.lz4:lz4-java:1.10.3")  // for xxHash, optional

    compileOnly("org.tukaani:xz:1.11") // LZMA support, optional

    testImplementation("io.netty:netty-buffer:$nettyVer")
    testImplementation("com.esotericsoftware:kryo:$kryoVer")
    testImplementation("at.yawk.lz4:lz4-java:1.10.2")
    testImplementation("org.tukaani:xz:1.11")
    testImplementation("junit:junit:4.13.2")
}

mavenCentral {
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
