# Migration Service Documentation

## Overview

The Migration Service is a flexible, asynchronous background service for tracking customer migration from Oracle to PostgreSQL. It supports multiple configurable stages, each with its own status tracking, and provides job tracking with UUID for reference purposes.

## Key Features

- **Flexible Stage System**: Add new migration stages dynamically without code changes
- **Background Processing**: All migrations run asynchronously using Spring's `@Async`
- **Job Tracking**: Each migration job has a UUID (`job_id`) for tracking and reference
- **Status Tracking**: Each stage has status (PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED)
- **Progress Monitoring**: Track completion percentage and completed stages count
- **Retry Support**: Automatically retry failed stages
- **Cache-Aside Pattern**: Efficient data access patterns

## Architecture

### Components

1. **MigrationStage** (Master Data)
   - Defines available migration stages
   - Configurable via database (no code changes needed)
   - Fields: `code`, `name`, `displayOrder`, `isFinal`, `isMandatory`

2. **CustomerMigrationStage** (Customer Progress)
   - Tracks each customer's progress per stage
   - One record per customer per stage
   - Fields: `stageStatus`, `startedAt`, `completedAt`, `retryCount`, `errorMessage`

3. **MigrationJob** (Job Tracking)
   - Tracks async migration jobs with UUID
   - Fields: `jobId` (UUID), `jobStatus`, `completedStages`, `totalStages`, `progressPercentage`

4. **MigrationStageHandler** (Business Logic)
   - Interface for stage-specific migration logic
   - Each stage has its own handler implementation
   - Handlers execute Oracle → PostgreSQL migration

5. **MigrationBackgroundProcessor** (Async Executor)
   - Processes stages asynchronously
   - Updates job status and progress
   - Handles errors and retries

## Database Schema

### Tables

#### `dgtl_migration_stage`
Master table for migration stages. Add new stages by inserting records.

```sql
CREATE TABLE dgtl_migration_stage (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER NOT NULL,
    is_final BOOLEAN NOT NULL DEFAULT FALSE,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    ...
);
```

#### `dgtl_customer_migration_stage`
Tracks customer progress per stage.

```sql
CREATE TABLE dgtl_customer_migration_stage (
    id BIGSERIAL PRIMARY KEY,
    customer_key VARCHAR(255) NOT NULL,
    stage_id BIGINT NOT NULL REFERENCES dgtl_migration_stage(id),
    stage_status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    retry_count INTEGER DEFAULT 0,
    ...
    UNIQUE (customer_key, stage_id)
);
```

#### `dgtl_migration_job`
Tracks async migration jobs with UUID.

```sql
CREATE TABLE dgtl_migration_job (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL UNIQUE,  -- UUID
    customer_key VARCHAR(255) NOT NULL,
    job_type VARCHAR(50),
    job_status VARCHAR(50) NOT NULL,
    completed_stages INTEGER DEFAULT 0,
    total_stages INTEGER,
    ...
);
```

## Default Stages

The system comes with 4 default stages (Oracle → PostgreSQL):

1. **MASTER_ACCOUNT** (Order: 1)
   - Migrates master account data
   - Handler: `MasterAccountMigrationHandler`

2. **MASTER_BINDING** (Order: 2)
   - Migrates master binding relationships
   - Handler: `MasterBindingMigrationHandler`

3. **DEVICE** (Order: 3)
   - Migrates device registration data
   - Handler: `DeviceMigrationHandler`

4. **NOTIFICATION** (Order: 4, Final)
   - Migrates notification preferences
   - Handler: `NotificationMigrationHandler`

## API Endpoints

### Submit Migration Job

**POST** `/migration/submit/{customerKey}`

Submits a full migration job for a customer. Returns immediately with `job_id` (UUID).

**Response:** `202 ACCEPTED`

```json
{
  "status": "success",
  "data": {
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "customerKey": "CUST001",
    "jobType": "FULL_MIGRATION",
    "jobStatus": "ACCEPTED",
    "message": "Migration job accepted and queued for processing"
  }
}
```

### Submit Single Stage Job

**POST** `/migration/submit/{customerKey}/stage/{stageCode}`

Submits a single stage migration job.

**Response:** `202 ACCEPTED`

```json
{
  "status": "success",
  "data": {
    "jobId": "660e8400-e29b-41d4-a716-446655440001",
    "customerKey": "CUST001",
    "jobType": "SINGLE_STAGE",
    "stageCode": "MASTER_ACCOUNT",
    "jobStatus": "ACCEPTED"
  }
}
```

### Submit Retry Job

**POST** `/migration/retry/{customerKey}`

Retries all failed stages for a customer.

**Response:** `202 ACCEPTED`

### Get Job Status

**GET** `/migration/job/{jobId}`

Gets the current status of a migration job by UUID.

**Response:** `200 OK`

```json
{
  "status": "success",
  "data": {
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "customerKey": "CUST001",
    "jobType": "FULL_MIGRATION",
    "jobStatus": "IN_PROGRESS",
    "completedStages": 2,
    "totalStages": 4,
    "progressPercentage": 50.0,
    "startedAt": "2024-12-28T10:30:00"
  }
}
```

**Job Status Values:**
- `ACCEPTED` - Job accepted, queued for processing
- `IN_PROGRESS` - Job currently being processed
- `COMPLETED` - Job completed successfully
- `FAILED` - Job failed
- `CANCELLED` - Job cancelled

### Get Customer Jobs

**GET** `/migration/jobs/{customerKey}`

Gets all migration jobs for a customer, ordered by most recent first.

**Response:** `200 OK`

```json
{
  "status": "success",
  "data": [
    {
      "jobId": "550e8400-e29b-41d4-a716-446655440000",
      "jobStatus": "COMPLETED",
      "completedStages": 4,
      "totalStages": 4,
      "progressPercentage": 100.0,
      "completedAt": "2024-12-28T10:35:00"
    },
    ...
  ]
}
```

### Check Migration Status

**GET** `/migration/check/{customerKey}`

Checks the migration status for a customer. Returns current stage, whether at final stage, and next pending stage.

**Response:** `200 OK`

```json
{
  "status": "success",
  "data": {
    "customerKey": "CUST001",
    "migrationCompleted": false,
    "isFinalStage": false,
    "completedStagesCount": 2,
    "totalStagesCount": 4,
    "progressPercentage": 50.0,
    "currentStage": {
      "stage": {
        "code": "DEVICE",
        "displayOrder": 3
      },
      "stageStatus": "PENDING"
    },
    "nextPendingStage": {
      "stage": {
        "code": "DEVICE",
        "displayOrder": 3
      },
      "stageStatus": "PENDING"
    },
    "allStages": [...]
  }
}
```

### Update Stage Status (Manual)

**PUT** `/migration/stage/update`

Manually updates the status of a specific migration stage.

**Request Body:**
```json
{
  "customer_key": "CUST001",
  "stage_code": "MASTER_ACCOUNT",
  "stage_status": "COMPLETED",
  "error_message": null,
  "error_code": null
}
```

**Stage Status Values:**
- `PENDING` - Stage not yet started
- `IN_PROGRESS` - Stage currently being processed
- `COMPLETED` - Stage completed successfully
- `FAILED` - Stage failed, needs retry
- `SKIPPED` - Stage skipped (optional stage)

### Get All Stages

**GET** `/migration/stages`

Returns all available migration stages in order.

**Response:** `200 OK`

```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "code": "MASTER_ACCOUNT",
      "name": "Master Account Migration",
      "description": "Migrate master account data from Oracle to PostgreSQL",
      "displayOrder": 1,
      "isFinal": false,
      "isMandatory": true
    },
    ...
  ]
}
```

## Usage Flow

### Typical Migration Flow

1. **Submit Migration**
   ```bash
   POST /migration/submit/CUST001
   ```
   Returns `job_id` immediately (HTTP 202 ACCEPTED)

2. **Poll Job Status**
   ```bash
   GET /migration/job/{jobId}
   ```
   Check `jobStatus` and `progressPercentage`

3. **Check Migration Status** (Optional)
   ```bash
   GET /migration/check/CUST001
   ```
   See detailed stage-by-stage progress

4. **When Complete**
   - `jobStatus` = `COMPLETED`
   - `progressPercentage` = 100.0
   - `migrationCompleted` = true

### Error Handling

If a stage fails:
- `jobStatus` = `FAILED`
- `errorCode` and `errorMessage` populated
- Use `/migration/retry/{customerKey}` to retry failed stages

## Adding New Stages

To add a new migration stage:

### 1. Insert Stage into Database

```sql
INSERT INTO dgtl_migration_stage 
  (code, name, description, display_order, is_final, is_mandatory, status, created_date, version)
VALUES 
  ('NEW_STAGE', 'New Stage Name', 'Description', 5, TRUE, TRUE, 'ACTIVE', NOW(), 1);
```

**Important:**
- Set `is_final = TRUE` on the new last stage
- Update previous final stage to `is_final = FALSE`
- Set `display_order` to control sequence

### 2. Create Handler Implementation

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class NewStageMigrationHandler implements MigrationStageHandler {

    public static final String STAGE_CODE = "NEW_STAGE";

    @Override
    public String getStageCode() {
        return STAGE_CODE;
    }

    @Override
    public MigrationResult execute(CustomerMigrationStage customerMigrationStage) {
        String customerKey = customerMigrationStage.getCustomerKey();
        log.info("Starting NEW_STAGE migration for customer: {}", customerKey);

        try {
            // TODO: Implement Oracle → PostgreSQL migration logic
            // 1. Fetch data from Oracle
            // 2. Transform data if needed
            // 3. Save to PostgreSQL
            // 4. Verify data integrity

            log.info("NEW_STAGE migration completed for customer: {}", customerKey);
            return MigrationResult.success();

        } catch (Exception e) {
            log.error("NEW_STAGE migration failed for customer: {}", customerKey, e);
            return MigrationResult.failure("NEW_STAGE_ERROR", e.getMessage());
        }
    }
}
```

### 3. Handler Auto-Discovery

The handler will be automatically discovered and registered. No additional configuration needed.

## Background Processing

### How It Works

1. **Job Submission**
   - User calls submit endpoint
   - Job created with UUID and `ACCEPTED` status
   - Returns immediately with `job_id`

2. **Async Processing**
   - `MigrationBackgroundProcessor` picks up job
   - Updates job to `IN_PROGRESS`
   - Processes stages sequentially

3. **Stage Processing**
   - For each stage:
     - Finds appropriate handler
     - Executes migration logic
     - Updates stage status
     - Updates job progress

4. **Completion**
   - When all stages complete → `COMPLETED`
   - If any stage fails → `FAILED`
   - Updates `completedAt` timestamp

### Thread Pool Configuration

Configured in `AsyncConfig.java`:
- Core pool size: 10
- Max pool size: 20
- Queue capacity: 500

## Stage Status Flow

```
PENDING → IN_PROGRESS → COMPLETED
                ↓
              FAILED → (retry) → IN_PROGRESS
```

## Job Status Flow

```
ACCEPTED → IN_PROGRESS → COMPLETED
                ↓
              FAILED
```

## Error Handling

### Stage Failures

- Stage marked as `FAILED`
- Error details stored (`errorCode`, `errorMessage`)
- `retryCount` incremented
- Job continues to next stage (if configured) or stops

### Job Failures

- Job marked as `FAILED`
- Error details stored
- Use retry endpoint to restart from failed stages

## Best Practices

1. **Polling Frequency**
   - Poll `/migration/job/{jobId}` every 5-10 seconds
   - Don't poll too frequently to avoid load

2. **Error Handling**
   - Always check `jobStatus` and `errorMessage`
   - Implement retry logic for transient failures

3. **Stage Design**
   - Keep stages atomic and independent
   - Make stages idempotent (safe to retry)

4. **Monitoring**
   - Monitor job completion rates
   - Track stage failure rates
   - Alert on high failure rates

## Example Integration

```java
// 1. Submit migration
MigrationJobResponse job = migrationService.submitMigration("CUST001");
String jobId = job.getJobId();

// 2. Poll for completion
while (true) {
    MigrationJobResponse status = migrationService.getJobStatus(jobId);
    
    if (status.getJobStatus() == MigrationJobStatus.COMPLETED) {
        log.info("Migration completed!");
        break;
    }
    
    if (status.getJobStatus() == MigrationJobStatus.FAILED) {
        log.error("Migration failed: {}", status.getErrorMessage());
        // Retry logic
        migrationService.submitRetry("CUST001");
        break;
    }
    
    log.info("Progress: {}%", status.getProgressPercentage());
    Thread.sleep(5000); // Wait 5 seconds
}
```

## Troubleshooting

### Job Stuck in ACCEPTED

- Check if async executor is running
- Check application logs for errors
- Verify thread pool configuration

### Stage Keeps Failing

- Check handler implementation
- Verify Oracle/PostgreSQL connectivity
- Review error messages in `errorMessage` field

### Migration Not Starting

- Verify customer has stages initialized
- Check if migration already completed
- Verify job was created successfully

## Database Migrations

Migration scripts are in `src/main/resources/db/migration/`:

- `V3__add_migration_stages.sql` - Creates tables and initial stages

## Related Files

- **Models**: `migration/model/`
- **Repositories**: `migration/repository/`
- **Services**: `migration/service/`
- **Handlers**: `migration/handler/`
- **Controllers**: `controller/MigrationController.java`
- **Config**: `config/AsyncConfig.java`

