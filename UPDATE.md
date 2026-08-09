# UPDATE.md

This document contains the instructions to follow when updating `README.md` for this project.

## Goal

Keep `README.md` clear, structured, and consistent with the current project style. The README should remain a concise technical overview of the Java/Maven project and its dataset utilities.

## When updating `README.md`

- Preserve the existing professional, technical tone.
- Keep the section order stable unless a new section is clearly needed.
- Prefer short, direct sentences and bullet points.
- Use backticks for file names, package names, class names, and other code identifiers.
- Use markdown tables for project metadata and dependency lists.
- Use fenced code blocks for directory trees or multi-line examples.
- Avoid marketing language or overly long explanations.
- Keep descriptions accurate to the codebase and dataset folders.

## Recommended README structure

If you need to expand the README, keep the existing structure aligned with these sections:

1. `## Overview`
2. `## What this project does`
3. `## Project metadata`
4. `## Dependencies`
5. `## Project structure`
6. `### Package responsibilities`
7. `## Notes`

## Content guidance

- Update the overview only if the project purpose changes.
- Keep feature bullets focused on the actual utilities in `src/main/java`.
- Update dependency tables if `pom.xml` changes.
- Update the project structure tree if files or folders are added or removed.
- If new utility classes are added, describe them in package responsibilities.
- If new datasets are added, mention them in the dataset-related section or a dedicated subsection if needed.

## Style rules

- Match the current Markdown formatting.
- Keep headings descriptive and consistent.
- Prefer lowercase package paths wrapped in backticks.
- Use the same terminology already used in the README, such as:
  - `Tablesaw`
  - `CSV`
  - `entry point`
  - `utility classes`
  - `train/test/validation`

## Before saving changes

- Verify that the text matches the current codebase.
- Remove outdated references.
- Keep the README concise and readable.
- Ensure any new section adds clear value.

