plugins {
	id("fabric-loom") version "1.6-SNAPSHOT"
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin") version "0.5.1"
	kotlin("jvm") version "2.0.0"
}

version = "${project.property("mod_version")}+${project.property("minecraft_version")}"
group = project.property("maven_group")!!

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	flatDir {
		dirs("libs")
	}
	maven("https://maven.terraformersmc.com/releases")
	maven("https://maven.shedaniel.me/")
	maven("https://maven.isxander.dev/releases")
	maven("https://maven.isxander.dev/snapshots") //For minecraft snapshots
	maven("https://repo.maven.apache.org/maven2") {
		name = "Maven Central"
	}
	maven("https://maven.meteordev.org/releases") {
		name = "meteor-maven"
	}
	maven("https://repo.codemc.io/repository/maven-public/") // For Occlusion Culling library
	maven("https://repo.nea.moe/releases") // For neu repoparser
}

dependencies {
	testImplementation("net.fabricmc:fabric-loader-junit:${project.property("loader_version")}")
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
	//Layered Yarn & Mojmap - used to fill in intermediary names
	mappings(loom.layered {
		//Using Mojmap breaks runClient, so uncomment only for snapshots when temp mappings are needed
		//officialMojangMappings()
		mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
	})
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

	// Fabric API
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")

	// YACL
	include(modImplementation("dev.isxander:yet-another-config-lib:${project.property("yacl_version")}-fabric")!!)

	// Mod Menu
	modImplementation("com.terraformersmc:modmenu:${project.property("mod_menu_version")}")

	// REI
	modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${project.property("rei_version")}")
	//modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-fabric:${project.property("rei_version")}")

	// EMI
	modCompileOnly("dev.emi:emi-fabric:${project.property("emi_version")}:api")
	//modLocalRuntime("dev.emi:emi-fabric:${project.property("emi_version")}")

	include(modImplementation("meteordevelopment:discord-ipc:1.1")!!)

	// Occlusion Culling (https://github.com/LogisticsCraft/OcclusionCulling)
	include(implementation("com.logisticscraft:occlusionculling:${project.property("occlusionculling_version")}")!!)

	// NEU RepoParser
	include(implementation("moe.nea:neurepoparser:${project.property("repoparser_version")}")!!)

	// JGit used pull data from the NEU item repo
	include(implementation("org.eclipse.jgit:org.eclipse.jgit:${project.property("jgit_version")}")!!)

	// Apache Commons Math
	include(implementation("org.apache.commons:commons-math3:${project.property("commons_math_version")}")!!)

	modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_language_kotlin_version")}")
}

loom {
	accessWidenerPath = file("src/main/resources/skyblocker.accesswidener")
	mixin.useLegacyMixinAp = false
}

base {
	archivesName = project.property("archives_base_name") as String
}
tasks {
	processResources {
		inputs.property("version", project.version)

		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}
	compileJava {
		options.release = 21
	}
	jar {
		from("LICENSE") {
			rename { "${it}_${base.archivesName.get()}"}
		}
	}
	test {
		useJUnitPlatform()
	}
	kotlin {
		jvmToolchain(21)
	}
}


java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

publishMods {
	file = tasks.remapJar.get().archiveFile
	changelog = System.getenv("CHANGELOG")
	version = "v${project.property("version")}"
	displayName = "Skyblocker $version for ${property("minecraft_version")}"
	modLoaders.add("fabric")
	type = STABLE

	modrinth {
		accessToken = System.getenv("MODRINTH_TOKEN")
		projectId = property("modrinth_id") as String
		minecraftVersions.add(property("minecraft_version") as String)
		announcementTitle = "<:modrinth:1237114573354438696> Download from Modrinth"
		requires("fabric-api")
		optional("modmenu", "rei", "emi")
	}

	curseforge {
		accessToken = System.getenv("CURSEFORGE_TOKEN")
		projectId = property("curseforge_id") as String
		minecraftVersions.add(property("minecraft_version") as String)
		announcementTitle = "<:curseforge:900697838453936149> Download from CurseForge"
		projectSlug = "skyblocker"
		requires("fabric-api")
		optional("roughly-enough-items", "emi")
	}

	discord {
		webhookUrl = System.getenv("DISCORD_WEBHOOK")
		username = "Changelog"
		content = changelog.map { "<@&1134565945482948638>\n## Skyblocker v${property("mod_version")}\n" + it}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}