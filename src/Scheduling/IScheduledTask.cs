using System.Threading;
using System.Threading.Tasks;

namespace Cursemeta.Scheduling {
    public interface IScheduledTask {
        string Schedule { get; }
        Task ExecuteAsync (CancellationToken cancellationToken);
    }
}