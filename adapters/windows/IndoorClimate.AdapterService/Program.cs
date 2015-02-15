using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Quartz;
using Topshelf;
using Topshelf.Quartz;

namespace IndoorClimate.AdapterService
{
    internal class Program
    {
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
                                .WithIntervalInSeconds(5)
                                .RepeatForever())
                            .Build())
                    );

                x.RunAsLocalSystem();
            });
        }
    }
}
