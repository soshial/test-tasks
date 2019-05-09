package lv.chi.giffoid.app

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

import java.util.concurrent.Executors


class RxSchedulers : SchedulerProvider {
    private val singleIoDatabase: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
    private val singleIoNetwork: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    override fun ui(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    override fun computation(): Scheduler {
        return Schedulers.computation()
    }

    override fun io(): Scheduler {
        return Schedulers.io()
    }

    override fun singleIoDatabase(): Scheduler {
        return singleIoDatabase
    }

    override fun singleIoNetwork(): Scheduler {
        return singleIoNetwork
    }
}
