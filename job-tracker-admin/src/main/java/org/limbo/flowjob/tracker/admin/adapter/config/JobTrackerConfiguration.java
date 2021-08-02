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

package org.limbo.flowjob.tracker.admin.adapter.config;

import org.limbo.flowjob.tracker.core.dispatcher.JobDispatchLauncher;
import org.limbo.flowjob.tracker.core.job.context.JobInstanceRepository;
import org.limbo.flowjob.tracker.core.plan.PlanBuilderFactory;
import org.limbo.flowjob.tracker.core.plan.PlanExecutor;
import org.limbo.flowjob.tracker.core.plan.PlanInstanceRepository;
import org.limbo.flowjob.tracker.core.schedule.calculator.ScheduleCalculatorFactory;
import org.limbo.flowjob.tracker.core.schedule.scheduler.HashedWheelTimerScheduler;
import org.limbo.flowjob.tracker.core.schedule.scheduler.Scheduler;
import org.limbo.flowjob.tracker.core.storage.JobInstanceStorage;
import org.limbo.flowjob.tracker.core.storage.MemoryJobInstanceStorage;
import org.limbo.flowjob.tracker.core.tracker.JobTracker;
import org.limbo.flowjob.tracker.core.tracker.LeaderJobTracker;
import org.limbo.flowjob.tracker.core.tracker.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Brozen
 * @since 2021-06-01
 */
@ComponentScan({
        "org.limbo.flowjob.tracker.infrastructure.plan",
        "org.limbo.flowjob.tracker.infrastructure.job",
        "org.limbo.flowjob.tracker.infrastructure.worker",
})
@EnableConfigurationProperties({TrackerProperties.class})
public class JobTrackerConfiguration {

    @Autowired
    private TrackerProperties trackerProperties;

    /**
     * JobTracker
     */
    @Bean
    @ConditionalOnMissingBean(LeaderJobTracker.class)
    public LeaderJobTracker jobTracker(WorkerRepository workerRepository) {
        String[] clusretStrings = trackerProperties.getCluster().trim().split(",");
        // todo 根据 name 获取信息
        String[] nameUrl = clusretStrings[0].trim().split("=");
        String[] protocolUrl = nameUrl[1].split("://");
        String protocol = protocolUrl[0];
        String[] hostport = protocolUrl[1].split(":");
        String hostname = hostport[0];
        int port = 80;
        if (hostport.length > 1) {
            port = Integer.parseInt(hostport[1]);
        }
        return new LeaderJobTracker(hostname, port, workerRepository);
    }

    /**
     * job 存储
     */
    @Bean
    @ConditionalOnMissingBean(JobInstanceStorage.class)
    public JobInstanceStorage jobStorage() {
        return new MemoryJobInstanceStorage();
    }

    /**
     * JobTracker
     */
    @Bean
    @ConditionalOnMissingBean(JobDispatchLauncher.class)
    public JobDispatchLauncher jobDispatchLauncher(JobTracker jobTracker, JobInstanceStorage jobInstanceStorage, JobInstanceRepository jobInstanceRepository) {
        JobDispatchLauncher jobDispatchLauncher = new JobDispatchLauncher(jobTracker, jobInstanceStorage, jobInstanceRepository);
        jobDispatchLauncher.start();
        return jobDispatchLauncher;
    }

    /**
     * 调度器
     */
    @Bean
    @ConditionalOnMissingBean(Scheduler.class)
    public Scheduler scheduler() {
        return new HashedWheelTimerScheduler();
    }

    /**
     * 计划工厂
     */
    @Bean
    @ConditionalOnMissingBean(PlanBuilderFactory.class)
    public PlanBuilderFactory planFactory(PlanInstanceRepository planInstanceRepository, JobInstanceStorage jobInstanceStorage) {
        return new PlanBuilderFactory(new ScheduleCalculatorFactory(), new PlanExecutor(planInstanceRepository, jobInstanceStorage));
    }

}
