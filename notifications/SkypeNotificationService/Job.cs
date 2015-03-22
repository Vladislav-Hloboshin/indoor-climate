using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Web;
using Newtonsoft.Json;
using Quartz;
using SKYPE4COMLib;

namespace SkypeNotificationService
{
    class Job : IJob
    {
        static Job()
        {
            var uriBuilder = new UriBuilder("http://indoor-climate.appspot.com/data");
            var query = HttpUtility.ParseQueryString(string.Empty);
            query["method"] = "actual";
            query["code"] = AppSettings.Default.Code;
            uriBuilder.Query = query.ToString();
            ServerUri = uriBuilder.Uri;
        }

        private static readonly Uri ServerUri;
        private static readonly Random Rnd = new Random();

        private static DateTime _notificationTime = DateTime.MinValue;
        private static int _sendedMessagesCount = 0;

        public void Execute(IJobExecutionContext context)
        {
            ActualData actualData;
            using (var client = new HttpClient())
            {
                var getAsyncTask = client.GetAsync(ServerUri);
                getAsyncTask.Wait();
                var response = getAsyncTask.Result;

                var readAsStringAsyncTask = response.Content.ReadAsStringAsync();
                readAsStringAsyncTask.Wait();
                actualData = JsonConvert.DeserializeObject<ActualData>(readAsStringAsyncTask.Result);
            }
            DateTime now = DateTime.Now;
            if (actualData.Co2 >= AppSettings.Default.CO2WarningLevel)
            {
                if ((now - _notificationTime).TotalMinutes >= AppSettings.Default.MessagesIntervalInMinutes)
                {
                    SendNotificationMessage(_sendedMessagesCount++ == AppSettings.Default.InitialMessagesCount);
                    _notificationTime = now;
                }
            }
            else
            {
                _sendedMessagesCount = 0;
            }

        }

        private void SendNotificationMessage(bool notificateUsers = false)
        {
            var messageIndex = Rnd.Next(AppSettings.Default.NotificationMessages.Count);
            string message = AppSettings.Default.NotificationMessages[messageIndex];
            var skype = new Skype();
            skype.Attach();
            foreach (var chatTopic in AppSettings.Default.SkypeChatTopics)
            {
                skype.BookmarkedChats
                    .Cast<Chat>()
                    .Where(c => c.Topic == chatTopic)
                    .ToList()
                    .ForEach(c=>c.SendMessage(message));
            }
            if (notificateUsers || AppSettings.Default.SkypeChatTopics.Count == 0)
            {
                foreach (var user in AppSettings.Default.SkypeUsers)
                {
                    skype.SendMessage(user, message);
                }
            }
        }
    }
}
