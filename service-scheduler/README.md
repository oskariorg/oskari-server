Oskari Scheduler Service
========================

Use this service to schedule executions of your JVM-level batch tasks.

### Integrating the scheduler into your application

            final SchedulerService ss = new SchedulerService();
            ss.initializeScheduler();

            ... normal application lifecycle ...

            ss.shutdownScheduler();

### Creating new scheduled tasks

Oskari.properties has predefined settings for Quartz in general.
You can use them as is or override them in oskari-ext.properties:

    # Quartz scheduler configuration

    org.quartz.scheduler.instanceName=OskariScheduler
    org.quartz.threadPool.threadCount=1
    org.quartz.scheduler.skipUpdateCheck=true
    org.quartz.jobStore.class=org.quartz.simpl.RAMJobStore

#### Annotation

When setting up a new scheduled task, create a class extending fi.nls.oskari.worker.ScheduledJob (service-base) and annotate with
 @Oskari("MyJobID"). Edit your oskari-ext.properties file to add schedule for your new job code:

    # Oskari Scheduler configuration

    oskari.scheduler.job.MyJobID.cronLine=0 * * * * ?

#### Static class

When setting up a new scheduled task, create a new static method with your job,
edit your oskari-ext.properties file, add your new job code
to the ``oskari.scheduler.jobs`` comma-separated list of job codes, and finally
add the job parameters ``cronLine``, ``className`` and ``methodName``
as demonstrated below.

    # Oskari Scheduler configuration

    oskari.scheduler.jobs=page_google
    oskari.scheduler.job.page_google.cronLine=0 * * * * ?
    oskari.scheduler.job.page_google.className=com.test.GoogleSpider
    oskari.scheduler.job.page_google.methodName=scheduledMethod
