// Small helpers for the command-line scripts (Tasks#1269).
import { pathToFileURL } from 'node:url';

/** Parse `--flag`, `--key=value` and `--out <value>`. */
export function parseArgs(argv) {
  const args = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (!a.startsWith('--')) continue;
    const [k, v] = a.slice(2).split('=');
    if (v !== undefined) args[k] = v;
    else if (k === 'out' && argv[i + 1] !== undefined) args[k] = argv[++i];
    else args[k] = true;
  }
  return args;
}

/** Run `main(argv)` when the file is the entry point; exit 2 on a thrown error. */
export function runCli(main, metaUrl) {
  if (metaUrl !== pathToFileURL(process.argv[1] ?? '').href) return;
  main(process.argv.slice(2)).then(
    (code) => process.exit(code),
    (e) => {
      console.error(`error ${e.message}`);
      process.exit(2);
    },
  );
}
