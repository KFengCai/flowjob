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

package org.limbo.flowjob.broker.core.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.limbo.flowjob.broker.core.cluster.WorkerManager;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelector;
import org.limbo.flowjob.broker.core.dispatcher.WorkerSelectorFactory;
import org.limbo.flowjob.broker.core.domain.task.Task;
import org.limbo.flowjob.broker.core.exceptions.JobDispatchException;
import org.limbo.flowjob.broker.core.statistics.WorkerStatisticsRepository;
import org.limbo.flowjob.broker.core.worker.Worker;
import org.limbo.flowjob.common.constants.TaskStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Devil
 * @since 2022/9/4
 */
@Slf4j
public class TaskDispatcher {

    private final WorkerManager workerManager;

    private final WorkerSelectorFactory workerSelectorFactory;

    private final WorkerStatisticsRepository statisticsRepository;

    public TaskDispatcher(WorkerManager workerManager, WorkerSelectorFactory workerSelectorFactory, WorkerStatisticsRepository statisticsRepository) {
        this.workerManager = workerManager;
        this.workerSelectorFactory = workerSelectorFactory;
        this.statisticsRepository = statisticsRepository;
    }

    /**
     * 将任务下发给worker。
     * task status -> EXECUTING or FAILED
     */
    public boolean dispatch(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("start dispatch task={}", task);
        }

        if (task.getStatus() != TaskStatus.DISPATCHING) {
            throw new JobDispatchException(task.getJobId(), task.getTaskId(), "Cannot startup context due to current status: " + task.getStatus());
        }

        if (StringUtils.isBlank(task.getWorkerId())) {
            return dispatchWithWorkerId(task);
        } else {
            return dispatchNoWorker(task);
        }
    }

    private boolean dispatchWithWorkerId(Task task) {
        Worker worker = workerManager.get(task.getWorkerId());
        if (worker == null) {
            return false;
        }

        try {
            // 发送任务到worker，根据worker返回结果，更新状态
            boolean dispatched = worker.sendTask(task);
            if (dispatched) {
                onDispatchSucceed(task, worker);
                return true;
            } else {
                onDispatchToWorkerFailed(task, worker);
            }
        } catch (Exception e) {
            log.error("Task dispatch with error task={}", task, e);
        }

        // 下发失败
        onDispatchFailed(task);
        return false;
    }

    private boolean dispatchNoWorker(Task task) {
        List<Worker> availableWorkers = workerManager.availableWorkers();
        if (CollectionUtils.isEmpty(availableWorkers)) {
            return false;
        }
        WorkerSelector workerSelector = workerSelectorFactory.newSelector(task.getDispatchOption().getLoadBalanceType());
        for (int i = 0; i < 3; i++) {
            try {
                SimpleWorkerSelectArguments args = new SimpleWorkerSelectArguments(task);
                Worker worker = workerSelector.select(args, availableWorkers);
                if (worker == null) {
                    return false;
                }

                // 发送任务到worker，根据worker返回结果，更新状态
                boolean dispatched = worker.sendTask(task);
                if (dispatched) {
                    onDispatchSucceed(task, worker);
                    return true;
                } else {
                    onDispatchToWorkerFailed(task, worker);
                }

                availableWorkers = availableWorkers.stream().filter(w -> !Objects.equals(w.getId(), worker.getId())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("Task dispatch with error task={}", task, e);
            }
        }

        // 下发失败
        onDispatchFailed(task);
        return false;
    }


    /**
     * 下发任务到 worker 成功时的流程
     */
    private void onDispatchSucceed(Task task, Worker worker) {
        // 更新状态
        task.setStatus(TaskStatus.EXECUTING);
        task.setWorkerId(worker.getId());

        // 记录统计数据
        statisticsRepository.recordTaskDispatched(task, worker);

        if (log.isDebugEnabled()) {
            log.debug("Task dispatch success task={}", task.getTaskId());
        }
    }


    /**
     * 下发任务到 worker 失败时的流程
     */
    private void onDispatchToWorkerFailed(Task task, Worker worker) {
        if (log.isDebugEnabled()) {
            log.debug("Task dispatch failed: task={} worker={}", task.getTaskId(), worker.getId());
        }
    }


    /**
     * 下发任务流程整体失败时的处理逻辑，重试后仍失败会触发此逻辑。
     */
    private void onDispatchFailed(Task task) {
        task.setStatus(TaskStatus.FAILED);

        if (log.isDebugEnabled()) {
            log.debug("Task dispatch fail task={}", task.getTaskId());
        }
    }

}
