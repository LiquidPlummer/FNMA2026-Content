# Consistency & Static Website Hosting

Two independent S3 behaviors worth knowing: what guarantees a read gives after a write, and how S3 serves a website directly.

---

## Consistency

**S3 provides strong read-after-write consistency** for all operations — `PUT`, `DELETE`, and `LIST` — in every region.

After a successful `PUT`, any subsequent `GET` returns the new data. After a `DELETE`, subsequent reads return 404. A `LIST` immediately reflects the change. There is no propagation delay and no configuration for this.

This has been true since December 2020. **Older documentation, tutorials, and Stack Overflow answers describe eventual consistency**, where a read after an overwrite could return stale data and applications inserted retries or delays to compensate. That workaround is no longer necessary, and code still carrying it is doing pointless work.

What strong consistency does **not** cover:

**Concurrent writes to the same key are last-writer-wins**, and "last" is determined by S3, not by the order requests were sent. There is no locking and no compare-and-swap. Two processes writing the same key concurrently produce one surviving object, with no guarantee which.

**Bucket configuration changes are eventually consistent.** A bucket policy update may take a short time to apply everywhere. This is occasionally visible as a permission change that seems not to have taken effect immediately.

For coordinated updates, S3 is not the right tool. DynamoDB provides conditional writes; S3 does not.

---

## Static Website Hosting

S3 can serve a bucket's contents as a website:

```bash
aws s3 website s3://my-site --index-document index.html --error-document error.html
```

*Enables website hosting with a default document for directory-style requests and a custom error page.*

This produces a **website endpoint** distinct from the REST endpoint:

```
http://my-site.s3-website-us-east-1.amazonaws.com   ← website endpoint
https://my-site.s3.us-east-1.amazonaws.com          ← REST endpoint
```

The differences matter:

| | Website endpoint | REST endpoint |
|---|---|---|
| HTTPS | **No** | Yes |
| Index document | Yes | No |
| Custom error pages | Yes | No |
| Redirect rules | Yes | No |
| Requires public bucket | **Yes** | No |
| Supports presigned URLs | No | Yes |

**The website endpoint is HTTP only.** There is no way to add TLS to it. That alone rules it out for most real use.

---

## The Better Pattern

Serve the site through **CloudFront with Origin Access Control**, using the REST endpoint as the origin:

```
Browser ──HTTPS──► CloudFront ──OAC──► S3 REST endpoint (bucket stays private)
```

*CloudFront provides HTTPS, caching, and a custom domain, while the bucket remains completely private.*

This gives:

- **HTTPS** with a free ACM certificate
- **A custom domain** via Route 53
- **Caching at edge locations**, reducing both latency and S3 request charges
- **A private bucket**, with Block Public Access fully enabled
- **Lower data transfer cost** than serving from S3 directly

The one thing to configure is single-page application routing. CloudFront needs a custom error response mapping 403 and 404 to `/index.html` with a 200 status, so client-side routes resolve rather than returning an error.

Static website hosting on S3 remains useful in two narrow cases: a genuinely internal or throwaway site where HTTP is acceptable, and redirect-only buckets — a bucket configured purely to redirect `example.com` to `www.example.com`, which needs no content at all.

---

## Practical Notes

**Content types matter.** S3 serves the `Content-Type` set at upload. A file uploaded without one is served as `application/octet-stream`, and the browser downloads it instead of rendering it. `aws s3 sync` infers types from file extensions; direct API uploads must set it explicitly.

**Cache headers matter.** Setting `Cache-Control` at upload controls both browser and CloudFront caching. The usual pattern for a built front end: long cache lifetimes for fingerprinted assets, and a short one for `index.html`.

```bash
aws s3 sync ./dist s3://my-site --cache-control "public,max-age=31536000" \
  --exclude index.html
aws s3 cp ./dist/index.html s3://my-site/ --cache-control "public,max-age=0,must-revalidate"
```

*Immutable hashed assets cached for a year, and the entry point never cached — so a deployment takes effect immediately without invalidating anything.*

---

## Key Takeaways

- S3 provides strong read-after-write consistency for all operations in all regions; older advice about eventual consistency is obsolete.
- Concurrent writes to one key are last-writer-wins with no locking or conditional put.
- The S3 website endpoint supports index documents, error pages, and redirects, but is HTTP only and requires a public bucket.
- Serve sites through CloudFront with Origin Access Control instead, keeping the bucket private and gaining HTTPS, caching, and a custom domain.
- For single-page applications, map CloudFront 403 and 404 responses to `/index.html` with a 200 status.
- Set `Content-Type` at upload or files are served as generic binary downloads.
- Use long cache lifetimes for fingerprinted assets and no caching for the entry point.
