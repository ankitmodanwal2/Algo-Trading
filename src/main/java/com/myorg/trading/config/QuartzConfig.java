package com.myorg.trading.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {

        Properties quartzProps = new Properties();
        quartzProps.setProperty("org.quartz.scheduler.instanceName", "TradingQuartzScheduler");
        quartzProps.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        // --- FIX: Use Spring's LocalDataSourceJobStore instead of JobStoreTX ---
        quartzProps.setProperty("org.quartz.jobStore.class", "org.springframework.scheduling.quartz.LocalDataSourceJobStore");

        quartzProps.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        quartzProps.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        quartzProps.setProperty("org.quartz.jobStore.isClustered", "true");
        quartzProps.setProperty("org.quartz.jobStore.clusterCheckinInterval", "5000");

        // Thread Pool
        quartzProps.setProperty("org.quartz.threadPool.threadCount", "10");
        quartzProps.setProperty("org.quartz.threadPool.threadPriority", "5");

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setQuartzProperties(quartzProps);
        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(false);

        return factory;
    }
}