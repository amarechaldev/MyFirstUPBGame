#!/bin/sh
# PreToolUse hook: stops Claude Code from running git commands that modify the repo.
# Read-only git commands are allowed. Exit code 2 blocks the tool call and sends stderr to Claude.
# POSIX sh + awk only, so it runs on Windows (Git Bash), macOS and Linux without jq/node/python.

reason=$(awk '
function block(r) { print r; exit }

{ json = json $0 "\n" }

END {
    if (json ~ /"tool_name"[ \t]*:[ \t]*"EnterWorktree"/)
        block("Creating git worktrees is not allowed.")

    # Decode the JSON string value of "command" (tool_input.command).
    if (!match(json, /"command"[ \t]*:[ \t]*"/)) exit
    rest = substr(json, RSTART + RLENGTH)
    cmd = ""
    for (i = 1; i <= length(rest); i++) {
        c = substr(rest, i, 1)
        if (c == "\"") break
        if (c == "\\") {
            i++
            c = substr(rest, i, 1)
            if (c == "n" || c == "r") c = "\n"
            else if (c == "t") c = " "
        }
        cmd = cmd c
    }

    # Split into individual commands on shell separators, then inspect each one.
    gsub(/[;&|(){}`]/, "\n", cmd)
    nseg = split(cmd, segs, "\n")
    for (s = 1; s <= nseg; s++) {
        nraw = split(segs[s], raw, /[ \t]+/)
        n = 0
        for (k = 1; k <= nraw; k++) {
            if (raw[k] == "") continue
            w = raw[k]
            gsub(/^["\047]+|["\047]+$/, "", w)
            words[++n] = w
        }
        for (k = 1; k <= n; k++) {
            name = tolower(words[k])
            sub(/.*[\/\\]/, "", name)
            sub(/\.exe$/, "", name)
            if (name == "gh") block("The GitHub CLI (gh) is not allowed.")
            if (name != "git") continue

            # Skip git global options to find the subcommand.
            j = k + 1
            while (j <= n && words[j] ~ /^-/) {
                if (words[j] ~ /^(-C|-c|--git-dir|--work-tree|--namespace|--exec-path)$/) j++
                j++
            }
            if (j > n) break

            subcmd = tolower(words[j])
            if (index(" status log diff show blame shortlog ls-files rev-parse describe grep reflog help version ", " " subcmd " ") == 0)
                block("\047git " subcmd "\047 is not allowed.")
            if (subcmd == "reflog" && j < n && tolower(words[j + 1]) ~ /^(expire|delete)$/)
                block("\047git reflog " words[j + 1] "\047 is not allowed.")
            break
        }
    }
}
')

if [ -n "$reason" ]; then
    echo "BLOCKED: $reason Git write operations are disabled in this project - the user handles commits, checkouts, merges, etc. themselves. Do not try to work around this; tell the user which command they should run instead." >&2
    exit 2
fi
exit 0
