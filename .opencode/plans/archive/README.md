## Archive Storage

This directory stores versioned snapshots of `.opencode/plans/` files.

## Path Format

- `.opencode/plans/archive/v-<N>/<YYYYMMDD>T<HHMMSS>-<filename>`

## Notes

- Version selection is handled by `/analyse` via `$3 = ARCHIVE`.
- If `ARCHIVE = prompt`, the user provides `v-<N>` and all root `.opencode/plans/*.md` files are archived.
- If a `v-<N>` directory already exists, suggest `v-(max+1)` as the recommended version.
- Existing `v-<N>` directories may be reused only after explicit confirmation.
- If generated content is identical to the current target file, the write for that file is skipped.
