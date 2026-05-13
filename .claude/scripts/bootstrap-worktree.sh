#!/usr/bin/env bash
# Copies untracked secret files from the main checkout into a linked git
# worktree on session start, so subagent-spawned worktrees can build without
# manual setup. Safe no-op outside worktrees. Cross-platform: macOS, Linux,
# and Windows via Git Bash.

set -u

git_dir="$(git rev-parse --git-dir 2>/dev/null || true)"
common_dir="$(git rev-parse --git-common-dir 2>/dev/null || true)"
[ -z "$git_dir" ] || [ -z "$common_dir" ] && exit 0

# Resolve to absolute paths so equality comparison is meaningful.
git_dir_abs="$(cd "$git_dir" 2>/dev/null && pwd -P)" || exit 0
common_dir_abs="$(cd "$common_dir" 2>/dev/null && pwd -P)" || exit 0
[ "$git_dir_abs" = "$common_dir_abs" ] && exit 0  # not a linked worktree

# Submodule guard: --git-dir != --git-common-dir is also true in submodules.
super="$(git rev-parse --show-superproject-working-tree 2>/dev/null || true)"
[ -n "$super" ] && exit 0

main_root="$(cd "$common_dir_abs/.." && pwd -P)"
worktree_root="$(git rev-parse --show-toplevel 2>/dev/null)"
worktree_root="$(cd "$worktree_root" && pwd -P)"
[ "$main_root" = "$worktree_root" ] && exit 0

files=(
    ".env"
    "composeApp/google-services.json"
    "iosApp/iosApp/GoogleService-Info.plist"
    "iosApp/iosApp/Secrets.plist"
)

copied=()
missing=()

for rel in "${files[@]}"; do
    src="$main_root/$rel"
    dst="$worktree_root/$rel"
    if [ ! -e "$src" ]; then missing+=("$rel"); continue; fi
    [ -e "$dst" ] && continue
    mkdir -p "$(dirname "$dst")"
    cp "$src" "$dst"
    copied+=("$rel")
done

# SessionStart hook stdout is appended to the session context.
if [ "${#copied[@]}" -gt 0 ]; then
    printf '[bootstrap-worktree] Copied from main: %s\n' "$(printf '%s, ' "${copied[@]}" | sed 's/, $//')"
fi
if [ "${#missing[@]}" -gt 0 ]; then
    printf '[bootstrap-worktree] Source missing in main (skipped): %s\n' "$(printf '%s, ' "${missing[@]}" | sed 's/, $//')"
fi
