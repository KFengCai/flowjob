/*
 * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import org.limbo.flowjob.broker.core.schedule.ScheduleOption;
import org.limbo.flowjob.common.constants.ScheduleType;

import java.time.Duration;

/**
 * @author Brozen
 * @since 2022-10-11
 */
public abstract class FixRateMetaTask extends LoopMetaTask {

    protected FixRateMetaTask(Duration interval, MetaTaskScheduler metaTaskScheduler) {
        this(Duration.ZERO, interval, metaTaskScheduler);
    }


    protected FixRateMetaTask(Duration delay, Duration interval, MetaTaskScheduler metaTaskScheduler) {
        super(null, null, new ScheduleOption(ScheduleType.FIXED_RATE, null, delay, interval, null, null), metaTaskScheduler);
    }

}
