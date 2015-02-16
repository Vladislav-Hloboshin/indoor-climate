using System.Configuration;
using Quartz;
using Topshelf;
using Topshelf.Quartz;

namespace IndoorClimate.AdapterService
{
    internal class Program
    {
        private static int Interval
        {
            get { return int.Parse(ConfigurationManager.AppSettings["Interval"]); }
        }

        private static void Main(string[] args)
        {
            HostFactory.Run(x =>
            {
                x.SetServiceName("IndoorClimate.AdapterService");
                x.SetDisplayName("IndoorClimate Adapter");
                x.SetDescription("IndoorClimate Adapter");

                x.ScheduleQuartzJobAsService(q => q
                    .WithJob(() =>
                        JobBuilder.Create<Job>().Build())
                    .AddTrigger(() =>
                        TriggerBuilder.Create()
                            .WithSimpleSchedule(builder => builder
                                .WithIntervalInSeconds(Interval)
                                .RepeatForever())
                            .Build())
                    );

                x.RunAsLocalSystem();
            });
        }
    }
}
