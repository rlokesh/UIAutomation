package common.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by akhilesh.singh on 4/12/16.
 */
public class LoadGenerator {
    private static Logger logger = LoggerFactory.getLogger(LoadGenerator.class);

    private final String collectorUrl;
    private volatile LoadGeneratorRunnable runnable;
    private static CommandLineExecutor commandLineExecutor;

    public LoadGenerator(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }

    public void start(String appKey) {
        if (runnable != null) {
            runnable.stop();
        }
        runnable = new LoadGeneratorRunnable(collectorUrl, appKey);
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        if (runnable != null) {
            runnable.stop();
        }
    }

    public boolean isRunning() {
        return runnable != null;
    }

    public void blockUntilLoadGeneratedFor(String appKey, int minutes) {
        if (! isRunning()) {
            logger.info("load is running in background for " + minutes);
            start(appKey);
        }
        long untilMillis = runnable.starTimeMillis + (minutes * 60 * 1000L);
        while (System.currentTimeMillis() < untilMillis) {
            try {
                Thread.sleep(10 * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class LoadGeneratorRunnable implements Runnable {
        private final String appKey;
        private final String collectorUrl;
        private final long starTimeMillis = System.currentTimeMillis();
        private volatile boolean stopped = false;

        LoadGeneratorRunnable(String collectorUrl, String appKey) {
            this.collectorUrl = collectorUrl;
            this.appKey = appKey;
        }

        @Override
        public void run() {
            while (! stopped) {
                sendBeacon();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stop() {
            stopped = true;
            String commandTemplate = "pkill -f";
            String task = "loadgen";
            commandLineExecutor = new CommandLineExecutor(commandTemplate);
            commandLineExecutor.setTaskNames(task);
            commandLineExecutor.run();
        }

        void sendBeacon() {

            // TODO: Currently using loadgen. will replace with simple beacon once done.
            String commandTemplate = "../../../gradlew -p ../../../eum-cloud -Dbeacon.host.url=%s -Dloadgen.appkey=%s";
            String task = "loadgen";
            commandLineExecutor = new CommandLineExecutor(String.format(commandTemplate, collectorUrl, appKey));
            commandLineExecutor.setTaskNames(task);
            commandLineExecutor.withWatchDog();
            commandLineExecutor.run();
        }
    }

}

