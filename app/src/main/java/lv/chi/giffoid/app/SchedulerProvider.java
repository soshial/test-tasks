package lv.chi.giffoid.app;

import io.reactivex.Scheduler;

public interface SchedulerProvider {
    Scheduler ui();

    Scheduler computation();

    Scheduler io();

    Scheduler singleIoDatabase();

    Scheduler singleIoNetwork();
}
