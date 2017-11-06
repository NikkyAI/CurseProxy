using System;

namespace Cursemeta.Cron {
    [Serializable]
    public enum CrontabFieldKind {
        Minute,
        Hour,
        Day,
        Month,
        DayOfWeek
    }
}