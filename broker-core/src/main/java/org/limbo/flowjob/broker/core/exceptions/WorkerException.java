/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.exceptions;

import lombok.Getter;

/**
 * Worker调用时发生的异常
 *
 * @author Brozen
 * @since 2021-05-25
 */
public class WorkerException extends RuntimeException {

    private static final long serialVersionUID = 8644570391864065637L;

    /**
     * 异常的worker id
     */
    @Getter
    private final String workerId;

    public WorkerException(String workerId, String message) {
        super(message);
        this.workerId = workerId;
    }

    public WorkerException(String workerId, String message, Throwable cause) {
        super(message, cause);
        this.workerId = workerId;
    }

}
