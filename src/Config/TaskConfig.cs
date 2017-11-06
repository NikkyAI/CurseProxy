using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Cursemeta.Configs {
    public class TaskConfig {
        public static readonly Lazy<TaskConfig> instance = new Lazy<TaskConfig> (() => new TaskConfig ());

        public HourlyConfig hourly { get; private set; }
        public CompleteConfig complete { get; private set; }
        public TestConfig test { get; private set; }

        public TaskConfig () {
            hourly = new HourlyConfig ();
            complete = new CompleteConfig ();
            test = new TestConfig ();
        }
    }

    public class HourlyConfig {
        public string Schedule { get; private set; } = "*/30 * * * *";
        public bool Enabled { get; private set; } = true;
    }
    
    public class CompleteConfig {
        public string Schedule { get; private set; } = "* */12 * * *";
        public bool Enabled { get; private set; } = true;
    }
    
    public class TestConfig {
        public string Schedule { get; private set; } = "*/1 * * * *";
        public bool Enabled { get; private set; } = false;
    }
}