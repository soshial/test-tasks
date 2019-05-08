package lv.chi.giffoid.app

import io.reactivex.Scheduler

interface SchedulerProvider {
    fun ui(): Scheduler

    fun computation(): Scheduler

    fun io(): Scheduler

    fun singleIoDatabase(): Scheduler

    fun singleIoNetwork(): Scheduler
}
