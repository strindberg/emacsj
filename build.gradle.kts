import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    alias(libs.plugins.changelog)
    alias(libs.plugins.detekt)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
}

group = "com.github.strindberg.emacsj"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter(libs.versions.jupiter)
}

dependencies {
    // No test uses JUnit 4, but the platform test framework touches junit.framework.TestCase.
    testRuntimeOnly(libs.junit)

    intellijPlatform {
        create(IntelliJPlatformType.IntellijIdeaCommunity, providers.gradleProperty("platformVersion")) {}

        bundledPlugins(listOf("com.intellij.java", "org.jetbrains.kotlin"))

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }
}

kotlin {
    jvmToolchain(21)
    // Pinned to the Kotlin stdlib the IDE actually ships (2.1.21 in 2025.2).
    compilerOptions.apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
    compilerOptions.freeCompilerArgs.addAll("-Xjsr305=strict", "-Xreturn-value-checker=full", "-Xcollection-literals")
}

intellijPlatform {
    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels =
            providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    runIde {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Dide.show.tips.on.startup.default.value=false",
                "-Dide.experimental.ui=true",
                "-Didea.trust.all.projects=true",
                "-Dide.plugins.snapshot.on.unload.fail=true",
            )
        }
    }

    prepareSandbox {
        doLast {
            sandboxConfigDirectory.file("options/keymap.xml").get().asFile.writeText(
                """
                    <application>
                      <component name="KeymapManager">
                        <active_keymap name="EmacsJ" />
                      </component>
                    </application>
                  """.trimIndent()
            )
            sandboxConfigDirectory.file("options/ide.general.xml").get().asFile.writeText(
                """
                    <application>
                      <component name="GeneralSettings">
                        <option name="confirmExit" value="false" />
                      </component>
                    </application>
                  """.trimIndent()
            )
        }
    }
}

intellijPlatformTesting.runIde.register("runIde53") {
    type = IntelliJPlatformType.IntellijIdea
    version = "2025.3.6.1"
}

intellijPlatformTesting.runIde.register("runIde61") {
    type = IntelliJPlatformType.IntellijIdea
    version = "2026.1.4"
}

intellijPlatformTesting.runIde.register("runIde62") {
    type = IntelliJPlatformType.IntellijIdea
    version = "2026.2.0.1"
}

changelog {
    groups.empty()
    repositoryUrl = "https://github.com/strindberg/emacsj"
}

kover {
    currentProject {
        sources {
            excludeJava = true
        }
    }
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

detekt {
    config.setFrom(file("$rootDir/detekt2.yml"))
    buildUponDefaultConfig = true
}
