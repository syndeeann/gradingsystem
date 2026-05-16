# Security Architecture

## Authentication Flow

```
POST /api/auth/login  { username, password }
        │
        ▼
AuthController → AuthServiceImpl
        │  AuthenticationManager.authenticate()
        │  ├─ CustomUserDetailsService.loadUserByUsername()   ← DB (login only)
        │  └─ BCryptPasswordEncoder.matches()
        │
        ▼  (success)
JwtUtil.generateToken(principal, userId, role, permissions[])
        │  Claims: sub=username, userId, role, permissions[]
        │
        ▼
LoginResponse { token, tokenType, expiresIn, userId, username, fullName, role, permissions[] }
```

Every subsequent request:
```
Authorization: Bearer <token>
        │
        ▼
JwtAuthFilter.doFilterInternal()
        │  JwtUtil.validateToken()   ← signature + expiry (no DB)
        │  extract: username, userId, role, permissions[]
        │  build UserPrincipal.fromToken(…)
        │
        ▼
SecurityContextHolder  ← UsernamePasswordAuthenticationToken
```

---

## Authority Naming Convention

All permission authorities follow the pattern **`RESOURCE_ACTION`** (uppercase, underscore-separated).

| Resource       | Actions                                      |
|---------------|----------------------------------------------|
| `GRADES`      | `CREATE` `READ` `UPDATE` `DELETE` `VIEW_PAGE` |
| `SUBJECTS`    | `CREATE` `READ` `UPDATE` `DELETE` `VIEW_PAGE` |
| `USERS`       | `CREATE` `READ` `UPDATE` `DELETE` `VIEW_PAGE` |
| `REPORTS`     | `CREATE` `READ` `UPDATE` `DELETE` `VIEW_PAGE` |
| `SCHEDULES`   | `CREATE` `READ` `UPDATE` `DELETE` `VIEW_PAGE` |
| `ENROLLMENTS` | `CREATE` `READ` `UPDATE` `DELETE` `VIEW_PAGE` |

**Examples:** `GRADES_CREATE`, `USERS_READ`, `REPORTS_VIEW_PAGE`

A role authority is also emitted: `ROLE_ADMIN`, `ROLE_TEACHER`, `ROLE_STUDENT`, `ROLE_REGISTRAR`, `ROLE_SECRETARY`.

---

## Using @PreAuthorize

### Simple authority check (preferred)
```java
@PreAuthorize("hasAuthority('GRADES_CREATE')")
public GradeResponse recordGrade(GradeRequest request) { … }

@PreAuthorize("hasAuthority('GRADES_READ')")
public List<GradeResponse> getGradesByStudent(String studentId) { … }
```

### Role check
```java
@PreAuthorize("hasRole('ADMIN')")                // checks ROLE_ADMIN
public void deleteUser(String id) { … }

@PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
public void updateEnrollment(…) { … }
```

### Compound expressions
```java
@PreAuthorize("hasAuthority('REPORTS_VIEW_PAGE') or hasRole('ADMIN')")
public Page<ReportSummary> listReports(Pageable p) { … }
```

### Object-level security (via CustomPermissionEvaluator)
```java
// Use hasPermission() when ownership rules apply (e.g. a student can only
// view their own grades). Implement CustomPermissionEvaluator accordingly.
@PreAuthorize("hasPermission(#studentId, 'GRADE', 'READ')")
public List<GradeResponse> getMyGrades(String studentId) { … }
```

---

## Permission Matrix (seed data roles)

| Authority           | ADMIN | TEACHER | STUDENT | REGISTRAR | SECRETARY |
|---------------------|:-----:|:-------:|:-------:|:---------:|:---------:|
| `GRADES_CREATE`     | ✓     | ✓       |         |           |           |
| `GRADES_READ`       | ✓     | ✓       | ✓       | ✓         | ✓         |
| `GRADES_UPDATE`     | ✓     | ✓       |         |           |           |
| `GRADES_DELETE`     | ✓     |         |         |           |           |
| `GRADES_VIEW_PAGE`  | ✓     | ✓       | ✓       | ✓         | ✓         |
| `USERS_CREATE`      | ✓     |         |         |           |           |
| `USERS_READ`        | ✓     |         |         | ✓         |           |
| `USERS_UPDATE`      | ✓     |         |         |           |           |
| `USERS_DELETE`      | ✓     |         |         |           |           |
| `ENROLLMENTS_*`     | ✓     | ✓ (R)   |         | ✓         | ✓ (R)     |
| `REPORTS_VIEW_PAGE` | ✓     | ✓       |         | ✓         | ✓         |

---

## JWT Secret

Set `JWT_SECRET` to a Base64-encoded 256-bit (32-byte) random key in production.

```bash
# generate a suitable secret:
openssl rand -base64 32
```

Never commit the real secret to source control. Use environment variables or a secrets manager (Vault, AWS Secrets Manager, etc.).

---

## Token Blacklisting (TODO)

`AuthServiceImpl.logout()` currently drops the token client-side only.  
Server-side invalidation requires storing the token `jti` claim in Redis until expiry.  
See the TODO comment in `AuthServiceImpl.logout()`.
