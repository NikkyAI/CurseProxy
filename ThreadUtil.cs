using System;
using System.Diagnostics;
using System.Threading.Tasks;

namespace Alpacka.Meta
{
    public static class ThreadUtil
    {
        public static async Task<int> RunProcessAsync(string fileName, string args)
        {
            using (var process = new Process
            {
                StartInfo =
                {
                    FileName = fileName, Arguments = args,
                    // UseShellExecute = false, CreateNoWindow = true
                    // RedirectStandardOutput = true, RedirectStandardError = true
                },
                EnableRaisingEvents = true
            })
            {
                return await RunProcessAsync(process).ConfigureAwait(false);
            }
        }
        
        private static Task<int> RunProcessAsync(Process process)
        {
            var tcs = new TaskCompletionSource<int>();

            process.Exited += (s, ea) => tcs.SetResult(process.ExitCode);
            // process.OutputDataReceived += (s, ea) => Console.WriteLine(ea.Data);
            // process.ErrorDataReceived += (s, ea) => Console.WriteLine("ERR: " + ea.Data);

            bool started = process.Start();
            if (!started)
            {
                //you may allow for the process to be re-used (started = false) 
                //but I'm not sure about the guarantees of the Exited event in such a case
                throw new InvalidOperationException("Could not start process: " + process);
            }

            // process.BeginOutputReadLine();
            // process.BeginErrorReadLine();

            return tcs.Task;
        }   
    }
}