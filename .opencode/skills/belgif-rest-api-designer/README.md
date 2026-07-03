# belgif-rest-api-designer

A professional Agent Skill for designing and reviewing REST APIs according to the Belgif REST Guidelines.

## Install
If published in a GitHub repository compatible with skills.sh:

```bash
npx skills add <owner>/belgif-rest-api-designer
```

Or copy this folder into your agent's skills directory.

## Contents
- `SKILL.md` — activation metadata and operating instructions.
- `references/belgif-rest-api-guide.md` — operational synthesis of Belgif REST guidance.
- `references/openapi-review-checklist.md` — audit checklist.
- `assets/openapi-template.yaml` — starter OpenAPI contract.
- `scripts/create_openapi_skeleton.py` — generator for starter contracts.

## Recommended use prompts
- "Design a Belgif-compliant REST API for managing tax debt correction files."
- "Review this OpenAPI YAML against Belgif REST guidelines and classify findings."
- "Generate a resource model, URI map and OpenAPI 3 contract for ..."

## Validation
Use the official Belgif REST guide and validator as final authority before publication.
