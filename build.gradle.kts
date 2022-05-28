import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//
// ==============
// ==== SECTION A: RECOMMENDED VARIABLES TO CHANGE ====
/**
 * The name of your mod. Used to create a mod folder name (and the name of your mod, if using auto-updated mod_info.json).
 * Defaults to the name of the mod's folder.
 */
val modName = rootDir.name

/**
 * Where your Starsector game is installed to.
 * Note: On Linux, if you installed Starsector into your home directory, you have to write /home/<user>/ instead of ~/
 */
val starsectorDirectory = "C:/Games/Starsector"

/** Defaults to the name of your mod, with spaces replaced by hyphens. */
val modFolderName = modName.replace(" ", "-")
//
//
// ==============
// ==== SECTION B: USE ONLY IF AUTOMATICALLY CREATING METADATA FILES (default is not to) ====
/** Set below to `true` to automatically create mod_info.json and Version Checker files. */
val shouldAutomaticallyCreateMetadataFiles = true
// Then, if above is set to true, update the rest of the information below in SECTION B.
val modVersion = "1.0.0"
val jarFileName = "${modName.replace(" ", "-")}.jar"
val modId = "exoticatechnologies"
val modAuthor = "President Matt Damon"
val modDescription = "Use and fight against a vast array of technologies installed on ships."
val gameVersion = "0.95.1a-RC6"
val jars = arrayOf("jars/$jarFileName")
val modPlugin = "exoticatechnologies.ETModPlugin"
val isUtilityMod = false
val masterVersionFile = "https://raw.githubusercontent.com/scardwell15/exoticatechnologies/main/$modId.version"
val modThreadId = "00000"
// If using auto-generated mod_info.json, scroll down to SECTION E.2 and find "THIS FILE IS GENERATED BY build.gradle.kts."
// Update the "dependencies" part of mod_info.json with any mod dependencies to be displayed in the Starsector launcher.

//
//
// ==============
// ==== SECTION C: Game paths, probably won't need to change these unless you're on Unix ====
// Note: On Linux, change the below line to `val starsectorCoreDirectory = "${starsectorDirectory}"`
val starsectorCoreDirectory = "${starsectorDirectory}/starsector-core"
val starsectorModDirectory = "${starsectorDirectory}/mods"
val modInModsFolder = File("$starsectorModDirectory/${modFolderName}")

//
//
//
//
//
// ==============
// ==== SECTION D: DEPENDENCIES/LIBS ====
// The dependencies for the mod to *build* (not necessarily to run).
dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    //////////////////////////////////////////
    // SECTION D.1: VANILLA STARSECTOR JARS AND VANILLA DEPENDENCIES
    implementation("com.thoughtworks.xstream:xstream:1.4.10")
    implementation("org.lwjgl.lwjgl:lwjgl:2.9.3")
    implementation("org.lwjgl.lwjgl:lwjgl_util:2.9.3")
    implementation("log4j:log4j:1.2.9")
    implementation("org.json:json:20090211")
    implementation("net.java.jinput:jinput:2.0.7")
    implementation("org.codehaus.janino:janino:3.0.7")
    implementation("starfarer:starfarer-api:1.0.0") // This grabs local files from the /libs folder, see `repositories` block.

    // If the above fails, uncomment this line to use the dependencies in starsector-core instead of getting them from The Internet.
    // compileOnly(fileTree(starsectorCoreDirectory) { include("**/*.jar") })

    //////////////////////////////////////////
    // SECTION D.2: MOD DEPENDENCIES (mods that this mods depends on to compile)
    // Uses all mods in /mods folder to compile (this does not mean the mod requires them to run).
    // LazyLib is needed to use Kotlin, as it provides the Kotlin Runtime, so ensure that that is in your mods folder.
    // IF IT IS TAKING A VERY LONG TIME to index dependencies, try commenting out this `compileOnly` section (which includes ALL mods you have)
    // and adding specific mods instead using the `compileOnly` below, just above section D.3.
    if (File(starsectorModDirectory).exists()) {
        //compileOnly(fileTree(starsectorModDirectory) {
        //    include("**/*.jar")
        //    exclude("**/$jarFileName", "**/lib/*", "**/libs/*")
        //})
        compileOnly(fileTree("$starsectorModDirectory/LazyLib/jars") { include("*.jar") })
        compileOnly(fileTree("$starsectorModDirectory/MagicLib/jars") { include("*.jar") })
        compileOnly(fileTree("$starsectorModDirectory/Console Commands/jars") { include("*.jar") })
        compileOnly(fileTree("$starsectorModDirectory/DroneLib/jars") { include("*.jar") })
        compileOnly(fileTree("$starsectorModDirectory/Nexerelin/jars") { include("*.jar") })
    } else {
        println("$starsectorModDirectory did not exist, not adding mod folder dependencies.")
    }

    // Add any specific library dependencies needed by uncommenting and modifying the below line to point to the folder of the .jar files.
    // All mods in the /mods folder are already included, so this would be for anything outside /mods.
    // compileOnly(fileTree("$starsectorModDirectory/modfolder") { include("*.jar") })

    //////////////////////////////////////////
    // SECTION D.3: KOTLIN DEPENDENCIES
    // Shouldn't need to change anything in SECTION D below here
    val kotlinVersionInLazyLib = "1.5.31"
    // Get kotlin sdk from LazyLib during runtime, only use it here during compile time
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersionInLazyLib")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersionInLazyLib")
}

// ==============
// ==== SECTION E: GRADLE TASKS ====
// You probably won't ever need to change this *except* to add a specific mod dependency in mod_info.json
tasks {
    //////////////////////////////////////////
    // SECTION E.1: BUILD A .JAR
    named<Jar>("jar")
    {
        // Tells Gradle to put the .jar file in the /jars folder.
        destinationDirectory.set(file("$rootDir/jars"))
        // Sets the name of the .jar file.
        archiveFileName.set(jarFileName)
    }

    //////////////////////////////////////////
    // SECTION E.2: CREATES & UPDATES METADATA (MOD_INFO.JSON AND VERSION CHECKER) FILES
    register("create-metadata-files") {
        val version = modVersion.split(".").let { javaslang.Tuple3(it[0], it[1], it[2]) }
        System.setProperty("line.separator", "\n") // Use LF instead of CRLF like a normal person

        if (shouldAutomaticallyCreateMetadataFiles) {
            // Generates a mod_info.json from the variables defined at the top of this script.
            File(projectDir, "mod_info.json")
                .writeText(
                    """
                    # THIS FILE IS GENERATED BY build.gradle.kts. (Note that Starsector's json parser permits `#` for comments)
                    {
                        "id": "${modId}",
                        "name": "${modName}",
                        "author": "${modAuthor}",
                        "utility": "${isUtilityMod}",
                        "version": { "major":"${version._1}", "minor": "${version._2}", "patch": "${version._3}" },
                        "description": "${modDescription}",
                        "gameVersion": "${gameVersion}",
                        "jars":[${jars.joinToString() { "\"$it\"" }}],
                        "modPlugin":"${modPlugin}",
                        "dependencies": [
                            {
                                "id": "lw_lazylib",
                                "name": "LazyLib",
                                # "version": "2.6" # If a specific version or higher is required, include this line
                            }
                        ]
                    }
                """.trimIndent()
                )

            // Generates a Version Checker csv file from the variables defined at the top of this script.
            with(File(projectDir, "data/config/version/version_files.csv")) {
                this.parentFile.mkdirs()
                this.writeText(
                    """
                    version file
                    ${modId}.version

                """.trimIndent()
                )
            }


            // Generates a Version Checker .version file from the variables defined at the top of this script.
            File(projectDir, "${modId}.version")
                .writeText(
                    """
                    # THIS FILE IS GENERATED BY build.gradle.kts.
                    {
                        "masterVersionFile":"${masterVersionFile}",
                        "modName":"${modName}",
                        "modThreadId":${modThreadId},
                        "modVersion":
                        {
                            "major":${version._1},
                            "minor":${version._2},
                            "patch":${version._3}
                        }
                    }
                """.trimIndent()
                )
        }

        // Creates a file with the mod name to tell the Github Actions script the name of the mod.
        // Not needed if not using Github Actions (but doesn't hurt to keep).
        with(File(projectDir, ".github/workflows/mod-folder-name.txt")) {
            this.parentFile.mkdirs()
            this.writeText(modFolderName)
        }
    }

    //////////////////////////////////////////
    // SECTION E.3: COPY TO /MODS
    // If enabled, will copy your mod to the /mods directory when run (and whenever gradle syncs).
    // Disabled by default, as it is not needed if your mod directory is symlinked into your /mods folder.
    register<Copy>("install-mod") {
        val enabled = false;

        if (!enabled) return@register

        println("Installing mod into Starsector mod folder...")

        val destinations = listOf(modInModsFolder)

        destinations.forEach { dest ->
            copy {
                from(projectDir)
                into(dest)
                exclude(".git", ".github", ".gradle", ".idea", ".run", "gradle")
            }
        }
    }

    //Define a task to ZIP archive a set of project files
    register<Zip>("myZip") {
        archiveBaseName.set(modFolderName)
        // Tells Gradle to put the .jar file in the /jars folder.
        destinationDirectory.set(file("$rootDir"))
        from(projectDir)
        include("*", "*/**")
        exclude(".git", ".github", "*.gradle", ".idea", ".run", "gradle*", modFolderName + "*", "build*", "libs")
    }
}

//
// ==============
// ==== SECTION F: SOURCE CODE LOCATIONS ====
sourceSets.main {
    // Add new folder names here, with the path, if your Java source code isn't in /src.
    java.setSrcDirs(listOf("src"))
}
kotlin.sourceSets.main {
    // Add new folder names here, with the path, if your Kotlin source code isn't in /src.
    kotlin.setSrcDirs(listOf("src"))
    // List of where resources (the "data" folder) are.
    resources.setSrcDirs(listOf("data"))
}

// ================
// ==== DANGER ====
// -----DON'T TOUCH STUFF BELOW THIS LINE UNLESS YOU KNOW WHAT YOU'RE DOING  -------------------
plugins {
    kotlin("jvm") version "1.5.0"
    java
}

version = modVersion

repositories {
    maven(url = uri("$projectDir/libs"))
    mavenCentral()
}

// Compile Kotlin to Java 6 bytecode so that Starsector can use it (options are only 6 or 8)
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.6"
}
// Compile Java to Java 7 bytecode so that Starsector can use it
java.sourceCompatibility = JavaVersion.VERSION_1_7
java.targetCompatibility = JavaVersion.VERSION_1_7
