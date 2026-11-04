# Deploying Bookora on Render

[`render.yaml`](../render.yaml) at the root of the repository describes everything Render has to create:

| Part            | Render type                  | What it runs                                                              |
| --------------- | ---------------------------- | ------------------------------------------------------------------------- |
| `bookora-db`    | PostgreSQL                   | The database. Flyway builds the schema and the demo data on first start.   |
| `bookora-backend` | Web service (Docker)       | Spring Boot (`docker/backend.Dockerfile`).                                 |
| `bookora-web`   | Web service (Docker)         | Apache with the React build, `.htaccess` and the proxy to the backend (`docker/web.Dockerfile`). This is the public address. |

Render ends HTTPS itself, so the site is served over HTTPS without any certificate work. Visitors only talk to
`bookora-web`; it forwards `/api` to the backend over Render's private network.

## 1. Create the services

1. Push the repository to GitHub (the `main` branch).
2. In Render: **New → Blueprint**, connect the GitHub repository, choose the `main` branch.
3. Render reads `render.yaml`, lists the three resources and asks for the values marked `sync: false`:

| Variable                  | Where it goes     | Value                                                                           |
| ------------------------- | ----------------- | ------------------------------------------------------------------------------- |
| `PUBLIC_BASE_URL`         | backend           | The public address, for example `https://bookora-web.onrender.com` (see step 2). |
| `ADMIN_EMAIL`             | backend           | The email of the first administrator.                                           |
| `ADMIN_PASSWORD`          | backend           | A strong password for that account.                                             |
| `STRIPE_SECRET_KEY`       | backend           | The Stripe **test** secret key (`sk_test_...`).                                 |
| `STRIPE_WEBHOOK_SECRET`   | backend           | Leave empty for now; filled in at step 3.                                       |
| `TURNSTILE_SECRET`        | backend           | The Cloudflare Turnstile secret key.                                            |
| `VITE_TURNSTILE_SITE_KEY` | web               | The Turnstile **site** key (public).                                            |

4. Press **Apply**. The first build takes several minutes (Maven and npm run inside Docker).

## 2. Set the real address

When the services are live, open `bookora-web` in the dashboard and copy its address (Render adds a suffix when the
name `bookora-web` is already taken). If it differs from what you entered, edit `PUBLIC_BASE_URL` on `bookora-backend`
to that address and let it redeploy. The address is used for the payment redirects and the sitemap.

## 3. Stripe webhook

In the Stripe dashboard (test mode): **Developers → Webhooks → Add endpoint**.

- Endpoint URL: `https://<your address>/api/webhooks/stripe`
- Events: `checkout.session.completed` and `checkout.session.expired`

Copy the **signing secret** (`whsec_...`) into `STRIPE_WEBHOOK_SECRET` on `bookora-backend`. Pay with the test card
`4242 4242 4242 4242` and check that the order turns to **Paid**.

## 4. Turnstile

In the Cloudflare Turnstile widget settings add the hostname of the site (for example `bookora-web.onrender.com`).
The site key is part of the page, so after changing `VITE_TURNSTILE_SITE_KEY` run **Manual Deploy → Clear build cache &
deploy** on `bookora-web`.

## 5. After it is online

- Sign in with `ADMIN_EMAIL` / `ADMIN_PASSWORD` and open **Your account → Administration**.
- Measure the security headers (securityheaders.com) and Lighthouse on the public address; both need the real HTTPS site.
- The migrations also create the **demo customers** (`@example.com`, with the password from the README). They are fine
  for a demonstration; remove them before real customers use the shop.

## What the free plans mean

`render.yaml` uses `plan: free` everywhere so the demo costs nothing. The limits:

- **Sleeping:** a service with no visits for 15 minutes goes to sleep. The next visit waits about a minute while it
  starts. A Stripe webhook that arrives while the backend sleeps fails once and is retried by Stripe.
- **Database:** a free PostgreSQL database is deleted after 30 days. Upgrade it (or export the data) before that.
- **Files:** uploaded covers and e-books live on the backend's disk, which is erased at every restart on the free plan.
  The demo e-books come back by themselves (the backend attaches them at start). Uploaded covers do not. A paid plan
  with a disk keeps them: uncomment the `disk` block in `render.yaml`.
- **Memory:** 512 MB. `JAVA_TOOL_OPTIONS` in `render.yaml` keeps the Java heap small enough.

## How the pieces are tied together

- `PORT`: Render tells each service which port to listen on. The backend reads it (`server.port=${PORT:8080}`) and
  Apache reads it (`Listen ${PORT}`); the Docker defaults (8080 and 80) are used locally.
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` come from the Render database. Locally a single `DB_URL` is used.
- `BACKEND_HOST` (`host:port`) tells Apache where the backend is.
- `TRUST_PROXY=true`: Render's load balancer puts the visitor's address in `X-Forwarded-For`. Apache keeps the header
  and the backend only believes the entries added by private addresses, so the rate limit counts each visitor
  separately and a made-up header does not help. Locally Apache removes the header.
- `COOKIE_SECURE=true`: the session cookie is only sent over HTTPS.
- Apache also recognises HTTPS through `X-Forwarded-Proto`, so the HSTS header is sent on Render.
