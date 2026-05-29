# Working Guidelines

This is the first document to check before making changes to Questack.

The goal is to keep day-to-day development fast while making the project history easy to review later.

## Before Starting Work

1. Check the current branch and working tree.
2. Use a branch prefix that matches the work:
   - `feat/`
   - `fix/`
   - `docs/`
   - `test/`
   - `refactor/`
   - `chore/`
3. Read the relevant code and nearby tests before editing.
4. If the work changes a previously recorded rule, update this file or `docs/technical-decisions.md`.

## Document Map

- `README.md`: project overview, quick start, public API summary
- `docs/working-guidelines.md`: day-to-day development rules
- `docs/technical-decisions.md`: append-only technical and workflow decisions
- `docs/troubleshooting.md`: append-only issue investigation log
- `docs/api-docs/index.md`: generated Spring REST Docs snippet index
- `docs/samples`: committed sample output artifacts
- `docs/daily-briefings`: runtime daily briefing output

## Controller Changes

When adding or changing a controller method:

1. Add or update a MockMvc controller test.
2. Document request parameters and response fields with Spring REST Docs.
3. Generate snippets under `docs/api-docs`.
4. Keep generated API docs out of `src`.
5. Run `./gradlew test`.

## Technical Decisions

Add a `TD-###` entry when a change introduces or changes:

- architecture boundaries
- persistence strategy
- external API integration strategy
- API documentation strategy
- testing harness strategy
- commit, PR, or workflow rules

Avoid adding a TD entry for tiny implementation details that are obvious from code.

## Troubleshooting

Add a `TR-###` entry when:

- a command fails in a non-obvious way
- a workflow assumption turns out to be wrong
- sandbox, environment, or API behavior affects development
- the same mistake could plausibly happen again

Use `관련 항목: 없음` when there is no meaningful technical decision to cross-reference.

## Commit Guidelines

PRs should stay feature-oriented, but commits should be split by reviewable intent.

Recommended commit boundaries:

- `feat`: domain/configuration, service logic, or API behavior changes
- `test`: controller, service, fixture, harness, or REST Docs test changes
- `docs`: README, technical decisions, troubleshooting, API snippets, sample outputs
- `refactor`: package moves, path changes, naming changes, or structure-only changes
- `fix`: bug fixes or behavior corrections
- `chore`: build, dependency, or repository maintenance

Rules:

- Keep each commit explainable with one sentence.
- Keep each commit buildable whenever practical.
- Put controller behavior changes and their MockMvc REST Docs tests in the same commit unless the test harness itself is being introduced separately.
- Put generated sample artifacts in a separate `docs:` commit when they are not required for runtime behavior.
- Keep path moves and package restructuring in a separate `refactor:` commit when possible.
- Technical decisions should be committed with the first code or documentation change that reflects the decision.
- Troubleshooting entries should be committed with the fix or workflow adjustment that resolves the issue.

## Pre-Commit Checklist

Run:

```bash
./gradlew test
```

Check:

- No generated docs under `src/docs`
- REST Docs snippets are under `docs/api-docs`
- Sample outputs are under `docs/samples`
- Runtime outputs are not accidentally committed unless intentionally marked as samples
- Commit scope is small enough to review
