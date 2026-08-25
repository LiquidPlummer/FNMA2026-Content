# S3 Basics — Buckets, Objects, Permissions & Static Hosting

We'll create an S3 bucket, upload and download objects through it, lock down a bucket policy so only one prefix is publicly readable, and turn that public prefix into a working static website. By the end, we'll have hands-on experience with every concept from the S3 notes — buckets, objects, keys, the permissions tab, and the "storage, hosting, backups" use cases — instead of just having read about them.

We'll drive this entirely from the AWS CLI rather than the console. It's more reproducible to write down as a lab, and the commands themselves double as a record of exactly what we did and why.

## Prerequisites

| Requirement | Notes |
|---|---|
| AWS CLI v2 | 2.27 or later. Run `aws --version` to check; see the [install/update guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) if you need to install or upgrade. |
| AWS account credentials | Configured locally via `aws configure` (or an equivalent credential source), with permissions to create and manage S3 buckets. |
| A terminal | Examples below use bash syntax (`export VAR=value`). If you're on Windows PowerShell, use `$env:VAR = "value"` instead — the `aws` commands themselves are identical either way. |

Clone or copy this lab folder locally, then `cd` into it before starting — the guided steps below assume your terminal is sitting in this lab's root directory (`src/` should be right there next to this README).

## Guided Walkthrough

### 1. Pick a bucket name and create the bucket

S3 bucket names are **globally unique across all of AWS**, not just within your account (a detail called out in the S3 notes). We can't just use `s3-lab-bucket` — someone else has almost certainly already taken it. Pick something distinctive, like your name plus a random suffix:

```bash
export BUCKET_NAME=s3-lab-yourname-4821
export AWS_REGION=us-east-1   # or whichever region you're working in
```
*Environment variables holding our unique bucket name and target region — we'll reuse these throughout.*

Now create the bucket:

```bash
aws s3 mb s3://$BUCKET_NAME --region $AWS_REGION
```
*Creates a new, empty bucket in the chosen region.*

Confirm it exists:

```bash
aws s3 ls
```
*Lists every bucket in the account — our new bucket should be in the list.*

### 2. Upload and download objects

Let's upload the sample file from `src/sample-data/`:

```bash
aws s3 cp src/sample-data/welcome.txt s3://$BUCKET_NAME/welcome.txt
```
*Uploads a single local file to the bucket under the key `welcome.txt`.*

List what's in the bucket:

```bash
aws s3 ls s3://$BUCKET_NAME
```
*Shows the objects currently stored in the bucket — `welcome.txt` should be there.*

Now let's pull it back down to prove the round trip works:

```bash
aws s3 cp s3://$BUCKET_NAME/welcome.txt ./downloaded-welcome.txt
diff src/sample-data/welcome.txt downloaded-welcome.txt
```
*Downloads the object back to a local file and diffs it against the original — no output from `diff` means they're identical.*

### 3. Upload the website files under their own prefix

Recall from the S3 notes that S3 doesn't have real folders — a key like `website/index.html` just *looks* like a folder because of the shared prefix. Let's upload the whole `src/website/` directory that way:

```bash
aws s3 cp src/website s3://$BUCKET_NAME/website/ --recursive
```
*Uploads every file under `src/website/` to the bucket, each keyed under the `website/` prefix.*

```bash
aws s3 ls s3://$BUCKET_NAME/website/
```
*Confirms both `index.html` and `error.html` landed under that prefix.*

At this point everything in the bucket is private by default — nobody outside our account can read any of it yet, `welcome.txt` included.

### 4. Allow public read access — but only for the website prefix

S3 buckets block public access by default, as a safety net. To publish the website prefix, we first need to turn that blanket block off for this bucket, and then grant read access narrowly, with a bucket policy scoped to just the `website/` prefix — not the whole bucket. This is the same instinct as least privilege from the IAM lesson, applied to a bucket policy: grant exactly what's needed, nothing more.

```bash
aws s3api put-public-access-block \
  --bucket $BUCKET_NAME \
  --public-access-block-configuration BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false
```
*Disables the account-level "block all public access" safety net for this one bucket, so a bucket policy is allowed to grant public access at all.*

Now fill in the policy template with our actual bucket name and apply it:

```bash
sed "s/BUCKET_NAME/$BUCKET_NAME/" src/bucket-policy.template.json > bucket-policy.json
aws s3api put-bucket-policy --bucket $BUCKET_NAME --policy file://bucket-policy.json
```
*Substitutes our real bucket name into the policy template, then attaches it to the bucket. The policy only grants `s3:GetObject` on keys under `website/*` — everything else in the bucket, `welcome.txt` included, stays private.*

### 5. Enable static website hosting

```bash
aws s3 website s3://$BUCKET_NAME --index-document index.html --error-document error.html
```
*Turns on static website hosting for the bucket, pointed at our two uploaded HTML files.*

Every bucket configured this way gets its own website endpoint URL. Look it up rather than guessing at the format (it varies by region):

```bash
aws s3api get-bucket-location --bucket $BUCKET_NAME
```
*Confirms the bucket's region, which determines the shape of its website endpoint.*

Using that region, the endpoint follows the pattern `http://$BUCKET_NAME.s3-website-$AWS_REGION.amazonaws.com`. Since our content lives under the `website/` prefix rather than the bucket root, browse to:

```
http://<your-bucket-name>.s3-website-<region>.amazonaws.com/website/index.html
```

You should see "It's alive!" rendered as a real web page — served straight out of the bucket, no server involved.

### 6. Verify the private object stayed private

Our policy only opened up the `website/` prefix. Let's confirm `welcome.txt` is still locked down, using the same endpoint style but pointed at that key instead:

```bash
curl -i "http://$BUCKET_NAME.s3-website-$AWS_REGION.amazonaws.com/welcome.txt"
```
*Requests the private object anonymously over HTTP — this should come back with an access-denied or not-found response, unlike the website files.*

If that request fails to load rather than succeeding, the scoped policy is doing its job.

### 7. Clean up

S3 storage is cheap but not free, and a bucket with public access enabled is worth tidying up promptly rather than leaving around. Empty and delete the bucket when you're done:

```bash
aws s3 rb s3://$BUCKET_NAME --force
```
*Deletes every object in the bucket and then the bucket itself. `--force` is what allows deleting a non-empty bucket in one step — without it, `s3 rb` refuses to delete a bucket that still has objects in it.*

## Exercises

Now that the guided bucket is torn down, work through these on a fresh bucket of your own (or a fresh prefix in a new one) — without step-by-step instructions this time.

1. **Versioning round trip.** Create a new bucket and enable versioning on it (`aws s3api put-bucket-versioning`). Upload a text file, then upload a second, *different* version of the same file to the same key. Use `aws s3api list-object-versions` to find the version ID of the original upload, and download that specific older version back down — confirming it still matches the original content, even though the key now points at the newer one.

2. **A second, differently-scoped policy.** Using a bucket with two prefixes (say, `public/` and `private/`), write your own bucket policy from scratch that grants public read access to `public/*` only. Verify both that `public/` objects are reachable anonymously and that `private/` objects are not — don't reuse the policy from the walkthrough, write the JSON yourself.

3. **Explicit deny.** Add a second statement to your policy from exercise 2 that explicitly denies `s3:GetObject` on one specific object under `public/` (by its exact key), even though the broader `public/*` statement would otherwise allow it. Upload an object at that exact key and confirm it's unreachable, while everything else under `public/` still works. This demonstrates that an explicit `Deny` always wins over an `Allow`, no matter which statement is listed first.
