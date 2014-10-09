Oskari Scheduler Service
========================

Use this service to schedule executions of your JVM-level batch tasks.

### Integrating the scheduler into your application

            final SchedulerService ss = new SchedulerService();
            ss.initializeScheduler();

            ... normal application lifecycle ...

            ss.shutdownScheduler();

### Creating new scheduled tasks

When setting up a new scheduled task, create a new static method with your job,
edit your oskari.properties (or oskari-ext.properties) file, add your new job code
to the ``oskari.scheduler.jobs`` comma-separated list of job codes, and finally
add the job parameters ``cronLine``, ``className`` and ``methodName``
as demonstrated below.

    # Quartz scheduler configuration

    org.quartz.scheduler.instanceName=OskariScheduler
    org.quartz.threadPool.threadCount=1
    org.quartz.scheduler.skipUpdateCheck=true
    org.quartz.jobStore.class=org.quartz.simpl.RAMJobStore

    # Oskari Scheduler configuration

    oskari.scheduler.jobs=page_google
    oskari.scheduler.job.page_google.cronLine=0 * * * * ?
    oskari.scheduler.job.page_google.className=com.test.GoogleSpider
    oskari.scheduler.job.page_google.methodName=scheduledMethod
