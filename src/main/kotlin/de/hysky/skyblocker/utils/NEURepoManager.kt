package de.hysky.skyblocker.utils

import com.mojang.brigadier.Command
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import io.github.moulberry.repo.NEURepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.text.Text
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Initializes the NEU repo, which contains item metadata and fairy souls location data. Clones the repo if it does not exist and checks for updates. Use [.runAsyncAfterLoad] to run code after the repo is initialized.
 */
object NEURepoManager {
	private const val REMOTE_REPO_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git"

	/**
	 * Use [.NEU_REPO].
	 */
	private val LOCAL_REPO_DIR = SkyblockerMod.CONFIG_DIR.resolve("item-repo") // TODO rename to NotEnoughUpdates-REPO
	private var REPO_LOADING: Deferred<Boolean>? = null

	val NEU_REPO: NEURepository = NEURepository.of(LOCAL_REPO_DIR)

	/**
	 * Adds command to update repository manually from ingame.
	 *
	 *
	 * TODO A button could be added to the settings menu that will trigger this command.
	 */
	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(
			ClientCommandRegistrationCallback { dispatcher, _ ->
				dispatcher.register(
					ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
						.then(ClientCommandManager.literal("updateRepository").executes {
							SkyblockerMod.globalJob.launch { deleteAndDownloadRepository() }
							Command.SINGLE_SUCCESS
						})
				)
			}
		)
	}

	val isLoading: Boolean
		get() = !REPO_LOADING?.isActive!!

	private fun loadRepository() = SkyblockerMod.globalJob.async {
		try {
			if (Files.isDirectory(LOCAL_REPO_DIR)) {
				Git.open(LOCAL_REPO_DIR.toFile()).use { localRepo ->
					localRepo.pull().setRebase(true).call()
					TextHandler.info("NEU Repository Updated")
				}
			} else {
				Git.cloneRepository().setURI(REMOTE_REPO_URL).setDirectory(LOCAL_REPO_DIR.toFile()).setBranchesToClone(listOf("refs/heads/master")).setBranch("refs/heads/master").call().close()
				TextHandler.info("NEU Repository Downloaded")
			}
			NEU_REPO.reload()
			return@async true
		} catch (e: TransportException) {
			TextHandler.error("Transport operation failed. Most likely unable to connect to the remote NEU repo on GitHub", e)
		} catch (e: RepositoryNotFoundException) {
			TextHandler.warn("Local NEU Repository not found or corrupted, downloading new one", e)
			deleteAndDownloadRepository()
		} catch (e: Exception) {
			TextHandler.error("Encountered unknown exception while initializing NEU Repository", e)
		}
		false
	}

	@OptIn(ExperimentalPathApi::class)
	private fun deleteAndDownloadRepository() {
		if (isLoading) {
			TextHandler.chat(Constants.PREFIX.append(Text.translatable("skyblocker.updateRepository.loading")))
			return
		}
		TextHandler.chat(Constants.PREFIX.append(Text.translatable("skyblocker.updateRepository.start")))

		REPO_LOADING = SkyblockerMod.globalJob.async {
			try {
				ItemRepository.setFilesImported(false)
				LOCAL_REPO_DIR.deleteRecursively()
				TextHandler.chat(Constants.PREFIX.append(Text.translatable("skyblocker.updateRepository.deleted")))
				TextHandler.chat(Constants.PREFIX.append(Text.translatable(if (loadRepository().isCancelled) "skyblocker.updateRepository.failed" else "skyblocker.updateRepository.success")))
				return@async true
			} catch (e: Exception) {
				TextHandler.error("Encountered unknown exception while deleting the NEU repo", e)
				TextHandler.chat(Constants.PREFIX.append(Text.translatable("skyblocker.updateRepository.error")))
				return@async false
			}
		}
	}

	/**
	 * Runs the given runnable after the NEU repo is initialized.
	 * @param runnable the runnable to run
	 * @return a completable future of the given runnable
	 */
	fun runAsyncAfterLoad(runnable: () -> Unit): CompletableFuture<Void> {
		return REPO_LOADING!!.asCompletableFuture().thenRunAsync(runnable)
	}
}
