#!/usr/bin/env python3
"""Assign an existing Crowdin translation memory to a project, then read it back.

Task: https://github.com/ItsCalebJones/SpaceLaunchNow-Tasks/issues/1265
Spec 017, section 7.4: TM 406219 (old project, 606 segments) goes into project 948217.

The token comes from CROWDIN_PERSONAL_TOKEN in the environment. It is never printed.

Usage:
  python3 scripts/i18n/crowdin_assign_tm.py [--project 948217] [--tm 406219]
                                            [--expect-segments 606] [--assign]
Without --assign the script only reads. With --assign it adds the TM to the project's
assigned TMs (the default TM and every other project setting stay as they are), then reads back.
Exit code: 0 the project uses the TM and the segment count is near the expected count, 1 otherwise.
"""
import argparse
import json
import os
import sys
import urllib.error
import urllib.request

API = "https://api.crowdin.com/api/v2"


def call(method, path, token, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(API + path, data=data, method=method)
    req.add_header("Authorization", f"Bearer {token}")
    req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            return json.load(resp)["data"]
    except urllib.error.HTTPError as err:
        # The body holds the API error message, never the token.
        print(f"{method} {path} -> HTTP {err.code}: {err.read().decode()[:500]}", file=sys.stderr)
        raise SystemExit(1)


def assigned_tms(project):
    """Return {tm id (str): {"priority": n}} from a project record."""
    return dict(project.get("assignedTms") or {})


def merged_with(current, tm_id):
    """Keep every assigned TM and add tm_id (priority after the existing ones). No-op if present."""
    out = dict(current)
    key = str(tm_id)
    if key not in out:
        out[key] = {"priority": max((v.get("priority", 1) for v in out.values()), default=0) + 1}
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--project", type=int, default=948217)
    ap.add_argument("--tm", type=int, default=406219)
    ap.add_argument("--expect-segments", type=int, default=606)
    ap.add_argument("--assign", action="store_true", help="assign the TM; without it, read only")
    args = ap.parse_args()

    token = os.environ.get("CROWDIN_PERSONAL_TOKEN")
    if not token:
        print("CROWDIN_PERSONAL_TOKEN is not set.", file=sys.stderr)
        return 1

    tm = call("GET", f"/tms/{args.tm}", token)
    print(f"TM {args.tm}: name={tm.get('name')!r} segments={tm.get('segmentsCount')}")

    project = call("GET", f"/projects/{args.project}", token)
    current = assigned_tms(project)
    print(f"Project {args.project}: defaultTmId={project.get('defaultTmId')} assignedTms={sorted(current)}")

    if args.assign and str(args.tm) not in current:
        new = merged_with(current, args.tm)
        call("PATCH", f"/projects/{args.project}", token,
             [{"op": "replace", "path": "/assignedTms", "value": new}])
        print(f"Assigned TM {args.tm} to project {args.project}.")

    # Read back: the check that proves the load.
    project = call("GET", f"/projects/{args.project}", token)
    tm = call("GET", f"/tms/{args.tm}", token)
    used = str(args.tm) in assigned_tms(project) or project.get("defaultTmId") == args.tm
    count = tm.get("segmentsCount")
    print(f"Read back: project uses TM {args.tm}: {used}; segments: {count} (expected about {args.expect_segments})")
    near = isinstance(count, int) and abs(count - args.expect_segments) <= max(10, args.expect_segments // 20)
    return 0 if used and near else 1


if __name__ == "__main__":
    sys.exit(main())
