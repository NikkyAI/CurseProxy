using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using YamlDotNet.Serialization;
using YamlDotNet.Serialization.NamingConventions;

namespace Cursemeta.Tasks {
    public class TaskConfig {
        public static readonly Lazy<TaskConfig> instance = new Lazy<TaskConfig> (() => new TaskConfig ());

        public HourlyConfig hourly { get; private set; } = new HourlyConfig ();
        public CompleteConfig complete { get; private set; } = new CompleteConfig ();
        public SyncConfig sync { get; private set; } = new SyncConfig ();
        public TestConfig test { get; private set; } = new TestConfig ();
    }

    public class HourlyConfig {
        public string Schedule { get; private set; } = "*/30 * * * *";
        public bool Enabled { get; private set; } = true;
        public bool OnStartup { get; private set; } = false;
    }

    public class CompleteConfig {
        public string Schedule { get; private set; } = "* */12 * * *";
        public bool Enabled { get; private set; } = true;
        public bool OnStartup { get; private set; } = false;
    }

    public class SyncConfig {
        public string Schedule { get; private set; } = "* */24 * * *";
        public bool Enabled { get; private set; } = false;
        public bool OnStartup { get; private set; } = false;
        public int BatchSize { get; private set; } = 500;
        public bool Addons { get; private set; } = true;
        public bool Descriptions { get; private set; } = false;
        public bool Files { get; private set; } = true;
        public bool Changelogs { get; private set; } = false;
    }

    public class TestConfig {
        public string Schedule { get; private set; } = "*/1 * * * *";
        public bool Enabled { get; private set; } = false;
    }
}