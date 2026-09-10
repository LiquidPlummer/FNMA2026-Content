# HTTPS, ACM & Data Transfer Cost

Serving a CloudFront distribution on a custom domain over HTTPS requires an ACM certificate — and one specific constraint about where that certificate lives stops people regularly.

---

## ACM Certificates

**AWS Certificate Manager (ACM)** issues free public TLS certificates and renews them automatically. Requesting one requires proving control of the domain, by DNS validation or email validation.

**Use DNS validation.** It requires adding a `CNAME` record — which Route 53 can do automatically — and, crucially, it enables **automatic renewal**. Email validation requires someone to click a link every renewal, which eventually gets missed and causes an outage.

```bash
aws acm request-certificate --region us-east-1 \
  --domain-name example.com \
  --subject-alternative-names "*.example.com" \
  --validation-method DNS
```

*One certificate covering the apex and all subdomains. A wildcard covers one level only — `*.example.com` matches `www.example.com` but not `a.b.example.com`.*

Certificates are free. There is no reason to buy one for use with AWS services, and no reason not to use HTTPS everywhere.

---

## The `us-east-1` Constraint

**A certificate used by CloudFront must be requested in `us-east-1`**, regardless of where anything else lives.

This is because CloudFront is a global service whose control plane runs in `us-east-1`. A certificate in `eu-west-1` simply does not appear in the CloudFront console's certificate list, which is a confusing way to discover the rule.

The constraint applies only to CloudFront. Certificates for an ALB must be in the **same region as the load balancer**. So a workload in `eu-west-1` behind CloudFront typically needs two certificates for the same domain:

| Resource | Certificate region |
|---|---|
| CloudFront | `us-east-1`, always |
| ALB in `eu-west-1` | `eu-west-1` |
| API Gateway (edge-optimized) | `us-east-1` |
| API Gateway (regional) | The API's region |

Requesting the same domain twice in different regions is normal and correct.

---

## HTTPS Configuration

Three settings on a distribution:

**Viewer protocol policy** — how clients connect. `redirect-to-https` is the usual choice, sending an HTTP request to HTTPS rather than serving it or rejecting it.

**Origin protocol policy** — how CloudFront connects to the origin. For an ALB, `https-only` keeps the second hop encrypted; `match-viewer` follows the client's protocol, which means HTTP to the origin for HTTP clients.

**Minimum protocol version** — the lowest TLS version accepted. `TLSv1.2_2021` is the reasonable default; older versions exist for legacy clients and should not be enabled without a specific reason.

**SNI is free; a dedicated IP is not.** Dedicated IP support costs roughly $600/month and is only needed for clients too old to support SNI — effectively no modern client.

---

## How CloudFront Changes Transfer Cost

CloudFront changes the data transfer picture in three ways.

**Origin-to-CloudFront transfer is free** from S3, EC2, ALB, and other AWS origins. Content moving from an origin into CloudFront costs nothing.

**CloudFront egress rates are lower** than direct S3 or EC2 egress, with volume discounts at scale.

**Cached content never touches the origin**, eliminating both the transfer and the origin request charge for every hit.

A worked comparison for 10 TB/month of public content with an 80% cache hit ratio:

```
Direct from S3:
  10,000 GB × $0.085                       = $850
  plus S3 GET requests for every request

Through CloudFront:
  10,000 GB × ~$0.085 (CloudFront egress)  = $850
  origin fetches: 2,000 GB, free
  S3 requests: 80% fewer
```

*Egress rates are broadly similar, and the saving comes from eliminating 80% of S3 requests and all origin transfer — plus better latency. At higher volumes CloudFront's tiered pricing widens the gap.*

The larger effect is on origin infrastructure: an ALB and its instances serve a fraction of the requests, so less capacity is needed.

**CloudFront also charges per request**, in addition to per gigabyte. For very high request rates with tiny responses, this can matter more than transfer.

---

## Key Takeaways

- ACM certificates are free and auto-renew when DNS validation is used; email validation requires manual action each renewal.
- A wildcard certificate covers one subdomain level only.
- CloudFront certificates must be requested in `us-east-1`; ALB certificates must be in the load balancer's own region.
- The same domain frequently needs certificates in two regions, which is normal.
- Use `redirect-to-https` for viewers and `https-only` to the origin, with a minimum of TLS 1.2.
- SNI is free; dedicated IP support is expensive and unnecessary for modern clients.
- Transfer from AWS origins into CloudFront is free, and cache hits eliminate origin requests entirely.
- CloudFront bills per request as well as per gigabyte, which matters for high-rate, small-response workloads.
