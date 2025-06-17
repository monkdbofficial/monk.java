// Copyright 2025, Movibase Platform Private Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.monkdb.exceptions;

import java.io.Serial;

public class MonkErrors extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String errorTrace;

    /**
     * Constructs a new exception with the specified detail message.
     * The error trace is set to null.
     *
     * @param message the detail message
     */
    public MonkErrors(String message) {
        this(message, (String) null);
    }

    /**
     * Constructs a new exception with the specified detail message and error trace.
     *
     * @param message the detail message
     * @param errorTrace additional error trace information
     */
    public MonkErrors(String message, String errorTrace) {
        super(message);
        this.errorTrace = errorTrace;
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * The error trace is set to null.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by getCause())
     */
    public MonkErrors(String message, Throwable cause) {
        super(message, cause);
        this.errorTrace = null;
    }

    @Override
    public String toString() {
        return errorTrace == null ? super.toString() : super.toString() + "\n" + errorTrace;
    }
}

