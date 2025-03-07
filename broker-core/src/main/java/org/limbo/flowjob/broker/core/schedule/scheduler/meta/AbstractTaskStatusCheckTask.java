/*
 *
 *  * Copyright 2020-2024 Limbo Team (https://github.com/limbo-world).
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.limbo.flowjob.broker.core.schedule.scheduler.meta;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.limbo.flowjob.broker.core.cluster.BrokerConfig;
import org.limbo.flowjob.broker.core.cluster.NodeManger;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.schedule.strategy.IScheduleStrategy;
import org.limbo.flowjob.broker.core.schedule.strategy.ScheduleStrategyFactory;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.broker.core.worker.WorkerRepository;

import java.time.Duration;
import java.util.List;

/**
 * task 如果长时间执行中没有进行反馈 需要对其进行状态检查
 * 可能导致task没有完成的原因
 * 1. worker服务真实下线
 * 2. worker服务假死
 * 3. worker完成task调用broker的接口失败
 */
public abstract class AbstractTaskStatusCheckTask extends FixDelayMetaTask {

    @Getter
    protected final BrokerConfig config;

    @Getter
    protected final NodeManger nodeManger;

    private final WorkerRepository workerRepository;

    private final ScheduleStrategyFactory scheduleStrategyFactory;

    protected AbstractTaskStatusCheckTask(Duration interval,
                                          BrokerConfig config,
                                          NodeManger nodeManger,
                                          MetaTaskScheduler metaTaskScheduler,
                                          WorkerRepository workerRepository,
                                          ScheduleStrategyFactory scheduleStrategyFactory) {
        super(interval, metaTaskScheduler);
        this.config = config;
        this.nodeManger = nodeManger;
        this.workerRepository = workerRepository;
        this.scheduleStrategyFactory = scheduleStrategyFactory;
    }


    @Override
    protected void executeTask() {
        // 判断自己是否存在 --- 可能由于心跳异常导致不存活
        if (!nodeManger.alive(config.getName())) {
            return;
        }

        List<TaskScheduleTask> dispatchingTasks = loadDispatchingTasks();
        if (CollectionUtils.isNotEmpty(dispatchingTasks)) {
            for (TaskScheduleTask scheduleTask : dispatchingTasks) {
                scheduleTask.execute();
            }
        }

        List<TaskScheduleTask> executingTasks = loadExecutingTasks();
        if (CollectionUtils.isNotEmpty(executingTasks)) {
            // 获取长时间为执行中的task 判断worker是否已经宕机
            for (TaskScheduleTask scheduleTask : executingTasks) {
                Task task = scheduleTask.getTask();
                Worker worker = workerRepository.get(task.getWorkerId());
                if (worker == null || !worker.isAlive()) {
                    IScheduleStrategy scheduleStrategy = scheduleStrategyFactory.build(task.getPlanType());
                    scheduleStrategy.handleTaskFail(task, String.format("worker %s is offline", task.getWorkerId()), "");
                }
            }
        }
    }

    /**
     * 加载下发中的 task。
     */
    protected abstract List<TaskScheduleTask> loadDispatchingTasks();

    /**
     * 加载执行中的 task。
     */
    protected abstract List<TaskScheduleTask> loadExecutingTasks();

    @Override
    public MetaTaskType getType() {
        return MetaTaskType.TASK_STATUS_CHECK;
    }

    @Override
    public String getMetaId() {
        return "TaskStatusCheckTask";
    }
}
