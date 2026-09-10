# Resources, Methods & Stages

The structure of an API in API Gateway, and the deployment model that determines when a change becomes live.

---

## Resources and Methods

A **resource** is a path segment; resources nest to form paths. A **method** is an HTTP verb on a resource, and it is where the integration is configured.

```
/                          
└── /orders                 GET, POST
    └── /{orderId}          GET, PUT, DELETE
        └── /items          GET
```

*Path parameters are braced. `/orders/{orderId}/items` matches `/orders/O-5501/items`, with `orderId` available to the integration.*

**Greedy path variables** capture everything remaining:

```
/legacy/{proxy+}    →  matches /legacy/anything/at/any/depth
```

*A proxy resource forwards all matching paths to one integration — the usual approach for putting a gateway in front of an existing application without enumerating its routes.*

The combination of `ANY` method and `{proxy+}` produces a catch-all, where routing is handled entirely by the backend. It is convenient and gives up per-route authorization, validation, and metrics — which is often the reason for using API Gateway in the first place.

---

## Stages

A **stage** is a named deployment of an API — `dev`, `test`, `prod`. Each has its own URL:

```
https://abc123.execute-api.us-east-1.amazonaws.com/prod/orders
https://abc123.execute-api.us-east-1.amazonaws.com/dev/orders
```

Stages carry their own configuration: throttling limits, logging settings, caching, and **stage variables**.

**Stage variables** are key-value pairs referenced in the integration configuration, which is how one API definition targets different backends per stage:

```
Integration URI: arn:aws:lambda:us-east-1:...:function:order-processor:${stageVariables.lambdaAlias}
```

*The `prod` stage sets `lambdaAlias=prod` and the `dev` stage sets `dev`, so one API definition points each stage at a different Lambda alias.*

---

## The Deployment Model

This is the part that catches people, and it differs between the two API types.

**REST APIs require an explicit deployment.** Changing a method, adding a resource, or editing an integration modifies the API definition and **changes nothing that is live**. The change takes effect only when a deployment is created and associated with a stage:

```bash
aws apigateway create-deployment \
  --rest-api-id abc123 \
  --stage-name prod \
  --description "Add DELETE /orders/{orderId}"
```

*Without this, the console shows the new method and the live API does not serve it. "I changed it and nothing happened" is almost always a missing deployment.*

**HTTP APIs deploy automatically by default**, with auto-deploy enabled on the stage. Changes are live immediately, which is simpler and removes the deliberate promotion step.

For REST APIs, the deployment model enables **canary releases**: a stage can send a percentage of traffic to a new deployment while the rest continues on the current one, with promotion or rollback as a single operation.

---

## Naming and Structure

Two practices worth adopting early.

**Use a custom domain rather than the generated URL.** The `execute-api` hostname includes the API ID and the stage name in the path, which means clients hard-code both. A custom domain with base path mapping decouples them:

```
api.example.com/v1/orders     →  prod stage of the orders API
api.example.com/v2/orders     →  prod stage of a newer API
```

*Clients see a stable hostname and a version, while the underlying API and stage can change behind it.*

**Define the API in code.** An API defined by clicking has the reproducibility problems described in Fundamentals, compounded by having many small pieces. OpenAPI documents can be imported directly, and CloudFormation, SAM, or CDK define the whole API alongside its functions.

---

## What Belongs at Each Level

| Configuration | Level |
|---|---|
| Path structure | Resource |
| HTTP verb, integration, authorizer | Method |
| Throttle rate, caching, logging | Stage |
| Backend target per environment | Stage variable |
| Custom domain mapping | Domain, per stage |

The useful principle: **the API definition describes shape and behavior; the stage describes environment.** Anything differing between environments belongs in stage configuration or a stage variable, not in a separately maintained API definition.

---

## Key Takeaways

- Resources form the path structure and methods attach HTTP verbs with their integrations.
- `{proxy+}` with `ANY` forwards everything to one backend, giving up per-route authorization, validation, and metrics.
- Stages are named deployments with their own URL, throttling, logging, and caching.
- Stage variables let one API definition target different backends per environment.
- REST APIs require an explicit deployment for changes to become live — a missing deployment is the usual reason a change has no effect.
- HTTP APIs auto-deploy by default.
- REST API stages support canary releases with percentage-based traffic shifting.
- Use a custom domain with base path mapping so clients do not hard-code the API ID and stage.
