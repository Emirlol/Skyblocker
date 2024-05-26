package de.hysky.skyblocker.config

import com.google.gson.JsonParser
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.utils.FileUtils.recursiveDelete
import de.hysky.skyblocker.utils.Http.downloadContent
import de.hysky.skyblocker.utils.Http.sendGetRequest
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ImageRepoLoader {
	private val LOGGER: Logger = LogUtils.getLogger()
	val REPO_DIRECTORY: Path = SkyblockerMod.CONFIG_DIR.resolve("image-repo")
	private const val BRANCH_INFO = "https://api.github.com/repos/SkyblockerMod/Skyblocker-Assets/branches/images"
	private const val REPO_DOWNLOAD = "https://github.com/SkyblockerMod/Skyblocker-Assets/archive/refs/heads/images.zip"
	private const val PLACEHOLDER_HASH = "None!"

	fun init() {
		update(0)
	}

	/**
	 * Attempts to update/load the image repository, if any errors are encountered it will try 3 times.
	 */
	private fun update(retries: Int) {
		CompletableFuture.runAsync({
			if (retries < 3) {
				try {
					val start = System.currentTimeMillis()
					//Retrieve the saved commit hash
					val savedCommitHash = checkSavedCommitData()

					//Fetch the latest commit data
					val response = JsonParser.parseString(sendGetRequest(BRANCH_INFO)).asJsonObject
					val latestCommitHash = response.getAsJsonObject("commit")["sha"].asString

					//Download the repository if there was a new commit
					if (savedCommitHash != latestCommitHash) {
						val `in` = downloadContent(REPO_DOWNLOAD)

						//Delete all directories to clear potentially now unused/old files
						//TODO change this to only delete periodically?
						if (Files.exists(REPO_DIRECTORY)) deleteDirectories()

						ZipInputStream(`in`).use { zis ->
							var entry: ZipEntry
							while ((zis.nextEntry.also { entry = it }) != null) {
								val outputFile = REPO_DIRECTORY.resolve(entry.name)

								if (entry.isDirectory) {
									Files.createDirectories(outputFile)
								} else {
									Files.createDirectories(outputFile.parent)
									Files.copy(zis, outputFile, StandardCopyOption.REPLACE_EXISTING)
								}
							}
						}
						writeCommitData(latestCommitHash)

						val end = System.currentTimeMillis()
						LOGGER.info("[Skyblocker] Successfully updated the Image Respository in {} ms! {} → {}", end - start, savedCommitHash, latestCommitHash)
					} else {
						LOGGER.info("[Skyblocker] The Image Respository is up to date!")
					}
				} catch (e: Exception) {
					LOGGER.error("[Skyblocker] Error while downloading image repo on attempt {}!", retries, e)
					update(retries + 1)
				}
			}
		}, Executors.newVirtualThreadPerTaskExecutor())
	}

	/**
	 * @return The stored hash or the [.PLACEHOLDER_HASH].
	 */
	@Throws(IOException::class)
	private fun checkSavedCommitData(): String {
		val file = REPO_DIRECTORY.resolve("image_repo.json")

		if (Files.exists(file)) {
			Files.newBufferedReader(file).use { reader ->
				val commitData = CommitData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow()
				return commitData.commit
			}
		}

		return PLACEHOLDER_HASH
	}

	/**
	 * Writes the `newHash` into a file to be used to check for repo updates.
	 *
	 * @implNote Checking whether the directory exists or not isn't needed as this is called after all files are written successfully.
	 */
	@Throws(IOException::class)
	private fun writeCommitData(newHash: String) {
		val file = REPO_DIRECTORY.resolve("image_repo.json")
		val commitData = CommitData(newHash, System.currentTimeMillis())

		Files.newBufferedWriter(file).use { writer ->
			SkyblockerMod.GSON.toJson(CommitData.CODEC.encodeStart(JsonOps.INSTANCE, commitData).getOrThrow(), writer)
		}
	}

	/**
	 * Deletes all directories (not files) inside of the [.REPO_DIRECTORY]
	 * @throws IOException
	 */
	@Throws(IOException::class)
	private fun deleteDirectories() {
		Files.list(REPO_DIRECTORY)
			.filter { path: Path? -> Files.isDirectory(path) }
			.forEach { dir: Path ->
				try {
					recursiveDelete(dir)
				} catch (e: Exception) {
					LOGGER.error("[Skyblocker] Encountered an exception while deleting a directory! Path: {}", dir.toAbsolutePath(), e)
				}
			}
	}

	@JvmRecord
	internal data class CommitData(val commit: String, val lastUpdated: Long) {
		companion object {
			val CODEC: Codec<CommitData> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<CommitData> ->
				instance.group(
					Codec.STRING.fieldOf("commit").forGetter(CommitData::commit),
					Codec.LONG.fieldOf("lastUpdated").forGetter(CommitData::lastUpdated)
				)
					.apply(instance) { commit: String, lastUpdated: Long -> CommitData(commit, lastUpdated) }
			}
		}
	}
}
