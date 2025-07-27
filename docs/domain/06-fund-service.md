# Fund Service (재정 관리 서비스)

## 1. 서비스 개요

### 1.1 목적
Fund Service는 시그나이트 플랫폼의 모든 재정 관련 기능을 관리합니다. SIG 지원금 신청, 예산 관리, 영수증 처리, 재정 보고서 생성 등을 담당합니다.

### 1.2 주요 책임
- 지원금 신청 및 승인
- 예산 배정 및 관리
- 지출 내역 관리
- 영수증 처리 및 검증
- 재정 보고서 생성
- 회계 감사 추적

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
storage: AWS S3 (영수증 이미지)
ocr: AWS Textract (영수증 인식)
reporting: JasperReports
```

## 3. API 설계

### 3.1 지원금 신청 API

#### POST /api/v1/funds/applications
지원금 신청
```json
// Request
{
  "sigId": "sig-uuid",
  "applicationType": "REGULAR", // REGULAR, SPECIAL, EVENT
  "requestedAmount": 500000,
  "purpose": "11월 정기 모임 지원",
  "description": "장소 대여료 및 다과비",
  "plannedDate": "2024-11-15",
  "breakdown": [
    {
      "category": "VENUE",
      "description": "스터디카페 대여",
      "amount": 300000
    },
    {
      "category": "FOOD",
      "description": "다과 및 음료",
      "amount": 200000
    }
  ],
  "attachments": ["budget-plan.pdf"]
}

// Response
{
  "applicationId": "app-uuid",
  "applicationNumber": "FUND-2024-001",
  "status": "PENDING",
  "submittedAt": "2024-10-15T10:00:00Z"
}
```

#### GET /api/v1/funds/applications/{applicationId}
지원금 신청 상세 조회
```json
// Response
{
  "id": "app-uuid",
  "applicationNumber": "FUND-2024-001",
  "sig": {
    "id": "sig-uuid",
    "name": "AI 연구 모임"
  },
  "applicant": {
    "id": "user-uuid",
    "name": "홍길동",
    "role": "SIG_LEADER"
  },
  "applicationType": "REGULAR",
  "requestedAmount": 500000,
  "approvedAmount": 450000,
  "purpose": "11월 정기 모임 지원",
  "status": "APPROVED",
  "statusHistory": [
    {
      "status": "PENDING",
      "changedAt": "2024-10-15T10:00:00Z",
      "changedBy": "시스템"
    },
    {
      "status": "UNDER_REVIEW",
      "changedAt": "2024-10-16T09:00:00Z",
      "changedBy": "재무담당자"
    },
    {
      "status": "APPROVED",
      "changedAt": "2024-10-17T14:00:00Z",
      "changedBy": "재무이사",
      "comment": "다과비 일부 조정"
    }
  ]
}
```

### 3.2 예산 관리 API

#### GET /api/v1/funds/budgets/{sigId}
SIG 예산 현황 조회
```json
// Response
{
  "sigId": "sig-uuid",
  "fiscalYear": 2024,
  "budget": {
    "allocated": 5000000,
    "used": 3200000,
    "committed": 500000,
    "available": 1300000
  },
  "monthlyBreakdown": [
    {
      "month": "2024-10",
      "allocated": 416667,
      "used": 350000,
      "transactions": 5
    }
  ],
  "categoryBreakdown": [
    {
      "category": "VENUE",
      "budgeted": 2000000,
      "used": 1500000,
      "percentage": 75.0
    },
    {
      "category": "FOOD",
      "budgeted": 1500000,
      "used": 1000000,
      "percentage": 66.7
    }
  ]
}
```

#### POST /api/v1/funds/budgets/{sigId}/allocate
예산 배정 (관리자용)
```json
// Request
{
  "fiscalYear": 2024,
  "totalAmount": 6000000,
  "categoryAllocations": [
    {
      "category": "VENUE",
      "amount": 2400000
    },
    {
      "category": "FOOD",
      "amount": 1800000
    },
    {
      "category": "SUPPLIES",
      "amount": 1200000
    },
    {
      "category": "OTHER",
      "amount": 600000
    }
  ]
}
```

### 3.3 지출 관리 API

#### POST /api/v1/funds/expenses
지출 내역 등록
```json
// Request
{
  "sigId": "sig-uuid",
  "applicationId": "app-uuid", // 관련 지원금 신청
  "amount": 285000,
  "category": "VENUE",
  "description": "스터디카페 대여료",
  "expenseDate": "2024-11-15",
  "paymentMethod": "CARD", // CARD, TRANSFER, CASH
  "merchant": "강남스터디카페",
  "receipts": ["receipt-1.jpg", "receipt-2.jpg"]
}

// Response
{
  "expenseId": "expense-uuid",
  "status": "PENDING_VERIFICATION",
  "message": "영수증 검증 중입니다"
}
```

#### GET /api/v1/funds/expenses
지출 내역 목록 조회
```json
// Query Parameters
?sigId=sig-uuid&startDate=2024-10-01&endDate=2024-10-31&page=0&size=20

// Response
{
  "content": [
    {
      "id": "expense-uuid",
      "sig": {
        "name": "AI 연구 모임"
      },
      "amount": 285000,
      "category": "VENUE",
      "description": "스터디카페 대여료",
      "expenseDate": "2024-10-15",
      "status": "VERIFIED",
      "submittedBy": {
        "name": "홍길동"
      }
    }
  ],
  "totalAmount": 1250000,
  "totalElements": 15
}
```

### 3.4 영수증 처리 API

#### POST /api/v1/funds/receipts/upload
영수증 업로드
```
// Request
Content-Type: multipart/form-data
file: receipt.jpg

// Response
{
  "receiptId": "receipt-uuid",
  "fileName": "receipt.jpg",
  "fileSize": 2048576,
  "uploadedAt": "2024-10-15T10:00:00Z",
  "ocrStatus": "PROCESSING"
}
```

#### GET /api/v1/funds/receipts/{receiptId}/ocr
영수증 OCR 결과 조회
```json
// Response
{
  "receiptId": "receipt-uuid",
  "ocrStatus": "COMPLETED",
  "extractedData": {
    "merchant": "강남스터디카페",
    "businessNumber": "123-45-67890",
    "date": "2024-10-15",
    "time": "14:30",
    "totalAmount": 285000,
    "taxAmount": 25909,
    "items": [
      {
        "name": "스터디룸 A (4시간)",
        "quantity": 1,
        "unitPrice": 70000,
        "amount": 280000
      },
      {
        "name": "음료",
        "quantity": 1,
        "unitPrice": 5000,
        "amount": 5000
      }
    ],
    "paymentMethod": "신용카드",
    "cardNumber": "****-****-****-1234"
  },
  "confidence": 0.95
}
```

### 3.5 보고서 API

#### GET /api/v1/funds/reports/monthly
월별 재정 보고서
```json
// Query Parameters
?sigId=sig-uuid&year=2024&month=10

// Response
{
  "report": {
    "period": "2024-10",
    "sig": {
      "id": "sig-uuid",
      "name": "AI 연구 모임"
    },
    "summary": {
      "openingBalance": 2000000,
      "totalIncome": 500000,
      "totalExpense": 850000,
      "closingBalance": 1650000
    },
    "income": [
      {
        "date": "2024-10-05",
        "type": "FUND_SUPPORT",
        "description": "10월 정기 지원금",
        "amount": 500000
      }
    ],
    "expenses": [
      {
        "date": "2024-10-15",
        "category": "VENUE",
        "description": "스터디카페 대여",
        "amount": 285000,
        "hasReceipt": true
      }
    ],
    "categoryAnalysis": [
      {
        "category": "VENUE",
        "amount": 500000,
        "percentage": 58.8,
        "trend": "UP"
      }
    ]
  }
}
```

#### GET /api/v1/funds/reports/annual
연간 재정 보고서
```json
// Query Parameters
?sigId=sig-uuid&year=2024

// Response
{
  "report": {
    "fiscalYear": 2024,
    "sig": {
      "id": "sig-uuid",
      "name": "AI 연구 모임"
    },
    "summary": {
      "totalBudget": 5000000,
      "totalUsed": 3200000,
      "utilizationRate": 64.0,
      "averageMonthlyExpense": 320000
    },
    "quarterlyBreakdown": [
      {
        "quarter": "Q1",
        "income": 1250000,
        "expense": 800000,
        "balance": 450000
      }
    ],
    "topExpenses": [
      {
        "category": "VENUE",
        "totalAmount": 1500000,
        "transactionCount": 12,
        "averageAmount": 125000
      }
    ],
    "complianceStatus": {
      "receiptsSubmitted": 95.5,
      "onTimeReporting": 100.0,
      "budgetAdherence": "GOOD"
    }
  }
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### fund_applications
```sql
CREATE TABLE fund_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_number VARCHAR(50) UNIQUE NOT NULL,
    sig_id UUID NOT NULL,
    applicant_id UUID NOT NULL,
    application_type VARCHAR(20) NOT NULL,
    requested_amount DECIMAL(12,2) NOT NULL,
    approved_amount DECIMAL(12,2),
    purpose VARCHAR(200) NOT NULL,
    description TEXT,
    planned_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by UUID,
    approved_at TIMESTAMP,
    approved_by UUID,
    rejection_reason TEXT
);

CREATE INDEX idx_fund_applications_sig_id ON fund_applications(sig_id);
CREATE INDEX idx_fund_applications_status ON fund_applications(status);
CREATE INDEX idx_fund_applications_submitted_at ON fund_applications(submitted_at);
```

#### fund_application_items
```sql
CREATE TABLE fund_application_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id UUID REFERENCES fund_applications(id),
    category VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### sig_budgets
```sql
CREATE TABLE sig_budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID NOT NULL,
    fiscal_year INTEGER NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sig_id, fiscal_year)
);

CREATE INDEX idx_sig_budgets_sig_year ON sig_budgets(sig_id, fiscal_year);
```

#### budget_categories
```sql
CREATE TABLE budget_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_id UUID REFERENCES sig_budgets(id),
    category VARCHAR(50) NOT NULL,
    allocated_amount DECIMAL(12,2) NOT NULL,
    UNIQUE(budget_id, category)
);
```

#### expenses
```sql
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID NOT NULL,
    application_id UUID REFERENCES fund_applications(id),
    amount DECIMAL(12,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    expense_date DATE NOT NULL,
    payment_method VARCHAR(20),
    merchant VARCHAR(200),
    status VARCHAR(20) DEFAULT 'PENDING_VERIFICATION',
    submitted_by UUID NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by UUID
);

CREATE INDEX idx_expenses_sig_id ON expenses(sig_id);
CREATE INDEX idx_expenses_expense_date ON expenses(expense_date);
CREATE INDEX idx_expenses_status ON expenses(status);
```

#### receipts
```sql
CREATE TABLE receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_id UUID REFERENCES expenses(id),
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),
    ocr_status VARCHAR(20) DEFAULT 'PENDING',
    ocr_data JSONB,
    ocr_confidence DECIMAL(3,2),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE INDEX idx_receipts_expense_id ON receipts(expense_id);
```

#### fund_transactions
```sql
CREATE TABLE fund_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sig_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    reference_type VARCHAR(50), -- APPLICATION, EXPENSE
    reference_id UUID,
    description VARCHAR(500),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fund_transactions_sig_id ON fund_transactions(sig_id);
CREATE INDEX idx_fund_transactions_date ON fund_transactions(transaction_date);
```

## 5. 도메인 모델

### 5.1 신청 유형
```kotlin
enum class ApplicationType {
    REGULAR,    // 정기 지원
    SPECIAL,    // 특별 지원
    EVENT,      // 행사 지원
    EMERGENCY   // 긴급 지원
}
```

### 5.2 지출 카테고리
```kotlin
enum class ExpenseCategory {
    VENUE,          // 장소 대여
    FOOD,           // 식음료
    SUPPLIES,       // 물품 구매
    TRANSPORTATION, // 교통비
    PRINTING,       // 인쇄비
    SPEAKER_FEE,    // 강사료
    OTHER          // 기타
}
```

### 5.3 신청 상태
```kotlin
enum class ApplicationStatus {
    DRAFT,          // 초안
    PENDING,        // 제출됨
    UNDER_REVIEW,   // 검토중
    APPROVED,       // 승인됨
    REJECTED,       // 거절됨
    CANCELLED      // 취소됨
}
```

## 6. 영수증 OCR 처리

### 6.1 OCR 워크플로우
```kotlin
@Service
class ReceiptOCRService(
    private val textractClient: TextractClient,
    private val s3Service: S3Service
) {
    suspend fun processReceipt(receiptId: String, fileUrl: String) {
        // 1. S3에서 이미지 가져오기
        val imageBytes = s3Service.downloadFile(fileUrl)
        
        // 2. AWS Textract로 텍스트 추출
        val textractResult = textractClient.analyzeExpense(imageBytes)
        
        // 3. 추출된 데이터 파싱
        val extractedData = parseTextractResult(textractResult)
        
        // 4. 데이터 검증
        val validationResult = validateExtractedData(extractedData)
        
        // 5. 결과 저장
        receiptRepository.updateOCRResult(
            receiptId,
            extractedData,
            validationResult.confidence
        )
    }
    
    private fun validateExtractedData(data: ExtractedReceiptData): ValidationResult {
        val checks = listOf(
            validateBusinessNumber(data.businessNumber),
            validateAmount(data.totalAmount),
            validateDate(data.date)
        )
        
        val confidence = checks.count { it } / checks.size.toDouble()
        return ValidationResult(confidence > 0.8, confidence)
    }
}
```

### 6.2 영수증 검증 규칙
```kotlin
class ReceiptValidator {
    fun validate(receipt: Receipt, expense: Expense): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 금액 일치 확인
        if (abs(receipt.amount - expense.amount) > 100) {
            errors.add("영수증 금액이 신고 금액과 다릅니다")
        }
        
        // 날짜 확인
        if (receipt.date != expense.expenseDate) {
            errors.add("영수증 날짜가 지출 날짜와 다릅니다")
        }
        
        // 사업자번호 유효성
        if (!isValidBusinessNumber(receipt.businessNumber)) {
            errors.add("유효하지 않은 사업자번호입니다")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
```

## 7. 재정 보고서 생성

### 7.1 보고서 템플릿
```kotlin
@Service
class ReportGenerationService(
    private val jasperReports: JasperReports
) {
    fun generateMonthlyReport(sigId: String, year: Int, month: Int): ByteArray {
        // 데이터 수집
        val reportData = collectMonthlyData(sigId, year, month)
        
        // 템플릿 로드
        val template = loadTemplate("monthly_financial_report.jrxml")
        
        // 파라미터 설정
        val parameters = mapOf(
            "SIG_NAME" to reportData.sigName,
            "REPORT_PERIOD" to "$year-${month.toString().padStart(2, '0')}",
            "GENERATED_DATE" to LocalDate.now()
        )
        
        // PDF 생성
        return jasperReports.generatePdf(template, parameters, reportData)
    }
}
```

## 8. 예산 알림 시스템

### 8.1 예산 모니터링
```kotlin
@Component
class BudgetMonitor {
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    fun checkBudgetStatus() {
        val sigs = sigRepository.findAllActive()
        
        sigs.forEach { sig ->
            val budget = budgetService.getCurrentBudget(sig.id)
            val usage = budgetService.calculateUsage(sig.id)
            val percentage = (usage / budget.totalAmount) * 100
            
            when {
                percentage >= 90 -> {
                    notificationService.sendBudgetAlert(
                        sig.leaderId,
                        "예산의 90% 이상을 사용했습니다"
                    )
                }
                percentage >= 75 -> {
                    notificationService.sendBudgetWarning(
                        sig.leaderId,
                        "예산의 75%를 사용했습니다"
                    )
                }
            }
        }
    }
}
```

## 9. 감사 추적

### 9.1 감사 로그
```kotlin
@Aspect
@Component
class FundAuditAspect {
    @Around("@annotation(Auditable)")
    fun auditFundOperation(joinPoint: ProceedingJoinPoint): Any {
        val startTime = Instant.now()
        val user = SecurityContextHolder.getContext().authentication
        
        try {
            val result = joinPoint.proceed()
            
            auditLogRepository.save(
                AuditLog(
                    userId = user.principal as String,
                    action = joinPoint.signature.name,
                    entityType = "FUND",
                    entityId = extractEntityId(joinPoint),
                    changes = extractChanges(joinPoint),
                    timestamp = startTime,
                    duration = Duration.between(startTime, Instant.now())
                )
            )
            
            return result
        } catch (e: Exception) {
            auditLogRepository.save(
                AuditLog(
                    userId = user.principal as String,
                    action = joinPoint.signature.name,
                    error = e.message,
                    timestamp = startTime
                )
            )
            throw e
        }
    }
}
```

## 10. 통합 및 연동

### 10.1 외부 시스템 연동
```kotlin
// 은행 API 연동 (입출금 내역 자동 동기화)
interface BankingAPIClient {
    fun getTransactions(accountNumber: String, startDate: LocalDate, endDate: LocalDate): List<BankTransaction>
    fun verifyTransaction(transactionId: String): TransactionVerification
}

// 국세청 API 연동 (사업자번호 검증)
interface TaxAPIClient {
    fun verifyBusinessNumber(businessNumber: String): BusinessInfo
    fun getBusinessStatus(businessNumber: String): BusinessStatus
}
```

## 11. 모니터링

### 11.1 비즈니스 메트릭
- 월별 지원금 신청 건수
- 평균 처리 시간
- 승인률
- 예산 사용률
- 영수증 제출률

### 11.2 기술 메트릭
- API 응답 시간
- OCR 처리 성공률
- 파일 업로드 성능
- 보고서 생성 시간 