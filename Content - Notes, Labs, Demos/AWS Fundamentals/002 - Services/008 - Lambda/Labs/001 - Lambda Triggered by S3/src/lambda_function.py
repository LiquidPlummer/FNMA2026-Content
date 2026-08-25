import json
from datetime import datetime, timezone
from urllib.parse import unquote_plus

import boto3

s3 = boto3.client("s3")


def lambda_handler(event, context):
    """Triggered whenever an object is created under the incoming/ prefix.

    Reads the uploaded object, computes a small summary of it, and writes
    that summary as a JSON object under the processed/ prefix in the same
    bucket.
    """
    record = event["Records"][0]
    bucket = record["s3"]["bucket"]["name"]
    key = unquote_plus(record["s3"]["object"]["key"])

    obj = s3.get_object(Bucket=bucket, Key=key)
    body = obj["Body"].read().decode("utf-8", errors="replace")
    size_bytes = obj["ContentLength"]

    # TODO: compute line_count and word_count from `body`
    line_count = None
    word_count = None

    summary = {
        "source_key": key,
        "size_bytes": size_bytes,
        "line_count": line_count,
        "word_count": word_count,
        "processed_at": datetime.now(timezone.utc).isoformat(),
    }

    summary_key = "processed/" + key.split("/")[-1] + ".summary.json"
    s3.put_object(
        Bucket=bucket,
        Key=summary_key,
        Body=json.dumps(summary, indent=2).encode("utf-8"),
        ContentType="application/json",
    )

    print(f"Wrote summary to {summary_key}: {summary}")
    return summary
