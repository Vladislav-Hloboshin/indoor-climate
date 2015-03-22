using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Quartz;
using SKYPE4COMLib;
using Topshelf;
using Topshelf.Quartz;

namespace SkypeNotificationService
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            HostFactory.Run(x =>
            {
                x.SetServiceName("IndoorClimate.SkypeNotificationService");
                x.SetDisplayName("IndoorClimate Skype Notification");
                x.SetDescription("IndoorClimate Skype Notification");

                x.ScheduleQuartzJobAsService(q => q
                    .WithJob(() =>
                        JobBuilder.Create<Job>().Build())
                    .AddTrigger(() =>
                        TriggerBuilder.Create()
                            .WithSimpleSchedule(builder => builder
                                .WithIntervalInSeconds(AppSettings.Default.PollIntervalInSeconds)
                                .RepeatForever())
                            .Build())
                    );

                x.RunAsLocalSystem();
            });
        }

        /*static void Main(string[] args)
        {
            var skype = new Skype();
            skype.Attach();
            skype.BookmarkedChats
                .Cast<Chat>()
                .Single(c => c.Topic == "Офис")
                .SendMessage(".");
            Console.ReadKey();
        }*/
    }
}
