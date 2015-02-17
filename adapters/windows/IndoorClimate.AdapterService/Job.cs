using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Net.Http;
using Quartz;

namespace IndoorClimate.AdapterService
{
    class Job : IJob
    {
        private string CO2MiniDataLoggerFolder
        {
            get { return ConfigurationManager.AppSettings["CO2miniDataLoggerFolder"]; }
        }

        private string Code
        {
            get { return ConfigurationManager.AppSettings["Code"]; }
        }

        public void Execute(IJobExecutionContext context)
        {
            var now = DateTime.Now;
            string co2 = "0";
            string temp = "0";
            if (string.IsNullOrWhiteSpace(CO2MiniDataLoggerFolder) == false)
            {
                var filename = string.Format(@"{0}\{1}\{2:00}\{3:00}.csv", CO2MiniDataLoggerFolder, now.Year, now.Month, now.Day);
                var line = File.ReadAllLines(filename).LastOrDefault();
                if (string.IsNullOrWhiteSpace(line) == false)
                {
                    var values = line.Split(',');
                    co2 = values[1];
                    temp = values[2];
                }
            }

            using (var client = new HttpClient())
            {
                var values = new Dictionary<string, string>
                {
                    {"code", Code},
                    {"co2", co2},
                    {"temp", temp}
                };

                var content = new FormUrlEncodedContent(values);

                var postAsyncTask = client.PostAsync("http://indoor-climate.appspot.com/data", content);
                postAsyncTask.Wait();
                var response = postAsyncTask.Result;

                var readAsStringAsyncTask = response.Content.ReadAsStringAsync();
                readAsStringAsyncTask.Wait();
                var responseString = readAsStringAsyncTask.Result;
            }
        }
    }
}
