# Configuration, Secrets & Packaging

How configuration reaches a function, why secrets should not travel with it, and the three ways code is delivered.

---

## Environment Variables

Lambda supports environment variables, set per function and per version:

```bash
aws lambda update-function-configuration \
  --function-name order-processor \
  --environment "Variables={TABLE_NAME=Orders,LOG_LEVEL=info,SECRET_ID=prod/orders/db}"
```

*Non-sensitive configuration, plus the **name** of a secret rather than its value.*

They are appropriate for resource names, feature flags, log levels, and endpoint URLs. They are the natural place for anything that differs between environments and is not sensitive.

**They are not appropriate for secrets.** Lambda environment variables are visible to anyone with `lambda:GetFunctionConfiguration`, appear in CloudFormation templates and stack outputs, and show up in deployment logs and CI output. They are encrypted at rest with KMS, which protects the stored value and does nothing about the read path.

The distinction that matters: an environment variable holds the secret's **identifier**, and the function fetches the value at runtime using its execution role.

```python
import boto3, json, os

_secret = None

def get_secret():
    global _secret
    if _secret is None:
        client = boto3.client("secretsmanager")
        _secret = json.loads(
            client.get_secret_value(SecretId=os.environ["SECRET_ID"])["SecretString"]
        )
    return _secret
```

*The function holds a name and fetches the value once per execution environment, caching it in module scope.*

The **Parameters and Secrets Lambda Extension** does this with a local cache and no code, which is usually the better option.

---

## Packaging

Three ways to deliver code.

### Zip archives

The default. Code and dependencies in a zip, uploaded directly (up to 50 MB) or via S3 (up to 250 MB unzipped).

Simple, fast to deploy, and supports editing small functions in the console. Suitable for most functions.

### Layers

A **layer** is a zip of shared content — libraries, runtime dependencies, configuration — mounted at `/opt` and shared across functions.

```bash
aws lambda publish-layer-version \
  --layer-name common-deps \
  --zip-file fileb://layer.zip \
  --compatible-runtimes python3.12
```

*One layer used by many functions. Up to five layers per function, counting toward the same 250 MB unzipped limit.*

Layers reduce duplication and deployment size. They also add a versioning burden — updating a shared layer means updating every function that references a version of it, and a layer that many functions depend on becomes something to change carefully.

Their clearest uses are large shared dependencies, the AWS-provided extensions, and separating slow-changing dependencies from fast-changing code.

### Container images

Up to **10 GB**, built from a Dockerfile and stored in ECR.

```dockerfile
FROM public.ecr.aws/lambda/python:3.12
COPY requirements.txt .
RUN pip install -r requirements.txt -t "${LAMBDA_TASK_ROOT}"
COPY app.py "${LAMBDA_TASK_ROOT}"
CMD ["app.handler"]
```

*A Lambda container image. The base image supplies the runtime interface client; a custom base must implement it.*

Container images suit large dependencies — machine learning libraries in particular — teams already using container tooling, and cases where the same image should run on Lambda and elsewhere.

The trade: a larger artifact, an ECR repository to manage, and deployment through a container build rather than a zip upload. Cold start performance is broadly comparable to zip for reasonably sized images.

---

## Versions and Aliases

**Publishing a version** creates an immutable snapshot of the code and configuration, numbered sequentially. `$LATEST` is the mutable working copy.

**An alias** is a named pointer to a version — `prod`, `staging` — which can also **split traffic** between two versions:

```bash
aws lambda update-alias --function-name order-processor --name prod \
  --function-version 7 --routing-config '{"AdditionalVersionWeights": {"8": 0.1}}'
```

*Sends 10% of invocations to version 8 and the rest to version 7 — a canary deployment, with rollback being a single alias update.*

The pattern: invoke through aliases rather than `$LATEST`, so what is running in production is an immutable, identifiable version and rollback does not require a redeployment.

---

## Key Takeaways

- Environment variables suit non-sensitive configuration and are visible to anyone who can read the function's configuration.
- Store a secret's identifier in an environment variable and fetch the value at runtime with the execution role.
- The Parameters and Secrets Lambda Extension provides cached retrieval with no code.
- Zip packaging is the default, limited to 250 MB unzipped including layers.
- Layers share dependencies across functions at the cost of version coordination.
- Container images allow up to 10 GB and suit large dependencies and container-based workflows.
- Published versions are immutable; aliases point at them and can split traffic for canary deployments.
- Invoke through aliases so production runs an identifiable version and rollback is one update.
