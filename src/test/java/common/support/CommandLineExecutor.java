/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.support;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by akhilesh.singh on 1/16/15.
 */
public class CommandLineExecutor implements Runnable {
    Logger logger = LoggerFactory.getLogger(CommandLineExecutor.class);

    private String commandString;
    private String[] taskNames;
    private static long timeInMinute = 5;
    public static DefaultExecutor defaultExecutor = new DefaultExecutor();
    public static ExecuteWatchdog watchdog;

    public CommandLineExecutor(String commandString, String... taskNames) {
        this.commandString = commandString;
        this.taskNames = taskNames;
    }

    public CommandLineExecutor withWatchDog(){
        watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis
                (timeInMinute));
        defaultExecutor.setWatchdog(watchdog);
        return this;
    }

    public CommandLineExecutor withTimeLimit(long timeInMinute) {
        this.timeInMinute = timeInMinute;
        return this;
    }

    @Override
    public void run() {
        for (String task : taskNames) {
            CommandLine commandLine = CommandLine.parse(commandString + " " + task);
            try {
                defaultExecutor.execute(commandLine);
            } catch (IOException e) {
                logger.error(
                        "Error occurred during load generation. Trying again" + e.getMessage());
                //Sometime load gen ends with exception. We need to ignore and retry to generate
                // load.
            }
        }
    }

    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }

    public void setTaskNames(String... taskNames) {
        this.taskNames = taskNames;
    }
}
