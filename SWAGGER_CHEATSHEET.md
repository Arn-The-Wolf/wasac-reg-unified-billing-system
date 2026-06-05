# WASAC/REG Utility Billing System — Swagger Cheat Sheet

> **Swagger URL:** http://localhost:8080/swagger-ui.html  
> **Base API:** http://localhost:8080/api/v1  
> **OTP source:** Check your Gmail inbox **or** the app console log (`EMAIL FALLBACK -> ... Your verification code is: XXXXXX`)

---

## Before You Start

### 1. Authorize in Swagger
1. Complete **Login + OTP** (see Task 1 below) and copy `data.token` from the response.
2. Click the green **Authorize** button (top right).
3. Paste **only the token** (no `Bearer` prefix).
4. Click **Authorize** → **Close**.

### 2. Seeded Test Accounts

| Role | Email | Password | Use For |
|------|-------|----------|---------|
| **ADMIN** | `ruyangearnold@gmail.com` | `Admin@123` | Tariffs, users, all config |
| **OPERATOR** | `operator@wasac.rw` | `Operator@123` | Customers, meters, readings, bills |
| **FINANCE** | `finance@wasac.rw` | `Finance@123` | Approve payments, view bills |
| **CUSTOMER** | `customer@wasac.rw` | `Customer@123` | View own bills & pay |

### 3. Recommended Test Order (Full Demo)

```
Task 1  → Login (get JWT)
Task 4  → View/create tariffs (ADMIN)
Task 2  → Create customer + meters (OPERATOR)
Task 3  → Record meter reading (OPERATOR)
Task 5  → Generate bill (OPERATOR)
Task 6  → Check notifications (ADMIN)
Task 5  → Record partial payment → approve (FINANCE)
Task 5  → Record remaining payment → approve (FINANCE)
Task 6  → Verify PAID notification
Task 1  → Login as CUSTOMER → view own bills
```

---

## Task 1 — User Management & Security (JWT)

**PDF requirements:** Signup, login, JWT auth, roles, phone with country code, password validation, secure all other endpoints.

### 1A. Signup (new user) — No auth required

**Endpoint:** `POST /api/v1/auth/signup`

```json
{
  "fullName": "Marie Uwase",
  "email": "marie.uwase@example.com",
  "countryCode": "+250",
  "phoneNumber": "0788123456",
  "password": "Secure@123",
  "role": "ROLE_CUSTOMER"
}
```

| Field | Rules |
|-------|-------|
| `countryCode` | Default `+250` (Rwanda) |
| `phoneNumber` | 9–12 digits, no country code |
| `password` | Min 8 chars, letters + digits + symbol (`@$!%*#?&`) |
| `role` | `ROLE_CUSTOMER` or `ROLE_OPERATOR` or `ROLE_FINANCE` — **not** `ROLE_ADMIN` |

**Expected:** `201` — *"Signup initiated. Verify OTP sent to your email."*

---

### 1B. Verify Signup OTP — No auth required

**Endpoint:** `POST /api/v1/auth/signup/verify-otp`

```json
{
  "email": "marie.uwase@example.com",
  "code": "123456"
}
```

**Expected:** `200` — returns JWT in `data.token` + account activated.

---

### 1C. Resend Signup OTP — No auth required

**Endpoint:** `POST /api/v1/auth/signup/resend-otp`

```json
{
  "email": "marie.uwase@example.com"
}
```

---

### 1D. Login (step 1 — sends OTP) — No auth required

**Endpoint:** `POST /api/v1/auth/login`

**ADMIN login:**
```json
{
  "email": "ruyangearnold@gmail.com",
  "password": "Admin@123"
}
```

**OPERATOR login:**
```json
{
  "email": "operator@wasac.rw",
  "password": "Operator@123"
}
```

**FINANCE login:**
```json
{
  "email": "finance@wasac.rw",
  "password": "Finance@123"
}
```

**CUSTOMER login:**
```json
{
  "email": "customer@wasac.rw",
  "password": "Customer@123"
}
```

**Expected:** `200` — *"Credentials valid. OTP sent to your email."*

---

### 1E. Verify Login OTP (step 2 — get JWT) — No auth required

**Endpoint:** `POST /api/v1/auth/login/verify-otp`

```json
{
  "email": "ruyangearnold@gmail.com",
  "code": "456789"
}
```

**Expected:** `200` — copy `data.token` → paste into Swagger **Authorize**.

---

### 1F. Resend Login OTP — No auth required

**Endpoint:** `POST /api/v1/auth/login/resend-otp`

```json
{
  "email": "ruyangearnold@gmail.com"
}
```

---

### Task 1 — Business Rules to Demonstrate

| Rule | How to Test in Swagger |
|------|------------------------|
| Password validation | Signup with `"password": "weak"` → `400` |
| Cannot self-register as ADMIN | Signup with `"role": "ROLE_ADMIN"` → `400` |
| Duplicate email | Signup same email twice → `409` |
| Secured endpoints | Call `GET /customers` without Authorize → `401` |
| Role-based access | OPERATOR token on `POST /config/tariffs` → `403` |

---

## Task 2 — Customer & Meter Management

**PDF requirements:** Customer details, unique national ID, no duplicates, inactive customers can't get bills. Meters: unique number, water/electricity, install date, status.

> **Use OPERATOR or ADMIN token** for create/update. Authorize first.

### 2A. Create Customer

**Endpoint:** `POST /api/v1/customers`  
**Role:** ADMIN, OPERATOR

```json
{
  "fullName": "Marie Uwase",
  "nationalId": "1199880077665545",
  "email": "marie.uwase@example.com",
  "phone": "0788123456",
  "address": "Kigali, Gasabo, Remera Sector",
  "status": "ACTIVE"
}
```

| Field | Rules |
|-------|-------|
| `nationalId` | Exactly **16 digits**, unique |
| `phone` | Rwanda format: `0788123456` or `+250788123456` |
| `status` | `ACTIVE` or `INACTIVE` |

**Save the returned `data.id`** — you'll need it as `customerId` for meters.

---

### 2B. List All Customers

**Endpoint:** `GET /api/v1/customers`  
**Role:** ADMIN, OPERATOR, FINANCE

No body. Returns all customers.

---

### 2C. Get Customer by ID

**Endpoint:** `GET /api/v1/customers/{id}`  
**Role:** ADMIN, OPERATOR, FINANCE, CUSTOMER

**Sample:** `GET /api/v1/customers/1`

---

### 2D. Update Customer

**Endpoint:** `PUT /api/v1/customers/{id}`  
**Role:** ADMIN, OPERATOR

```json
{
  "fullName": "Marie Uwase Updated",
  "nationalId": "1199880077665545",
  "email": "marie.updated@example.com",
  "phone": "0788999888",
  "address": "Kigali, Kicukiro, Niboye",
  "status": "ACTIVE"
}
```

---

### 2E. Deactivate Customer (inactive = no bills)

**Endpoint:** `PATCH /api/v1/customers/{id}/deactivate`  
**Role:** ADMIN, OPERATOR

**Sample:** `PATCH /api/v1/customers/2` — no body.

Later, try generating a bill for this customer → should fail.

---

### 2F. Activate Customer

**Endpoint:** `PATCH /api/v1/customers/{id}/activate`  
**Role:** ADMIN, OPERATOR

**Sample:** `PATCH /api/v1/customers/2` — no body.

---

### 2G. Create Water Meter

**Endpoint:** `POST /api/v1/meters`  
**Role:** ADMIN, OPERATOR

```json
{
  "meterNumber": "WTR-002-2025",
  "type": "WATER",
  "installationDate": "2025-01-15",
  "status": "ACTIVE",
  "customerId": 1
}
```

| Field | Rules |
|-------|-------|
| `meterNumber` | Unique, uppercase letters/digits/hyphens |
| `type` | `WATER` or `ELECTRICITY` |
| `installationDate` | Cannot be in the future |
| `customerId` | ID from customer create response |

**Save `data.id`** as `meterId` for readings.

---

### 2H. Create Electricity Meter

**Endpoint:** `POST /api/v1/meters`

```json
{
  "meterNumber": "ELC-002-2025",
  "type": "ELECTRICITY",
  "installationDate": "2025-02-01",
  "status": "ACTIVE",
  "customerId": 1
}
```

---

### 2I. List Meters / Get by Customer

| Endpoint | Sample |
|----------|--------|
| `GET /api/v1/meters` | All meters |
| `GET /api/v1/meters/1` | Meter by ID |
| `GET /api/v1/meters/customer/1` | All meters for customer 1 |

---

### 2J. Deactivate Meter

**Endpoint:** `PATCH /api/v1/meters/{id}/deactivate`

Inactive meter → cannot record readings.

---

### Task 2 — Business Rules to Demonstrate

| Rule | How to Test |
|------|-------------|
| Duplicate national ID | Create customer with same `nationalId` → `409` |
| Duplicate meter number | Create meter with same `meterNumber` → `409` |
| Inactive customer no bills | Deactivate customer → generate bill → `400` |
| Inactive meter no readings | Deactivate meter → record reading → `400` |

---

## Task 3 — Meter Reading Management

**PDF requirements:** Operator captures readings. Rules: current > previous, one per meter per month/year, meter must be active.

> **Use OPERATOR token.** Authorize with `operator@wasac.rw` credentials.

### 3A. Record Water Meter Reading

**Endpoint:** `POST /api/v1/meter-readings`  
**Role:** OPERATOR only

```json
{
  "meterId": 1,
  "previousReading": 100.0,
  "currentReading": 125.5,
  "readingDate": "2025-06-01"
}
```

| Field | Rules |
|-------|-------|
| `currentReading` | Must be **greater than** `previousReading` |
| `readingDate` | Cannot be in the future |
| One per month/year | Same meter + same month → `400` |

**Save `data.id`** as `meterReadingId` for bill generation.

---

### 3B. Record Electricity Reading (next month)

```json
{
  "meterId": 2,
  "previousReading": 50.0,
  "currentReading": 78.25,
  "readingDate": "2025-06-01"
}
```

---

### 3C. List Readings

| Endpoint | Sample |
|----------|--------|
| `GET /api/v1/meter-readings` | All readings |
| `GET /api/v1/meter-readings/1` | Reading by ID |
| `GET /api/v1/meter-readings/meter/1` | All readings for meter 1 |

---

### Task 3 — Business Rules to Demonstrate

| Rule | How to Test |
|------|-------------|
| Current ≤ previous | `"currentReading": 90, "previousReading": 100` → `400` |
| Duplicate month/year | Same meter, same `readingDate` month → `400` |
| Inactive meter | Deactivate meter first → record reading → `400` |
| Wrong role | ADMIN token on `POST /meter-readings` → `403` |

---

## Task 4 — Tariff, Tax & Penalty Configuration

**PDF requirements:** Admin configures flat/tier tariffs, fixed charges, VAT, penalties. Versioned — new tariffs apply only to future billing cycles.

> **Use ADMIN token** (`ruyangearnold@gmail.com`). Tariffs are pre-seeded on first run.

### 4A. View Existing Tariffs (seeded)

**Endpoint:** `GET /api/v1/config/tariffs`  
**Role:** ADMIN, FINANCE

No body. You should see:
- Water Flat Rate v1 (FLAT, 350 FRW)
- Electricity Flat Rate v1 (FLAT, 120 FRW)
- Water Tiered Rate v1 (TIER, 2 tiers)

---

### 4B. Create Flat Tariff (new version)

**Endpoint:** `POST /api/v1/config/tariffs`  
**Role:** ADMIN

```json
{
  "name": "Water Flat Rate v2",
  "tariffType": "FLAT",
  "meterType": "WATER",
  "effectiveFrom": "2026-01-01",
  "flatRate": 400.00,
  "tiers": []
}
```

> `effectiveFrom` in the **future** → applies only to bills generated after that date.

---

### 4C. Create Tier-Based Tariff

**Endpoint:** `POST /api/v1/config/tariffs`

```json
{
  "name": "Electricity Tiered Rate v2",
  "tariffType": "TIER",
  "meterType": "ELECTRICITY",
  "effectiveFrom": "2026-01-01",
  "flatRate": null,
  "tiers": [
    {
      "fromUnits": 0,
      "toUnits": 50,
      "ratePerUnit": 100.00
    },
    {
      "fromUnits": 50,
      "toUnits": null,
      "ratePerUnit": 150.00
    }
  ]
}
```

| Tier field | Meaning |
|------------|---------|
| `fromUnits` | Start of tier (kWh or m³) |
| `toUnits` | End of tier; `null` = unlimited |
| `ratePerUnit` | FRW per unit in that tier |

---

### 4D. Create Fixed Service Charge

**Endpoint:** `POST /api/v1/config/fixed-charges`

```json
{
  "name": "Monthly Connection Fee",
  "amount": 750.00,
  "effectiveFrom": "2026-01-01"
}
```

---

### 4E. Create VAT / Tax

**Endpoint:** `POST /api/v1/config/taxes`

```json
{
  "name": "VAT 18%",
  "percentage": 18.00,
  "effectiveFrom": "2026-01-01"
}
```

---

### 4F. Create Late Payment Penalty

**Endpoint:** `POST /api/v1/config/penalties`

```json
{
  "name": "Late Payment Penalty 5%",
  "percentage": 5.00,
  "effectiveFrom": "2026-01-01"
}
```

---

### 4G. View All Config

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/config/tariffs` | All tariffs |
| `GET /api/v1/config/tariffs/1` | Tariff by ID |
| `GET /api/v1/config/fixed-charges` | Fixed charges |
| `GET /api/v1/config/taxes` | Taxes |
| `GET /api/v1/config/penalties` | Penalties |

---

## Task 5 — Payment Processing

**PDF requirements:** Bill reference, amount, method, date. Partial/full payment. Update balance. Mark PAID when balance = 0.

### 5A. Generate Bill (from reading)

**Endpoint:** `POST /api/v1/bills/generate`  
**Role:** ADMIN, OPERATOR

```json
{
  "meterReadingId": 1
}
```

**Expected:** `201` — bill created with `reference`, `totalAmount`, `balance`, `status: PENDING`.

**Save:** `data.id` (billId), `data.reference`, `data.balance`.

---

### 5B. View Bills

| Endpoint | Sample | Role |
|----------|--------|------|
| `GET /api/v1/bills` | All bills | ADMIN, OPERATOR, FINANCE |
| `GET /api/v1/bills/1` | Bill by ID | All roles |
| `GET /api/v1/bills/reference/BILL-2025-06-001` | By reference | All roles |
| `GET /api/v1/bills/customer/1` | Customer's bills | All roles |

---

### 5C. Record Partial Payment

**Endpoint:** `POST /api/v1/payments`  
**Role:** ADMIN, FINANCE, CUSTOMER

```json
{
  "billId": 1,
  "amount": 1000.00,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2025-06-05",
  "notes": "Partial payment via MTN Mobile Money"
}
```

| Field | Valid Values |
|-------|-------------|
| `paymentMethod` | `MOBILE_MONEY`, `BANK_TRANSFER`, `CASH`, `CARD` |
| `paymentDate` | ISO date `YYYY-MM-DD`, cannot be in the future |

**Expected:** `201` — payment status `PENDING` (needs finance approval).

---

### 5D. Approve Payment

**Endpoint:** `PATCH /api/v1/payments/{id}/approve`  
**Role:** ADMIN, FINANCE

**Sample:** `PATCH /api/v1/payments/1` — no body.

> Switch to **FINANCE token** for this step.

**Expected:** Bill `balance` reduced. If balance > 0 → bill stays `PENDING`.

---

### 5E. Record Remaining Payment (full settlement)

**Endpoint:** `POST /api/v1/payments`

```json
{
  "billId": 1,
  "amount": 500.00,
  "notes": "Final payment - bank transfer"
}
```

Then approve: `PATCH /api/v1/payments/2/approve`

**Expected:** Bill `balance: 0`, `status: PAID`.

---

### 5F. View Payments

| Endpoint | Sample | Role |
|----------|--------|------|
| `GET /api/v1/payments` | All payments | ADMIN, FINANCE |
| `GET /api/v1/payments/pending` | Pending approval | ADMIN, FINANCE |
| `GET /api/v1/payments/bill/1` | Payments for bill 1 | ADMIN, FINANCE, CUSTOMER |

---

### 5G. Reject Payment

**Endpoint:** `PATCH /api/v1/payments/{id}/reject`  
**Role:** ADMIN, FINANCE

**Sample:** `PATCH /api/v1/payments/1` — no body.

---

### Task 5 — Business Rules to Demonstrate

| Rule | How to Test |
|------|-------------|
| Partial payment | Pay less than balance → bill still `PENDING` |
| Full payment | Pay remaining balance → bill `PAID` |
| Overpayment blocked | Pay more than balance → `400` |
| Inactive customer | Deactivate customer → generate bill → `400` |

---

## Task 6 — Database Routines & Messaging

**PDF requirements:** On bill generation → notification. On full payment → bill PAID + notify customer.

> Notifications are created automatically by DB triggers **and** email service. No manual POST needed.

### 6A. View All Notifications

**Endpoint:** `GET /api/v1/notifications`  
**Role:** ADMIN, OPERATOR, FINANCE

No body. After bill generation you should see:

```
Dear <CustomerName>, Your <Month/Year> utility bill of <Amount> FRW has been successfully processed.
```

---

### 6B. View Customer Notifications

**Endpoint:** `GET /api/v1/notifications/customer/{customerId}`  
**Role:** ADMIN, OPERATOR, FINANCE, CUSTOMER

**Sample:** `GET /api/v1/notifications/customer/1`

> CUSTOMER role can only view notifications for their own linked customer.

---

### Task 6 — What to Verify

| Event | What Happens |
|-------|--------------|
| Bill generated | Notification inserted + email sent |
| Payment approved (balance > 0) | Payment confirmation notification |
| Payment approved (balance = 0) | Bill → `PAID` + final notification |
| Email | Check Gmail or console `EMAIL FALLBACK` log |

---

## Complete End-to-End Demo Script

Use this sequence in Swagger for a full demonstration matching the PDF tasks:

| Step | Action | Token | Endpoint | Key Sample Data |
|------|--------|-------|----------|-----------------|
| 1 | Login | — | `POST /auth/login` | `ruyangearnold@gmail.com` / `Admin@123` |
| 2 | Verify OTP | — | `POST /auth/login/verify-otp` | OTP from email/logs |
| 3 | Authorize | ADMIN | Swagger Authorize | Paste JWT |
| 4 | View tariffs | ADMIN | `GET /config/tariffs` | — |
| 5 | Create customer | OPERATOR* | `POST /customers` | Marie Uwase sample |
| 6 | Create water meter | OPERATOR* | `POST /meters` | `WTR-003-2025` |
| 7 | Create elec meter | OPERATOR* | `POST /meters` | `ELC-003-2025` |
| 8 | Record reading | OPERATOR* | `POST /meter-readings` | prev 100, curr 125.5 |
| 9 | Generate bill | OPERATOR* | `POST /bills/generate` | `meterReadingId: 1` |
| 10 | Check notification | ADMIN | `GET /notifications` | Verify message format |
| 11 | Partial payment | FINANCE* | `POST /payments` | `amount: 1000` |
| 12 | Approve payment | FINANCE* | `PATCH /payments/1/approve` | — |
| 13 | Final payment | FINANCE* | `POST /payments` | remaining balance |
| 14 | Approve final | FINANCE* | `PATCH /payments/2/approve` | Bill → PAID |
| 15 | Customer view | CUSTOMER* | `GET /bills/customer/1` | Own bills only |

> *Re-login with the appropriate role and re-Authorize between role switches.

---

## Quick Reference — Enum Values

| Field | Valid Values |
|-------|-------------|
| `role` (signup) | `ROLE_CUSTOMER`, `ROLE_OPERATOR`, `ROLE_FINANCE` |
| `status` (customer) | `ACTIVE`, `INACTIVE` |
| `type` (meter) | `WATER`, `ELECTRICITY` |
| `status` (meter) | `ACTIVE`, `INACTIVE` |
| `tariffType` | `FLAT`, `TIER` |
| `meterType` (tariff) | `WATER`, `ELECTRICITY` |

---

## Common Errors & Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `401 Unauthorized` | No JWT or expired token | Re-login + re-Authorize |
| `403 Forbidden` | Wrong role for endpoint | Login with correct role |
| `409 Conflict` | Duplicate email/nationalId/meter | Use unique values |
| `400 Validation failed` | Invalid field format | Check patterns above |
| `400 OTP expired` | OTP older than 10 min | Resend OTP |
| `500 on /v3/api-docs` | Old springdoc version | Restart app after `mvn clean install` |

---

## Password Examples (Valid / Invalid)

| Password | Valid? |
|----------|--------|
| `Admin@123` | ✅ |
| `Operator@123` | ✅ |
| `Secure@123` | ✅ |
| `password` | ❌ (no symbol) |
| `Pass1` | ❌ (too short) |
| `password1` | ❌ (no symbol) |

---

*Generated for WASAC/REG Unified Utility Billing System — matches PDF Task 1–6 requirements.*
