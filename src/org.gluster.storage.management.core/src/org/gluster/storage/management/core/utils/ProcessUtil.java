/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.utils;

import static org.gluster.storage.management.core.constants.CoreConstants.NEWLINE;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;


/**
 * Utility class for creating processes (foreground/background) with given
 * command and checking the output/exit status
 */
public class ProcessUtil {

    public static ProcessResult executeCommand(List<String> command) {
        return executeCommand(true, command);
    }

	/**
	 * Waits till all the threads in given list are dead
	 * @param threads Threads to watch
	 * @throws InterruptedException
	 */
	public static void waitForThreads(List<Thread> threads) throws InterruptedException {
		// Wait till all servers have been processed
		for (int i = threads.size() - 1; i >= 0; i--) {
			if (threads.get(i).isAlive()) {
				// thread alive. sleep for half a second and check again.
				Thread.sleep(500);
				i++; // check the same thread in next iteration
			}
		}
	}

    /**
     * Executes given command in a separate process in FOREGROUND
     * @param command
     * @return {@link ProcessResult} object
     */
    public static ProcessResult executeCommand(String... command) {
        ArrayList<String> commandList = new ArrayList<String>();
        for (String part : command) {
            commandList.add(part);
        }
        return executeCommand(commandList);
    }

    /**
     * Executes given command in foreground/background
     * @param runInForeground Boolean flag indicating whether the command should
     * be executed in foreground
     * @param command
     * @return {@link ProcessResult} object
     */
    public static ProcessResult executeCommand(boolean runInForeground, List<String> command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            if (runInForeground) {
                process.waitFor();      // Wait for process to finish

                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;

                while ((line = br.readLine()) != null) {
                    output.append(line);
                    output.append(NEWLINE);
                }
                br.close();
                isr.close();
                is.close();
            } else {
                output.append("Command [");
                output.append(command);
                output.append("] triggerred in background.");
            }

            return new ProcessResult(process.exitValue(), output.toString());
        } catch (Throwable e) {
			throw new GlusterRuntimeException("Exception while executing command [" + command + "] : ["
					+ e.getMessage() + "]", e);
        }
    }
}