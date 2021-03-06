# AndroidPolling2
解决15分钟以下的轮询任务

## github 地址见[这里](https://github.com/hypertrack/smart-scheduler-android)

### 源码分析
源码很简单核心点在这里
```java
/**
 * @return The job object to hand to the SmartScheduler. This object is immutable.
 */
public Job build() {
    if (mJobType == Job.Type.JOB_TYPE_NONE) {

        // Schedule via Handlers if mIntervalMillis is less than JOB_TYPE_HANDLER_THRESHOLD
        if (mIntervalMillis < JOB_TYPE_HANDLER_THRESHOLD) {
            mJobType = Job.Type.JOB_TYPE_HANDLER;

            // Schedule via PeriodicTask if job requires charging or network connectivity
        } else if (mRequiresCharging || mNetworkType != Job.NetworkType.NETWORK_TYPE_ANY) {
            mJobType = Job.Type.JOB_TYPE_PERIODIC_TASK;

        } else {
            mJobType = Job.Type.JOB_TYPE_ALARM;
        }
    }

    return new Job(this);
}


// Threshold to schedule via Handlers
protected static final long JOB_TYPE_HANDLER_THRESHOLD = 60000;
```

- 如果任务间隔小于60s，就选择Handler的方式实现
- 如果要求充电，并且要求网络类型不是任意网络类型也就是联网，就选择JOB_TYPE_PERIODIC_TASK方式，也就是PeriodicTask，基于GCM，所以国内用户如果把
要求充电选项设置为false或者网络类型设置为ANY,就不会用这个选项，要不然可能会出错。这一点尤为重要
>（英语：Google Cloud Messaging，简称GCM），一项由Google提供的云端推播服务。
- 否则就用AlarmManager实现

## 关于上面的第二种方式，我测试了一下如果选择了JOB_TYPE_PERIODIC_TASK，那么国内的机型，如果没有Google play服务，就不会执行任务，所以国内用户，务必设置要求充电选项为false，或者自己改源码

### 补充一点
就选是符合了上面第二种方式也不一定会选择GCM，这里还有个坑
```java
 switch (job.getJobType()) {
            case Job.Type.JOB_TYPE_HANDLER:
                result = addHandlerJob(job);
                break;

            case Job.Type.JOB_TYPE_PERIODIC_TASK:
                if (Utils.checkIfPowerSaverModeEnabled(mContext)) {
                    // Schedule an AlarmJob if PowerSaverMode enabled
                    result = addAlarmJob(job);
                } else {
                    result = addPeriodicTaskJob(job);
                }
                break;

            case Job.Type.JOB_TYPE_ALARM:
                result = addAlarmJob(job);
                break;

            default:
                Log.e(TAG, "Error occurred while addJob: JobType is INVALID");
                break;
        }
```

#### 可以看到如果是PowerSaverMode，那么依旧会选择AlarmManager方式。
#### 个人认为这个库是不错的，我们写代码基本不会考虑这么多，轮询任务我们基本上，设置个时间就开始了，不考虑其他状况，Google已经开始准备清理这种方式了，JobScheduler就是个信号，Android的生态圈也确实需要大家一起改变。
### 完结
和之前的AndroidPolling项目合起来，Android轮询这些东西已经很清楚了，到此结束吧。

```java
 /**
     * Schedule a repeating alarm.  <b>Note: for timing operations (ticks,
     * timeouts, etc) it is easier and much more efficient to use
     * {@link android.os.Handler}.</b>  If there is already an alarm scheduled
     * for the same IntentSender, it will first be canceled.
     *
     * <p>Like {@link #set}, except you can also supply a period at which
     * the alarm will automatically repeat.  This alarm continues
     * repeating until explicitly removed with {@link #cancel}.  If the stated
     * trigger time is in the past, the alarm will be triggered immediately, with an
     * alarm count depending on how far in the past the trigger time is relative
     * to the repeat interval.
     *
     * <p>If an alarm is delayed (by system sleep, for example, for non
     * _WAKEUP alarm types), a skipped repeat will be delivered as soon as
     * possible.  After that, future alarms will be delivered according to the
     * original schedule; they do not drift over time.  For example, if you have
     * set a recurring alarm for the top of every hour but the phone was asleep
     * from 7:45 until 8:45, an alarm will be sent as soon as the phone awakens,
     * then the next alarm will be sent at 9:00.
     * 
     * <p>If your application wants to allow the delivery times to drift in 
     * order to guarantee that at least a certain time interval always elapses
     * between alarms, then the approach to take is to use one-time alarms, 
     * scheduling the next one yourself when handling each alarm delivery.
     *
     * <p class="note">
     * <b>Note:</b> as of API 19, all repeating alarms are inexact.  If your
     * application needs precise delivery times then it must use one-time
     * exact alarms, rescheduling each time as described above. Legacy applications
     * whose {@code targetSdkVersion} is earlier than API 19 will continue to have all
     * of their alarms, including repeating alarms, treated as exact.
     *
     * @param type One of {@link #ELAPSED_REALTIME}, {@link #ELAPSED_REALTIME_WAKEUP},
     *        {@link #RTC}, or {@link #RTC_WAKEUP}.
     * @param triggerAtMillis time in milliseconds that the alarm should first
     * go off, using the appropriate clock (depending on the alarm type).
     * @param intervalMillis interval in milliseconds between subsequent repeats
     * of the alarm.
     * @param operation Action to perform when the alarm goes off;
     * typically comes from {@link PendingIntent#getBroadcast
     * IntentSender.getBroadcast()}.
     *
     * @see android.os.Handler
     * @see #set
     * @see #setExact
     * @see #setWindow
     * @see #cancel
     * @see android.content.Context#sendBroadcast
     * @see android.content.Context#registerReceiver
     * @see android.content.Intent#filterEquals
     * @see #ELAPSED_REALTIME
     * @see #ELAPSED_REALTIME_WAKEUP
     * @see #RTC
     * @see #RTC_WAKEUP
     */
    public void setRepeating(int type, long triggerAtMillis,
            long intervalMillis, PendingIntent operation) {
        setImpl(type, triggerAtMillis, legacyExactLength(), intervalMillis, 0, operation,
                null, null, null, null, null);
    }
```
API大于19的手机AlarmManager都是不准确的，所以如果不想用这个库，你们还是老老实实的用Handler吧，这也是AlarmManager推荐我们的，也是最简单的。