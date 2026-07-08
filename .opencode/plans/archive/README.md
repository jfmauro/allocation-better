## Archive Storage

This directory stores versioned snapshots of `.opencode/plans/` files.

## Path Format

- `.opencode/plans/archive/v-<N>/<YYYYMMDD>T<HHMMSS>-<filename>`

## Notes

- Version selection is handled by `/analyse` when the user explicitly requests archiving.
- Existing `v-<N>` directories may be reused only after explicit confirmation.
- If generated content is identical to the current target file, the write for that file is skipped.
