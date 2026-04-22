import dev.architectury.pack200.java.Pack200Adapter
import org.apache.commons.lang3.SystemUtils
import org.gradle.kotlin.dsl.java

plugins {
    idea
    java
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("gg.essential.loom") version "0.10.0.+"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val mixinGroup = "$baseGroup.mixin"
val modid: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        accessTransformer("src/main/resources/META-INF/skyfall_at.cfg")
        pack200Provider.set(Pack200Adapter())
        mixinConfig("mixins.skyfall.json")
    }
    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName.set("mixins.skyfall.refmap.json")
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.essential.gg/repository/maven-public/")
    maven("https://repo.essential.gg/repository/maven-releases/")
    maven("https://repo.essential.gg/public")
    maven("https://jitpack.io")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    shadowImpl("org.reflections:reflections:0.10.2") {
        exclude(group = "org.dom4j", module = "dom4j")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "javax.servlet", module = "servlet-api")
        exclude(group = "org.jboss", module = "jboss-vfs")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.slf4j")
    }
    shadowImpl("com.jagrosh:DiscordIPC:0.4")
    compileOnly("org.spongepowered:mixin:0.8.5")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    mustRunAfter("processResources")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)
    filesMatching("mcmod.info") {
        expand(inputs.properties)
    }
    from(sourceSets.main.get().resources.srcDirs) {
        include("mixins.skyfall.json")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.jar {
    archiveBaseName.set(modid)
    archiveClassifier.set("without-deps")
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["FMLAT"] = "skyfall_at.cfg"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.skyfall.json"
        this["TweakOrder"] = "0"
        this["FMLCorePlugin"] = "me.lyric.skyfall.asm.SkyfallCorePlugin"
        this["ModSide"] = "CLIENT"
    }
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveBaseName.set(modid)
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)
    from(tasks.jar.get().outputs.files.map { zipTree(it) })
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["FMLAT"] = "skyfall_at.cfg"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.skyfall.json"
        this["TweakOrder"] = "0"
        this["FMLCorePlugin"] = "me.lyric.skyfall.asm.SkyfallCorePlugin"
        this["ModSide"] = "CLIENT"
    }
    exclude("**/LICENSE.txt", "**/LICENSE", "**/NOTICE", "**/NOTICE.txt", "META-INF/maven/**")
    mergeServiceFiles()
    dependsOn(tasks.jar)

    doLast {
        configurations.forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }
}

tasks.remapJar {
    archiveClassifier.set("")
    archiveBaseName.set(modid)
    input.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
}

tasks.assemble.get().dependsOn(tasks.remapJar)