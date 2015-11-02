/**
 *
 *   Copyright 2014-2015 Barcelona Supercomputing Center (www.bsc.es) All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package integratedtoolkit.nio.worker;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;

public class ThreadPrintStream extends PrintStream {

    public HashMap<Long, PrintStream> threadToStream = new HashMap<Long, PrintStream>();
    public final String end;
    public final PrintStream defaultStream;

    public ThreadPrintStream(String end, PrintStream defaultStream) {
        super(defaultStream);
        this.end = end;
        this.defaultStream = defaultStream;
    }

    public void registerThread(String name) {
        try {
            PrintStream ps = new PrintStream(name + end);
            threadToStream.put(Thread.currentThread().getId(), ps);
            ps.print("");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void unregisterThread() {
        try {
            threadToStream.remove(Thread.currentThread().getId());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void flush() {
        getStream().flush();

    }

    public void close() {
        getStream().close();
    }

    public boolean checkError() {
        return getStream().checkError();
    }

    public void write(int b) {
        getStream().write(b);
    }

    public void write(byte buf[], int off, int len) {
        getStream().write(buf, off, len);
    }

    public void print(boolean b) {
        getStream().print(b);
    }

    public void print(char c) {
        getStream().print(c);
    }

    public void print(int i) {
        getStream().print(i);
    }

    public void print(long l) {
        getStream().print(l);
    }

    public void print(float f) {
        getStream().print(f);
    }

    public void print(double d) {
        getStream().print(d);
    }

    public void print(char s[]) {
        getStream().print(s);
    }

    public void print(String s) {
        getStream().print(s);
    }

    public void print(Object obj) {
        getStream().print(obj);
    }

    public void println() {
        getStream().println();
    }

    public void println(boolean x) {
        getStream().println(x);
    }

    public void println(char x) {
        getStream().println(x);
    }

    public void println(int x) {
        getStream().println(x);
    }

    public void println(long x) {
        getStream().println(x);
    }

    public void println(float x) {
        getStream().println(x);
    }

    public void println(double x) {
        getStream().println(x);
    }

    public void println(char x[]) {
        getStream().println(x);
    }

    public void println(String x) {
        getStream().println(x);
    }

    public void println(Object x) {
        getStream().println(x);
    }

    public PrintStream printf(String format, Object... args) {
        return getStream().printf(format, args);
    }

    public PrintStream printf(Locale l, String format, Object... args) {
        return getStream().format(l, format, args);
    }

    public PrintStream format(String format, Object... args) {
        return getStream().format(format, args);
    }

    public PrintStream format(Locale l, String format, Object... args) {

        return getStream().format(l, format, args);
    }

    public PrintStream append(CharSequence csq) {
        return getStream().append(csq);
    }

    public PrintStream append(CharSequence csq, int start, int end) {
        return getStream().append(csq, start, end);
    }

    public PrintStream append(char c) {
        return getStream().append(c);
    }

    public PrintStream getStream() {
        PrintStream ps = threadToStream.get(Thread.currentThread().getId());
        if (ps == null) {
            ps = defaultStream;
        }
        return ps;
    }
}
