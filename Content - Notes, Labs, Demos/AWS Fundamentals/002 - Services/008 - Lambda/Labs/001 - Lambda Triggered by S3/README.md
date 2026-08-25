# Lambda Triggered by S3

We'll write a small Lambda function, deploy it, and wire it up so that uploading a file to an S3 bucket automatically triggers it — no server running anywhere, waiting for that upload to happen. The function reads the uploaded file, computes a tiny summary of it (size, line count, word count), and writes that summary back into the same bucket under a different prefix. This is exactly the event-driven pipeline sketched out in the Lambda notes, built for real this time.

We'll deploy everything via the AWS CLI, consistent with the rest of this course's labs — no console click-throughs to go stale on us later.

## Prerequisites

| Requirement | Notes |
|---|---|
| AWS CLI v2 | 2.27 or later. Run `aws --version` to check. |
| AWS account credentials | Configured locally via `aws configure`, with permissions to manage Lambda, IAM, and S3. |
| Python | 3.14.x locally, to match the Lambda runtime we'll deploy to (`python3.14`). Only needed for editing/testing the function — no packages need installing locally, since `boto3` already ships inside the Lambda Python runtime. `requirements.txt` is provided for reference only. |
| `zip` | Available on macOS/Linux by default; on Windows, use `Compress-Archive` in PowerShell instead (shown below where needed). |

`cd` into this lab's directory before starting.

## Guided Walkthrough

### 1. Create the bucket

```bash
export BUCKET_NAME=lambda-s3-lab-yourname-4821
export AWS_REGION=us-east-1
aws s3 mb s3://$BUCKET_NAME --region $AWS_REGION
```
*A single bucket will hold both the `incoming/` files we upload and the `processed/` summaries the function generates.*

### 2. Finish the function's summary logic

Open `src/lambda_function.py`. Most of it is already written — it downloads the newly uploaded object, and at the bottom, packages up a `summary` dict and writes it to `processed/`. But right now `line_count` and `word_count` are just placeholders:

```python
# TODO: compute line_count and word_count from `body`
line_count = None
word_count = None
```

Let's replace those two lines with the real calculation:

```python
line_count = len(body.splitlines())
word_count = len(body.split())
```
*`body` is the decoded text of the uploaded file. `splitlines()` counts lines without caring about the exact newline style, and `split()` with no arguments splits on any run of whitespace — a simple, good-enough word count.*

Save the file once that's in place.

### 3. Package and upload the function code

```bash
cd src
zip function.zip lambda_function.py
cd ..
```
*Lambda deploys from a zip archive. Since we're not bundling any third-party packages (boto3 is already provided by the runtime), the archive only needs our one source file.* On Windows PowerShell, use `Compress-Archive -Path src/lambda_function.py -DestinationPath src/function.zip` instead.

### 4. Create the function's execution role

Same pattern as the EC2 lab: the function needs an identity, and that identity should be scoped narrowly rather than granted broad access.

```bash
aws iam create-role \
  --role-name lambda-s3-lab-role \
  --assume-role-policy-document file://src/lambda-trust-policy.json
```
*`src/lambda-trust-policy.json` allows the Lambda service itself to assume this role.*

```bash
aws iam attach-role-policy \
  --role-name lambda-s3-lab-role \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole
```
*This AWS-managed policy grants only what every Lambda function needs at minimum: permission to write its own execution logs to CloudWatch Logs.*

```bash
sed "s/BUCKET_NAME/$BUCKET_NAME/g" src/lambda-s3-policy.template.json > lambda-s3-policy.json
POLICY_ARN=$(aws iam create-policy \
  --policy-name lambda-s3-lab-policy \
  --policy-document file://lambda-s3-policy.json \
  --query 'Policy.Arn' --output text)
aws iam attach-role-policy --role-name lambda-s3-lab-role --policy-arn $POLICY_ARN
sleep 15   # let the new role/policy propagate through IAM before we reference it
```
*Grants read access to `incoming/*` and write access to `processed/*` — nothing broader. The function can't touch any other bucket, or any other prefix in this one.*

### 5. Create the Lambda function

```bash
ROLE_ARN=$(aws iam get-role --role-name lambda-s3-lab-role --query 'Role.Arn' --output text)

FUNCTION_ARN=$(aws lambda create-function \
  --function-name s3-summary-lab \
  --runtime python3.14 \
  --handler lambda_function.lambda_handler \
  --role $ROLE_ARN \
  --zip-file fileb://src/function.zip \
  --query 'FunctionArn' --output text)
```
*`--handler lambda_function.lambda_handler` tells Lambda to call the `lambda_handler` function inside `lambda_function.py` — matching the file and function names we already have.*

### 6. Let S3 invoke the function

Two separate things have to line up before an S3 upload actually reaches the function: S3 needs *permission* to invoke it, and the bucket needs to be *configured* to send it notifications. Skipping either one is a common real-world mistake, so we'll do both explicitly.

```bash
aws lambda add-permission \
  --function-name s3-summary-lab \
  --statement-id AllowS3Invoke \
  --action lambda:InvokeFunction \
  --principal s3.amazonaws.com \
  --source-arn arn:aws:s3:::$BUCKET_NAME
```
*Grants the S3 service permission to invoke this specific function — this is a resource-based policy on the function itself, separate from the execution role we set up in step 4. The role controls what the function can do; this controls who's allowed to trigger it.*

```bash
sed "s|FUNCTION_ARN|$FUNCTION_ARN|" src/notification-config.template.json > notification-config.json
aws s3api put-bucket-notification-configuration \
  --bucket $BUCKET_NAME \
  --notification-configuration file://notification-config.json
```
*Tells the bucket to invoke our function on every object-created event under the `incoming/` prefix — matching the `Filter` in `notification-config.json`.*

### 7. Trigger it

```bash
aws s3 cp src/sample-data/hello.txt s3://$BUCKET_NAME/incoming/hello.txt
sleep 5   # give the trigger a moment to fire
aws s3 ls s3://$BUCKET_NAME/processed/
```
*Uploading into `incoming/` should trigger the function within a second or two, which then writes a summary file into `processed/`.*

```bash
aws s3 cp s3://$BUCKET_NAME/processed/hello.txt.summary.json - 
```
*Streams the generated summary straight to the terminal (the trailing `-` means "write to stdout instead of a file"). You should see `line_count` and `word_count` populated with real numbers instead of `null`.*

If nothing shows up in `processed/`, check the function's logs:

```bash
aws logs tail /aws/lambda/s3-summary-lab --since 5m
```
*Every Lambda function's execution logs land in CloudWatch Logs automatically — this is one of the things the `AWSLambdaBasicExecutionRole` policy from step 4 grants.*

### 8. Clean up

```bash
aws s3api put-bucket-notification-configuration --bucket $BUCKET_NAME --notification-configuration '{}'
aws lambda delete-function --function-name s3-summary-lab

aws iam detach-role-policy --role-name lambda-s3-lab-role --policy-arn $POLICY_ARN
aws iam detach-role-policy --role-name lambda-s3-lab-role --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole
aws iam delete-policy --policy-arn $POLICY_ARN
aws iam delete-role --role-name lambda-s3-lab-role

aws s3 rb s3://$BUCKET_NAME --force
```
*Removes the trigger, the function, the role and its policies, and finally the bucket. Clearing the notification configuration first avoids S3 trying to notify a function that no longer exists.*

## Exercises

Keep the same function and bucket around (or recreate them) for these — each one means editing `lambda_function.py`, re-zipping, and redeploying with:

```bash
cd src && zip function.zip lambda_function.py && cd ..
aws lambda update-function-code --function-name s3-summary-lab --zip-file fileb://src/function.zip
```

1. **Add a field.** Extend the summary to also include `character_count` (the total number of characters in `body`). Upload a new file and confirm the new field shows up correctly in its generated summary.

2. **Skip oversized files.** Before computing the summary, check `size_bytes` — if it's over some threshold you choose (say, 1000 bytes), skip the line/word counting entirely, log a message noting the file was too large to summarize, and write a summary that just records the size and a `"skipped": true` flag instead. Test it with both a small file and a deliberately larger one.

3. **A second trigger prefix.** Without changing the function's code at all, update the bucket's notification configuration so that uploads under a new `urgent/` prefix *also* trigger the function (in addition to `incoming/`). Upload a test file to `urgent/` and confirm it gets processed too — notice that this required no code change whatsoever, only a change to what the trigger is configured to listen for.
