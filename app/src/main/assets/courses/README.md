# Course content format

The Java code contains only the reader and renderer. This directory currently retains the
independently authored bytecode course data while the learning-center entry point is disabled.

- `index.json` defines courses, chapters, lesson IDs, titles, and descriptions.
- `lessons/<course-id>/<lesson-id>.json` contains the two localized versions of one lesson.
- Every lesson must have both `uk` and `en` arrays, even when one translation is still brief.

Each block has a `type` and `text`. Supported types are `heading`, `paragraph`, `code`,
`list`, `note`, `warning`, and `table`. A `table` block also has `tableHeader`; table columns
are separated by a tab and rows by a newline. A runnable `code` block has `runMode`
(`java_statements` or `java_source`) and `executionText`. Leave those fields out for an
illustrative, non-runnable code sample.

Keep lesson IDs stable: navigation and future progress storage use them as identifiers.
