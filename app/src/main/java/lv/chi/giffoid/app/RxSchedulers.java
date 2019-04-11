package lv.chi.giffoid.app;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Executors;


public class RxSchedulers implements SchedulerProvider {
    private final Scheduler singleIoDatabase;
    private final Scheduler singleIoNetwork;

    public RxSchedulers() {
        singleIoDatabase = Schedulers.from(Executors.newSingleThreadExecutor());
        singleIoNetwork = Schedulers.from(Executors.newSingleThreadExecutor());
    }

    @Override
    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    @Override
    public Scheduler computation() {
        return Schedulers.computation();
    }

    @Override
    public Scheduler io() {
        return Schedulers.io();
    }

    @Override
    public Scheduler singleIoDatabase() {
        return singleIoDatabase;
    }

    @Override
    public Scheduler singleIoNetwork() {
        return singleIoNetwork;
    }
}
