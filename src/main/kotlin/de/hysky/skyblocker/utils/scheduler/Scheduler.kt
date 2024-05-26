package de.hysky.skyblocker.utils.scheduler

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mojang.brigadier.Command
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A scheduler for running tasks at a later time. Tasks will be run synchronously on the main client thread.
 */
object Scheduler {
	var currentTick = 0
	private val LOGGER: Logger = LoggerFactory.getLogger(Scheduler::class.java)
	private val tasks: Int2ObjectMap<MutableList<ScheduledTask>> = Int2ObjectOpenHashMap()
	private val executors: ExecutorService = Executors.newSingleThreadExecutor(ThreadFactoryBuilder().setNameFormat("Skyblocker-Scheduler-%d").build())

	fun schedule(scheduledTime: Int, task: ScheduledTask) {
		if (scheduledTime >= currentTick) {
			addTask(scheduledTime, task)
		} else {
			LOGGER.warn("Scheduled a task with negative delay")
		}
	}

	/**
	 * Schedules a task to run after a delay.
	 *
	 * @param task  the task to run
	 * @param delay the delay in ticks
	 * @param multithreaded whether to run the task on the schedulers dedicated thread pool
	 */
	fun schedule(delay: Int, multithreaded: Boolean = false, task: () -> Unit) {
		schedule(currentTick + delay, ScheduledTask(multithreaded, task))
	}

	/**
	 * Schedules a task to run every period ticks.
	 *
	 * @param task   the task to run
	 * @param period the period in ticks
	 * @param multithreaded whether to run the task on the schedulers dedicated thread pool
	 */
	fun scheduleCyclic(period: Int, multithreaded: Boolean = false, task: () -> Unit) {
		schedule(currentTick, ScheduledTask(period, true, multithreaded, task))
	}

	/**
	 * Schedules a screen to open in the next tick. Used in commands to avoid screen immediately closing after the command is executed.
	 *
	 * @param screenSupplier the supplier of the screen to open
	 * @see .queueOpenScreenCommand
	 */
	private fun queueOpenScreen(screenSupplier: () -> Screen): Int {
		MinecraftClient.getInstance().send { MinecraftClient.getInstance().setScreen(screenSupplier.invoke()) }
		return Command.SINGLE_SUCCESS
	}

	fun tick() {
		if (tasks.containsKey(currentTick)) {
			val currentTickTasks: List<ScheduledTask> = tasks[currentTick]
			for (i in currentTickTasks.indices) {
				val task = currentTickTasks[i]
				if (!task.runTask()) {
					tasks.computeIfAbsent(currentTick + 1, Int2ObjectFunction { ArrayList() }).add(task)
				}
			}
			tasks.remove(currentTick)
		}
		currentTick += 1
	}

	fun addTask(schedule: Int, scheduledTask: ScheduledTask) {
		if (tasks.containsKey(schedule)) {
			tasks[schedule].add(scheduledTask)
		} else {
			val list: MutableList<ScheduledTask> = ArrayList()
			list.add(scheduledTask)
			tasks.put(schedule, list)
		}
	}

	open class ScheduledTask(private val interval: Int, private val cyclic: Boolean, private val multithreaded: Boolean, val task: () -> Unit)  {
		constructor(multithreaded: Boolean, task: () -> Unit) : this(-1, false, multithreaded, task)

		/**
		 * Runs the task if able.
		 *
		 * @param task the task to run
		 * @return `true` if the task is run, and [false] if task is not run.
		 */
		open fun runTask(): Boolean {
			if (multithreaded) {
				executors.execute(task)
			} else {
				task.invoke()
			}

			return true
		}
	}

	fun queueOpenScreenCommand(screenSupplier: () -> Screen) = Command<FabricClientCommandSource> { queueOpenScreen(screenSupplier) }
}
