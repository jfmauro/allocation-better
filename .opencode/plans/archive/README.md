## Archive Policy

- Single archive policy (MUST): `.opencode/plans/archive/v-<N>/<YYYYMMDD>T<HHMMSS>-<filename>`.
- Legacy root archive path is forbidden: `.opencode/plans/archive/<YYYYMMDD>T<HHMMSS>-<filename>`.
- Version is requested once per command execution with format `V-<number>` where number is `0..100000`.
- Reuse of an existing `v-<N>` directory requires explicit user confirmation.

## No-op Rule

- Generate candidate content first.
- If candidate content is identical to the current target file, skip archive and skip write.
- Required log line: `No content change detected — no archive/write performed.`

## Legacy Migration

- `v-0` is reserved for migrated legacy archives if any root-level legacy files are discovered.
- `v-1+` are normal versioned archives produced by current commands and agents.
