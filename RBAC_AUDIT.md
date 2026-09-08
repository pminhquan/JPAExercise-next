# RBAC audit

## Scope and evidence

This is a read-only static audit of exactly these files:

- `src/main/java/com/hcmute/jpa/entity/User.java`
- `src/main/java/com/hcmute/jpa/filter/AuthenticationFilter.java`
- `src/main/java/com/hcmute/jpa/controller/ProductController.java`
- `src/main/java/com/hcmute/jpa/controller/CategoryController.java`
- `database.sql`

No source code was changed and no commit was created. The audit is tied to
`main` at `a96a462e2c907f07fb08afea5fcd9aa12a75e8a4`.

## 1. Current authentication flow

`AuthenticationFilter` is mapped to profile, category, product-management,
public-product, and `/admin/*`-style paths (`AuthenticationFilter.java:11-18`).
Its current flow is:

1. Read the servlet path with `request.getServletPath()` (`AuthenticationFilter.java:26-30`).
2. Allow the exact paths `/product` and `/products/detail` without creating or
   checking a session (`AuthenticationFilter.java:32-36`). These are the public
   catalog list and product-detail paths exposed by `ProductController`.
3. For every other matched path, read an existing session only with
   `getSession(false)`. A request is considered logged in when the session
   exists and `authenticatedUserId` is non-null (`AuthenticationFilter.java:38-40`).
4. Redirect an unauthenticated request to `/login` (`AuthenticationFilter.java:41-44`).
5. For an authenticated request, rewrite `/admin/product...` to the equivalent
   `/products...` target and `/admin/category...` to the equivalent
   `/categories...` target, then forward (`AuthenticationFilter.java:46-58`).
6. All other matched authenticated requests continue through the filter chain
   (`AuthenticationFilter.java:61`).

### Current authorization gaps

- There is no role read or role check anywhere in the filter.
- Any non-null `authenticatedUserId` is accepted; the filter does not re-load
  the user, check `User.active`, or verify that the session identity still
  exists.
- The `/admin/product` and `/admin/category` namespaces are currently only
  aliases. A logged-in Customer can reach them because the filter forwards
  after checking login only.
- `ProductController` sets `managementView` from the URL
  (`ProductController.java:139-145`). That is presentation state, not an
  authorization boundary and must not be used as the RBAC decision.
- The login/registration code that creates `authenticatedUserId` is outside
  the allowed audit scope. Its exact current session population and role
  assignment are therefore not observed here.

## 2. Current `User` entity fields

`User` maps to the `users` table (`User.java:7-14`) and currently contains:

| Field | Java type | Mapping / current behavior |
|---|---|---|
| `id` | `int` | `Id`, identity-generated primary key (`User.java:18-21`) |
| `username` | `String` | Required, unique, max length 50 (`User.java:23-24`) |
| `email` | `String` | Required, unique, max length 100 (`User.java:26-27`) |
| `passwordHash` | `String` | Required, max length 255 (`User.java:29-30`) |
| `active` | `boolean` | Required; Java default is `false` (`User.java:32-33`) |
| `createdAt` | `Timestamp` | Required, not updatable; populated in `@PrePersist` when absent (`User.java:35-36`, `47-52`) |
| `fullname` | `String` | Nullable `NVARCHAR(100)` (`User.java:38-39`) |
| `phone` | `String` | Nullable `NVARCHAR(30)` (`User.java:41-42`) |
| `images` | `String` | Nullable `NVARCHAR(500)` (`User.java:44-45`) |

There is no role field, role enum, authority collection, or role-related
query. The three-argument constructor initializes credentials and explicitly
leaves a new entity inactive (`User.java:57-62`); it has no role assignment.

## 3. Current database `users` table

`database.sql` defines this table (`database.sql:20-34`):

| Column | SQL type | Constraints |
|---|---|---|
| `Id` | `INT IDENTITY(1,1)` | Not null, primary key |
| `Username` | `NVARCHAR(50)` | Not null, unique |
| `Email` | `NVARCHAR(100)` | Not null, unique |
| `PasswordHash` | `NVARCHAR(255)` | Not null |
| `Active` | `BIT` | Not null |
| `CreatedAt` | `DATETIME2(6)` | Not null |
| `Fullname` | `NVARCHAR(100)` | Nullable |
| `Phone` | `NVARCHAR(30)` | Nullable |
| `Images` | `NVARCHAR(500)` | Nullable |

The table has no role column, default, or allowed-value constraint. The supplied
SQL seeds a category and products but no users (`database.sql:69-86`), so there
is no current database-level admin account defined by this file.

## 4. URLs requiring admin protection

The current filter provides login-only protection for these routes. The RBAC
target should require `ADMIN` for every management route below.

| URL | Observed behavior | RBAC decision |
|---|---|---|
| `/products` | Product list; `managementView` is true for this path (`ProductController.java:77-78`, `139-145`) | Admin only |
| `/products/add` | GET shows the form; POST inserts a product (`ProductController.java:71-73`, `98-100`) | Admin only |
| `/products/edit` | GET shows the form; POST updates a product (`ProductController.java:73-74`, `100-101`) | Admin only |
| `/products/delete` | GET redirects; POST deletes a product (`ProductController.java:79-80`, `102-103`, `540-575`) | Admin only, including the GET route |
| `/categories` with any supported action | GET lists/shows forms; POST inserts, updates, or deletes (`CategoryController.java:15`, `36-63`, `79-109`) | Admin only |
| `/admin/product` and `/admin/product/*` | Filter aliases these paths to `/products` and its suffix (`AuthenticationFilter.java:16`, `46-51`) | Admin only |
| `/admin/category` and `/admin/category/*` | Filter aliases these paths to `/categories` and its suffix (`AuthenticationFilter.java:17`, `53-57`) | Admin only |

These routes are all covered by the current filter mappings, but only the
presence of `authenticatedUserId` is enforced.

The following routes should not be made admin-only by this RBAC change:

- `/product` is the public product list (`ProductController.java:77-78` and
  `AuthenticationFilter.java:32-35`).
- `/products/detail` is the public product detail (`ProductController.java:75-76`
  and `AuthenticationFilter.java:32-35`).
- `/profile` is authenticated-user functionality, not an admin-management
  route (`AuthenticationFilter.java:12`). It should be available to both
  roles unless a separate requirement says otherwise.

The filter also lists `/product/*` (`AuthenticationFilter.java:15`), while the
allowed `ProductController` mapping only shows `/product` and the listed
`/products...` paths (`ProductController.java:32`). The wildcard is not a
confirmed supported application URL from this scope; its intended policy
should be decided before adding any new public exception.

## 5. Recommended RBAC design

### Role field

Add one required role to `User`, backed by a string-valued two-role type:

- `CUSTOMER`
- `ADMIN`

Use a string-backed enum (`@Enumerated(EnumType.STRING)`), not an ordinal, so
adding or reordering enum constants cannot silently change stored meaning.
Map it to a required `Role` column with a length limit. Add a database
constraint allowing only `ADMIN` and `CUSTOMER` as a second enforcement layer.

Do not overload `Active` to mean role. `Active` is account state; role is
authorization state.

### Default role

Every newly registered user must default to `CUSTOMER` in both places:

1. Initialize the entity/constructor to `CUSTOMER`.
2. Give the SQL column a `CUSTOMER` default and backfill existing users as
   `CUSTOMER` when the schema is migrated.

Registration must not accept a client-supplied role. An admin account should be
created only by an explicit, controlled seed or administrator operation; do not
make the first registered user an admin. The current `database.sql` does not
define such an account, so provisioning one is a separate deployment decision.

### Login session contract

After login has verified the user and account state, store only server-side
values derived from that database user:

- `authenticatedUserId` — keep the existing identity attribute.
- `authenticatedUserRole` — the canonical string `ADMIN` or `CUSTOMER`.

Never derive the role from a request parameter, hidden form field, cookie,
URL, or JSP value. A role stored in `HttpSession` is not client-editable, but
it can become stale after an admin is demoted. The database remains the source
of truth.

### AuthenticationFilter validation

Keep authorization centralized in `AuthenticationFilter`; hiding links or
setting `managementView` cannot protect an endpoint. The filter should:

1. Keep the exact public exceptions `/product` and `/products/detail`.
2. Classify the direct management routes and both `/admin/...` aliases as
   admin routes before forwarding an alias.
3. For an unauthenticated request, preserve the current login redirect.
4. For an authenticated Customer on an admin route, stop with HTTP `403` and
   never forward to a management controller.
5. For an authenticated Admin, allow the existing alias forwarding and the
   direct `/products...`/`/categories` routes.
6. Treat a missing, malformed, inactive, or unknown session identity as
   unauthenticated and fail closed.

For the safest revocation behavior, use `authenticatedUserId` to reload the
current `User` from the existing user data access path when validating a
protected request (at least every admin request), require `active == true`,
and compare the current role to `ADMIN`. Refresh the session role from that
current record or invalidate the session when it is no longer valid. A
session-only role check is the smaller baseline, but it permits a stale Admin
session to retain access after demotion.

Role-path matching should use exact routes or a slash boundary, such as
`/admin/product` or `/admin/product/...`; do not rely on an unbounded
`startsWith("/admin/product")` test. This prevents similarly prefixed paths
from being treated as aliases accidentally.

The allowed files do not expose the existing user lookup service or login
session-creation code. Those integration points must be inspected during
implementation, but no new authorization framework is needed for this
two-role policy.

## Decision summary

- Add a required string-backed `Role` field and matching constrained database
  column.
- Default all new and existing unclassified users to `CUSTOMER`.
- Store the database-derived role in the server-side session alongside
  `authenticatedUserId`; never accept role from the client.
- Protect `/products`, product add/edit/delete, `/categories`, and both admin
  alias namespaces with an `ADMIN` check in the filter.
- Leave `/product` and `/products/detail` public and `/profile` available to
  authenticated users of either role.
- Prefer current-user/database validation in the filter so active-state and
  role revocation take effect without waiting for logout.

