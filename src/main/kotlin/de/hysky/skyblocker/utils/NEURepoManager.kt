package de.hysky.skyblocker.utils

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.hysky.skyblocker.SkyblockerMod
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository
import de.hysky.skyblocker.utils.scheduler.Scheduler
import io.github.moulberry.repo.NEURepository
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import org.apache.commons.lang3.function.Consumers
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/**
 * Initializes the NEU repo, which contains item metadata and fairy souls location data. Clones the repo if it does not exist and checks for updates. Use [.runAsyncAfterLoad] to run code after the repo is initialized.
 */
object NEURepoManager {
	private val LOGGER: Logger = LoggerFactory.getLogger(NEURepoManager::class.java)
	const val REMOTE_REPO_URL: String = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git"

	/**
	 * Use [.NEU_REPO].
	 */
	private val LOCAL_REPO_DIR: Path = SkyblockerMod.CONFIG_DIR.resolve("item-repo") // TODO rename to NotEnoughUpdates-REPO
	private var REPO_LOADING: CompletableFuture<Void>? = loadRepository().thenAccept(Consumers.nop())
	@JvmField
    val NEU_REPO: NEURepository = NEURepository.of(LOCAL_REPO_DIR)

	/**
	 * Adds command to update repository manually from ingame.
	 *
	 *
	 * TODO A button could be added to the settings menu that will trigger this command.
	 */
	fun init() {
		ClientCommandRegistrationCallback.EVENT.register(
			ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
				dispatcher.register(
					ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
						.then(ClientCommandManager.literal("updateRepository").executes { context: CommandContext<FabricClientCommandSource> ->
							deleteAndDownloadRepository(context.source.player)
							Command.SINGLE_SUCCESS
						})
				)
			}
		)
	}

	@JvmStatic
    val isLoading: Boolean
		get() = REPO_LOADING != null && !REPO_LOADING!!.isDone

	private fun loadRepository(): CompletableFuture<Boolean> {
		return CompletableFuture.supplyAsync {
			try {
				if (Files.isDirectory(LOCAL_REPO_DIR)) {
					Git.open(LOCAL_REPO_DIR.toFile()).use { localRepo ->
						localRepo.pull().setRebase(true).call()
						LOGGER.info("[Skyblocker] NEU Repository Updated")
					}
				} else {
					Git.cloneRepository().setURI(REMOTE_REPO_URL).setDirectory(LOCAL_REPO_DIR.toFile()).setBranchesToClone(listOf("refs/heads/master")).setBranch("refs/heads/master").call().close()
					LOGGER.info("[Skyblocker] NEU Repository Downloaded")
				}
				NEU_REPO.reload()
				return@supplyAsync true
			} catch (e: TransportException) {
				LOGGER.error("[Skyblocker] Transport operation failed. Most likely unable to connect to the remote NEU repo on github", e)
			} catch (e: RepositoryNotFoundException) {
				LOGGER.warn("[Skyblocker] Local NEU Repository not found or corrupted, downloading new one", e)
				Scheduler.INSTANCE.schedule({ deleteAndDownloadRepository(MinecraftClient.getInstance().player) }, 1)
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker] Encountered unknown exception while initializing NEU Repository", e)
			}
			false
		}
	}

	private fun deleteAndDownloadRepository(player: PlayerEntity?) {
		if (isLoading) {
			sendMessage(player, Constants.Companion.PREFIX.get().append(Text.translatable("skyblocker.updateRepository.loading")))
			return
		}
		sendMessage(player, Constants.Companion.PREFIX.get().append(Text.translatable("skyblocker.updateRepository.start")))

		REPO_LOADING = CompletableFuture.runAsync {
			try {
				ItemRepository.setFilesImported(false)
				FileUtils.recursiveDelete(LOCAL_REPO_DIR)
				sendMessage(player, Constants.Companion.PREFIX.get().append(Text.translatable("skyblocker.updateRepository.deleted")))
				sendMessage(player, Constants.Companion.PREFIX.get().append(Text.translatable(if (loadRepository().join()) "skyblocker.updateRepository.success" else "skyblocker.updateRepository.failed")))
			} catch (e: Exception) {
				LOGGER.error("[Skyblocker] Encountered unknown exception while deleting the NEU repo", e)
				sendMessage(player, Constants.Companion.PREFIX.get().append(Text.translatable("skyblocker.updateRepository.error")))
			}
		}
	}

	/**
	 * Runs the given runnable after the NEU repo is initialized.
	 * @param runnable the runnable to run
	 * @return a completable future of the given runnable
	 */
    @JvmStatic
    fun runAsyncAfterLoad(runnable: Runnable?): CompletableFuture<Void> {
		return REPO_LOADING!!.thenRunAsync(runnable)
	}

	private fun sendMessage(player: PlayerEntity?, text: Text) {
		player?.sendMessage(text, false)
	}
}
