/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.fixture;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * When there are no more lines to read from the source reader, this implementation waits util new content is available.
 * We need this kind of thing because when gc log is used by a forked process (e.g. the daemon) there is a delay
 * between a) forked process has finished and b) the gc log information has the final heap information.
 */
public class WaitingReader {

    private final BufferedReader reader;
    private final int timeoutMs;
    private final int clockTick;

    //for testing
    int retriedCount;

    public WaitingReader(BufferedReader reader) {
        this(reader, 5000, 200);
    }

    public WaitingReader(BufferedReader reader, int timeoutMs, int clockTick) {
        this.reader = reader;
        this.timeoutMs = timeoutMs;
        this.clockTick = clockTick;
    }

    String readLine() throws IOException {
        long upTo = System.currentTimeMillis() + timeoutMs;
        String line = reader.readLine();
        while(line == null && System.currentTimeMillis() < upTo) {
            try {
                Thread.sleep(clockTick);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            retriedCount++;
            line = reader.readLine();
        }
        return line;
    }
}
