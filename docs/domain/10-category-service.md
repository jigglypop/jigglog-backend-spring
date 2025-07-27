# Category Service (카테고리 서비스)

## 1. 서비스 개요

### 1.1 목적
Category Service는 시그나이트 플랫폼의 모든 계층적 카테고리를 관리합니다. 무제한 깊이의 중첩 카테고리, 동적 재구성, 권한 기반 접근 제어 등 복잡한 카테고리 시스템을 지원합니다.

### 1.2 주요 책임
- 계층적 카테고리 구조 관리
- 카테고리 생성/수정/삭제/이동
- 권한 기반 카테고리 접근 제어
- 카테고리별 메타데이터 관리
- 카테고리 트리 최적화
- 카테고리 검색 및 필터링

## 2. 카테고리 시스템 설계

### 2.1 카테고리 계층 구조
```
시그나이트 카테고리 시스템
├── 전체 (Root)
├── SIG별 카테고리
│   ├── AI 연구 모임
│   │   ├── 일반 게시판
│   │   ├── 공지사항
│   │   ├── 질문답변
│   │   ├── 스터디 자료
│   │   │   ├── 기초 자료
│   │   │   ├── 심화 자료
│   │   │   └── 프로젝트
│   │   │       ├── 진행중
│   │   │       └── 완료됨
│   │   └── 자유게시판
│   └── 독서 모임
│       ├── 도서 추천
│       │   ├── 소설
│       │   ├── 비소설
│       │   └── 학술서
│       ├── 독후감
│       └── 모임 후기
├── 관리자 전용
│   ├── 운영진 게시판
│   ├── 재정 관리
│   └── 시스템 공지
└── 기타
    ├── 자유게시판
    ├── 건의사항
    └── FAQ
```

### 2.2 카테고리 유형
- **SIG 카테고리**: SIG별 독립적인 카테고리 트리
- **전역 카테고리**: 모든 사용자가 접근 가능
- **관리자 카테고리**: 관리자만 접근 가능
- **임시 카테고리**: 특정 기간 동안만 활성화
- **동적 카테고리**: 조건에 따라 자동 생성/삭제

## 3. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL (기본), Neo4j (그래프 관계)
cache: Redis
search: Elasticsearch
```

## 4. API 설계

### 4.1 카테고리 트리 API

#### GET /api/v1/categories/tree
카테고리 트리 조회
```json
// Query Parameters
?rootId=cat-uuid&depth=3&includeInactive=false&sigId=sig-uuid

// Response
{
  "tree": {
    "id": "cat-root",
    "name": "전체",
    "slug": "root",
    "type": "ROOT",
    "level": 0,
    "isActive": true,
    "permissions": {
      "canView": true,
      "canPost": false,
      "canManage": false
    },
    "metadata": {
      "postCount": 1250,
      "lastActivityAt": "2024-11-15T10:30:00Z",
      "description": "전체 카테고리 루트"
    },
    "children": [
      {
        "id": "cat-sig-1",
        "name": "AI 연구 모임",
        "slug": "ai-research",
        "type": "SIG_CATEGORY",
        "level": 1,
        "parentId": "cat-root",
        "sigId": "sig-uuid",
        "displayOrder": 1,
        "permissions": {
          "canView": true,
          "canPost": true,
          "canManage": false
        },
        "metadata": {
          "postCount": 89,
          "memberCount": 25,
          "icon": "🤖",
          "color": "#FF5733"
        },
        "children": [
          {
            "id": "cat-study-materials",
            "name": "스터디 자료",
            "slug": "study-materials",
            "type": "CONTENT_CATEGORY",
            "level": 2,
            "displayOrder": 4,
            "settings": {
              "allowedFileTypes": ["pdf", "doc", "ppt"],
              "maxFileSize": 52428800,
              "requireApproval": true,
              "autoArchive": {
                "enabled": true,
                "afterDays": 365
              }
            },
            "children": [
              {
                "id": "cat-basic",
                "name": "기초 자료",
                "level": 3,
                "metadata": {
                  "postCount": 15
                }
              },
              {
                "id": "cat-advanced",
                "name": "심화 자료", 
                "level": 3,
                "metadata": {
                  "postCount": 8
                }
              }
            ]
          }
        ]
      }
    ]
  },
  "totalCategories": 45,
  "maxDepth": 5,
  "userPermissions": {
    "canCreateCategory": true,
    "canManageTree": false
  }
}
```

#### GET /api/v1/categories/{categoryId}/path
카테고리 경로 조회
```json
// Response
{
  "path": [
    {
      "id": "cat-root",
      "name": "전체",
      "slug": "root",
      "level": 0
    },
    {
      "id": "cat-sig-1", 
      "name": "AI 연구 모임",
      "slug": "ai-research",
      "level": 1
    },
    {
      "id": "cat-study-materials",
      "name": "스터디 자료",
      "slug": "study-materials", 
      "level": 2
    }
  ],
  "breadcrumb": "전체 > AI 연구 모임 > 스터디 자료"
}
```

### 4.2 카테고리 관리 API

#### POST /api/v1/categories
카테고리 생성
```json
// Request
{
  "name": "프로젝트 발표",
  "slug": "project-presentations", // 선택적, 자동 생성 가능
  "description": "완료된 프로젝트 발표 자료",
  "type": "CONTENT_CATEGORY",
  "parentId": "cat-study-materials",
  "sigId": "sig-uuid", // SIG 카테고리인 경우
  "displayOrder": 3,
  "settings": {
    "isActive": true,
    "allowPosts": true,
    "allowSubcategories": true,
    "requireApproval": false,
    "allowedRoles": ["SIG_MEMBER", "SIG_LEADER"],
    "autoArchive": {
      "enabled": true,
      "afterDays": 180
    },
    "notifications": {
      "onNewPost": true,
      "notifyRoles": ["SIG_LEADER"]
    }
  },
  "metadata": {
    "icon": "📊",
    "color": "#4CAF50",
    "template": "project-category"
  }
}

// Response
{
  "id": "cat-project-presentations",
  "name": "프로젝트 발표",
  "slug": "project-presentations",
  "fullPath": "ai-research/study-materials/project-presentations",
  "level": 3,
  "createdAt": "2024-11-15T10:00:00Z"
}
```

#### PUT /api/v1/categories/{categoryId}
카테고리 수정
```json
// Request
{
  "name": "프로젝트 발표 자료실",
  "description": "완료된 프로젝트 발표 자료 및 결과물",
  "settings": {
    "allowedFileTypes": ["pdf", "ppt", "zip"],
    "maxFileSize": 104857600
  },
  "metadata": {
    "icon": "🎯"
  }
}

// Response
{
  "success": true,
  "updatedAt": "2024-11-15T10:30:00Z",
  "changes": ["name", "description", "settings.allowedFileTypes", "metadata.icon"]
}
```

#### POST /api/v1/categories/{categoryId}/move
카테고리 이동
```json
// Request
{
  "newParentId": "cat-projects",
  "newPosition": 2
}

// Response
{
  "success": true,
  "oldPath": "ai-research/study-materials/project-presentations",
  "newPath": "ai-research/projects/project-presentations",
  "affectedCategories": [
    {
      "id": "cat-project-presentations",
      "oldLevel": 3,
      "newLevel": 3
    }
  ]
}
```

#### DELETE /api/v1/categories/{categoryId}
카테고리 삭제
```json
// Query Parameters
?movePostsTo=cat-general&deleteSubcategories=false

// Response
{
  "success": true,
  "deletedCategories": ["cat-project-presentations"],
  "movedPosts": 15,
  "deletedPosts": 0,
  "affectedSubcategories": []
}
```

### 4.3 카테고리 검색 API

#### GET /api/v1/categories/search
카테고리 검색
```json
// Query Parameters
?query=스터디&type=CONTENT_CATEGORY&sigId=sig-uuid&hasAccess=true

// Response
{
  "categories": [
    {
      "id": "cat-study-materials",
      "name": "스터디 자료",
      "path": "AI 연구 모임 > 스터디 자료",
      "type": "CONTENT_CATEGORY",
      "sigName": "AI 연구 모임",
      "postCount": 23,
      "relevanceScore": 0.95
    }
  ],
  "totalCount": 3,
  "searchTime": 15
}
```

### 4.4 카테고리 권한 API

#### GET /api/v1/categories/{categoryId}/permissions
카테고리 권한 조회
```json
// Response
{
  "categoryId": "cat-study-materials",
  "userPermissions": {
    "canView": true,
    "canPost": true,
    "canComment": true,
    "canUploadFile": true,
    "canCreateSubcategory": false,
    "canManage": false,
    "canDelete": false
  },
  "rolePermissions": [
    {
      "role": "SIG_MEMBER",
      "permissions": {
        "canView": true,
        "canPost": true,
        "canComment": true
      }
    },
    {
      "role": "SIG_LEADER", 
      "permissions": {
        "canView": true,
        "canPost": true,
        "canComment": true,
        "canManage": true,
        "canCreateSubcategory": true
      }
    }
  ],
  "inheritedFrom": "cat-sig-1"
}
```

#### PUT /api/v1/categories/{categoryId}/permissions
카테고리 권한 설정
```json
// Request
{
  "rolePermissions": [
    {
      "role": "SIG_MEMBER",
      "permissions": {
        "canPost": false,
        "canComment": true
      }
    }
  ],
  "inheritFromParent": false
}

// Response
{
  "success": true,
  "updatedPermissions": 2
}
```

### 4.5 카테고리 통계 API

#### GET /api/v1/categories/{categoryId}/statistics
카테고리 통계
```json
// Query Parameters
?period=MONTH&includeSubcategories=true

// Response
{
  "categoryId": "cat-study-materials",
  "period": {
    "start": "2024-10-01T00:00:00Z",
    "end": "2024-10-31T23:59:59Z"
  },
  "statistics": {
    "postCount": 45,
    "commentCount": 123,
    "viewCount": 1250,
    "uniqueViewers": 89,
    "activeUsers": 34,
    "fileUploads": 23,
    "totalFileSize": 157286400,
    "subcategoryStats": [
      {
        "categoryId": "cat-basic",
        "name": "기초 자료",
        "postCount": 25,
        "viewCount": 750
      }
    ]
  },
  "trends": {
    "postCountChange": "+15%",
    "viewCountChange": "+8%",
    "popularTimes": [
      {
        "hour": 14,
        "count": 45
      },
      {
        "hour": 20,
        "count": 38
      }
    ]
  }
}
```

## 5. 데이터베이스 설계

### 5.1 PostgreSQL 테이블

#### categories
```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    sig_id UUID, -- SIG 카테고리인 경우
    parent_id UUID REFERENCES categories(id),
    level INTEGER NOT NULL DEFAULT 0,
    path LTREE, -- 계층 경로를 위한 PostgreSQL LTREE
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    settings JSONB,
    metadata JSONB,
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_sig_id ON categories(sig_id);
CREATE INDEX idx_categories_path ON categories USING GIST(path);
CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_categories_level ON categories(level);
CREATE UNIQUE INDEX idx_categories_slug_parent ON categories(slug, parent_id) WHERE deleted_at IS NULL;

-- 트리거: path 자동 업데이트
CREATE OR REPLACE FUNCTION update_category_path()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.parent_id IS NULL THEN
        NEW.path = NEW.id::TEXT::LTREE;
        NEW.level = 0;
    ELSE
        SELECT path || NEW.id::TEXT::LTREE, level + 1
        INTO NEW.path, NEW.level
        FROM categories 
        WHERE id = NEW.parent_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_category_path
    BEFORE INSERT OR UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_category_path();
```

#### category_permissions
```sql
CREATE TABLE category_permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES categories(id),
    role VARCHAR(50) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    granted BOOLEAN DEFAULT true,
    inherit_from_parent BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permissions_category_role ON category_permissions(category_id, role);
CREATE UNIQUE INDEX idx_permissions_unique ON category_permissions(category_id, role, permission);
```

#### category_statistics
```sql
CREATE TABLE category_statistics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES categories(id),
    stat_date DATE NOT NULL,
    post_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    unique_viewers INTEGER DEFAULT 0,
    file_uploads INTEGER DEFAULT 0,
    total_file_size BIGINT DEFAULT 0,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(category_id, stat_date)
);

CREATE INDEX idx_stats_category_date ON category_statistics(category_id, stat_date);
```

#### category_subscriptions
```sql
CREATE TABLE category_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    category_id UUID REFERENCES categories(id),
    notification_type VARCHAR(50) NOT NULL, -- NEW_POST, NEW_COMMENT, ADMIN_NOTICE
    is_active BOOLEAN DEFAULT true,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, category_id, notification_type)
);

CREATE INDEX idx_subscriptions_user ON category_subscriptions(user_id);
CREATE INDEX idx_subscriptions_category ON category_subscriptions(category_id);
```

### 5.2 Neo4j 그래프 (선택적 - 복잡한 관계 분석용)

#### 노드 및 관계
```cypher
// 카테고리 노드
CREATE (c:Category {
    id: 'cat-study-materials',
    name: '스터디 자료',
    type: 'CONTENT_CATEGORY',
    level: 2
})

// 계층 관계
(parent:Category)-[:PARENT_OF]->(child:Category)

// 사용자 접근 관계
(user:User)-[:CAN_ACCESS]->(category:Category)

// SIG 소속 관계
(sig:SIG)-[:OWNS]->(category:Category)

// 쿼리 예시: 특정 깊이의 하위 카테고리 조회
MATCH (root:Category {id: 'cat-root'})-[:PARENT_OF*1..3]->(sub:Category)
WHERE sub.isActive = true
RETURN sub
```

## 6. 도메인 모델

### 6.1 카테고리 유형
```kotlin
enum class CategoryType {
    ROOT,              // 루트 카테고리
    SIG_CATEGORY,      // SIG 전용 카테고리
    CONTENT_CATEGORY,  // 컨텐츠 카테고리
    ADMIN_CATEGORY,    // 관리자 전용
    TEMP_CATEGORY,     // 임시 카테고리
    DYNAMIC_CATEGORY   // 동적 카테고리
}
```

### 6.2 권한 유형
```kotlin
enum class CategoryPermission {
    CAN_VIEW,              // 조회 권한
    CAN_POST,              // 게시 권한
    CAN_COMMENT,           // 댓글 권한
    CAN_UPLOAD_FILE,       // 파일 업로드 권한
    CAN_CREATE_SUBCATEGORY,// 하위 카테고리 생성
    CAN_MANAGE,            // 관리 권한
    CAN_DELETE             // 삭제 권한
}
```

### 6.3 카테고리 엔티티
```kotlin
@Entity
@Table(name = "categories")
data class Category(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    var name: String,
    
    @Column(nullable = false)
    var slug: String,
    
    var description: String? = null,
    
    @Enumerated(EnumType.STRING)
    val type: CategoryType,
    
    val sigId: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: Category? = null,
    
    @OneToMany(mappedBy = "parent", cascade = [CascadeType.ALL])
    val children: MutableList<Category> = mutableListOf(),
    
    val level: Int = 0,
    
    @Column(columnDefinition = "ltree")
    val path: String? = null,
    
    var displayOrder: Int = 0,
    
    var isActive: Boolean = true,
    
    @Type(JsonType::class)
    val settings: CategorySettings = CategorySettings(),
    
    @Type(JsonType::class)
    val metadata: CategoryMetadata = CategoryMetadata(),
    
    val createdBy: UUID,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    var deletedAt: LocalDateTime? = null
) {
    fun getFullPath(): String {
        return generateSequence(this) { it.parent }
            .map { it.name }
            .toList()
            .reversed()
            .joinToString(" > ")
    }
    
    fun getAllDescendants(): List<Category> {
        return children.flatMap { listOf(it) + it.getAllDescendants() }
    }
    
    fun isDescendantOf(ancestor: Category): Boolean {
        return path?.startsWith(ancestor.path ?: "") == true
    }
}
```

## 7. 계층 관리 서비스

### 7.1 카테고리 트리 서비스
```kotlin
@Service
class CategoryTreeService(
    private val categoryRepository: CategoryRepository,
    private val permissionService: CategoryPermissionService
) {
    
    fun getCategoryTree(
        rootId: UUID?,
        userId: UUID,
        maxDepth: Int = 10,
        includeInactive: Boolean = false
    ): CategoryTreeNode {
        
        val root = rootId?.let { categoryRepository.findById(it) } 
            ?: categoryRepository.findRootCategory()
            
        return buildTreeNode(root, userId, 0, maxDepth, includeInactive)
    }
    
    private fun buildTreeNode(
        category: Category,
        userId: UUID,
        currentDepth: Int,
        maxDepth: Int,
        includeInactive: Boolean
    ): CategoryTreeNode {
        
        val permissions = permissionService.getUserPermissions(category.id, userId)
        
        if (!permissions.canView) {
            throw CategoryAccessDeniedException(category.id)
        }
        
        val children = if (currentDepth < maxDepth) {
            categoryRepository.findByParentIdAndActiveStatus(
                category.id, 
                includeInactive
            ).map { child ->
                buildTreeNode(child, userId, currentDepth + 1, maxDepth, includeInactive)
            }
        } else {
            emptyList()
        }
        
        return CategoryTreeNode(
            category = category,
            permissions = permissions,
            children = children,
            postCount = getPostCount(category.id),
            lastActivityAt = getLastActivityAt(category.id)
        )
    }
    
    fun moveCategory(
        categoryId: UUID,
        newParentId: UUID?,
        newPosition: Int,
        userId: UUID
    ): CategoryMoveResult {
        
        val category = categoryRepository.findById(categoryId)
        val newParent = newParentId?.let { categoryRepository.findById(it) }
        
        // 권한 검사
        if (!permissionService.canManage(categoryId, userId)) {
            throw InsufficientPermissionException()
        }
        
        // 순환 참조 검사
        if (newParent?.isDescendantOf(category) == true) {
            throw CircularReferenceException()
        }
        
        val oldPath = category.getFullPath()
        val affectedCategories = mutableListOf<Category>()
        
        // 이동 실행
        category.parent = newParent
        category.displayOrder = newPosition
        
        // 하위 카테고리들의 경로도 업데이트
        updateDescendantPaths(category)
        
        categoryRepository.save(category)
        
        return CategoryMoveResult(
            success = true,
            oldPath = oldPath,
            newPath = category.getFullPath(),
            affectedCategories = affectedCategories
        )
    }
}
```

### 7.2 권한 관리 서비스
```kotlin
@Service
class CategoryPermissionService(
    private val permissionRepository: CategoryPermissionRepository,
    private val userService: UserService
) {
    
    fun getUserPermissions(categoryId: UUID, userId: UUID): CategoryPermissions {
        val userRoles = userService.getUserRoles(userId)
        val category = categoryRepository.findById(categoryId)
        
        return calculatePermissions(category, userRoles)
    }
    
    private fun calculatePermissions(
        category: Category,
        userRoles: List<String>
    ): CategoryPermissions {
        
        val permissions = mutableMapOf<CategoryPermission, Boolean>()
        
        // 기본 권한 설정
        permissions[CategoryPermission.CAN_VIEW] = false
        permissions[CategoryPermission.CAN_POST] = false
        permissions[CategoryPermission.CAN_COMMENT] = false
        
        // 상위 카테고리부터 권한 상속
        val categoryPath = getCategoryPath(category)
        
        for (pathCategory in categoryPath.reversed()) {
            val categoryPermissions = permissionRepository
                .findByCategoryIdAndRoleIn(pathCategory.id, userRoles)
                
            for (permission in categoryPermissions) {
                if (!permission.inheritFromParent || 
                    !permissions.containsKey(permission.permission)) {
                    permissions[permission.permission] = permission.granted
                }
            }
        }
        
        // SIG 멤버십 기반 권한
        if (category.sigId != null) {
            val sigMembership = sigService.getMembership(category.sigId, userId)
            if (sigMembership != null) {
                permissions[CategoryPermission.CAN_VIEW] = true
                
                if (sigMembership.role in listOf("MEMBER", "LEADER")) {
                    permissions[CategoryPermission.CAN_POST] = true
                    permissions[CategoryPermission.CAN_COMMENT] = true
                }
                
                if (sigMembership.role == "LEADER") {
                    permissions[CategoryPermission.CAN_MANAGE] = true
                    permissions[CategoryPermission.CAN_CREATE_SUBCATEGORY] = true
                }
            }
        }
        
        return CategoryPermissions(permissions)
    }
}
```

## 8. 성능 최적화

### 8.1 LTREE를 활용한 계층 쿼리
```sql
-- 특정 카테고리의 모든 하위 카테고리 조회
SELECT * FROM categories 
WHERE path ~ 'cat-study-materials.*'
AND level <= 3;

-- 특정 카테고리의 직접 하위 카테고리만 조회
SELECT * FROM categories
WHERE parent_id = 'cat-study-materials';

-- 카테고리 경로 조회
SELECT * FROM categories
WHERE 'cat-project-presentations' ~ (path::text || '.*');

-- 계층 구조 통계
SELECT 
    level,
    COUNT(*) as category_count,
    AVG(ARRAY_LENGTH(STRING_TO_ARRAY(path::text, '.'), 1)) as avg_depth
FROM categories
GROUP BY level;
```

### 8.2 캐싱 전략
```kotlin
@Service
class CategoryCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    fun getCachedCategoryTree(rootId: UUID, userId: UUID): CategoryTreeNode? {
        val cacheKey = "category:tree:$rootId:$userId"
        return redisTemplate.opsForValue().get(cacheKey) as? CategoryTreeNode
    }
    
    fun cacheCategoryTree(
        rootId: UUID, 
        userId: UUID, 
        tree: CategoryTreeNode,
        ttl: Duration = Duration.ofMinutes(15)
    ) {
        val cacheKey = "category:tree:$rootId:$userId"
        redisTemplate.opsForValue().set(cacheKey, tree, ttl)
    }
    
    fun invalidateCategoryCache(categoryId: UUID) {
        // 해당 카테고리와 관련된 모든 캐시 무효화
        val pattern = "category:*:*$categoryId*"
        val keys = redisTemplate.keys(pattern)
        if (keys.isNotEmpty()) {
            redisTemplate.delete(keys)
        }
    }
}
```

## 9. 동적 카테고리 관리

### 9.1 자동 카테고리 생성
```kotlin
@Service
class DynamicCategoryService {
    
    @EventListener
    fun handleSigCreated(event: SigCreatedEvent) {
        // 새 SIG를 위한 기본 카테고리 구조 생성
        val sigRootCategory = createCategory(
            name = event.sigName,
            type = CategoryType.SIG_CATEGORY,
            parentId = getSigRootCategoryId(),
            sigId = event.sigId
        )
        
        // 기본 하위 카테고리들 생성
        val defaultCategories = listOf(
            "일반 게시판" to "general",
            "공지사항" to "announcements", 
            "질문답변" to "qna",
            "자료실" to "resources"
        )
        
        defaultCategories.forEachIndexed { index, (name, slug) ->
            createCategory(
                name = name,
                slug = slug,
                type = CategoryType.CONTENT_CATEGORY,
                parentId = sigRootCategory.id,
                sigId = event.sigId,
                displayOrder = index
            )
        }
    }
    
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    fun processTemporaryCategories() {
        val expiredCategories = categoryRepository
            .findTemporaryCategoriesExpiredBefore(LocalDateTime.now())
            
        expiredCategories.forEach { category ->
            archiveCategory(category)
        }
    }
}
```

## 10. 검색 및 분석

### 10.1 Elasticsearch 인덱싱
```kotlin
@Service
class CategorySearchService(
    private val elasticsearchClient: ElasticsearchClient
) {
    
    fun indexCategory(category: Category) {
        val document = CategoryDocument(
            id = category.id.toString(),
            name = category.name,
            description = category.description,
            path = category.getFullPath(),
            type = category.type.name,
            level = category.level,
            sigId = category.sigId?.toString(),
            tags = extractTags(category),
            postCount = getPostCount(category.id),
            lastActivityAt = getLastActivityAt(category.id)
        )
        
        elasticsearchClient.index(
            IndexRequest.of { i ->
                i.index("categories")
                    .id(category.id.toString())
                    .document(document)
            }
        )
    }
    
    fun searchCategories(query: CategorySearchQuery): CategorySearchResult {
        val searchRequest = SearchRequest.of { s ->
            s.index("categories")
                .query { q ->
                    q.bool { b ->
                        // 텍스트 검색
                        if (query.keyword.isNotEmpty()) {
                            b.must { m ->
                                m.multiMatch { mm ->
                                    mm.query(query.keyword)
                                        .fields("name^3", "description^2", "path")
                                        .fuzziness("AUTO")
                                }
                            }
                        }
                        
                        // 필터
                        query.type?.let { type ->
                            b.filter { f -> f.term { t -> t.field("type").value(type) } }
                        }
                        
                        query.sigId?.let { sigId ->
                            b.filter { f -> f.term { t -> t.field("sigId").value(sigId) } }
                        }
                        
                        query.minLevel?.let { minLevel ->
                            b.filter { f -> f.range { r -> r.field("level").gte(JsonData.of(minLevel)) } }
                        }
                        
                        query.maxLevel?.let { maxLevel ->
                            b.filter { f -> f.range { r -> r.field("level").lte(JsonData.of(maxLevel)) } }
                        }
                    }
                }
                .sort { sort ->
                    sort.field { f -> f.field("_score").order(SortOrder.Desc) }
                        .field { f -> f.field("level").order(SortOrder.Asc) }
                        .field { f -> f.field("name.keyword").order(SortOrder.Asc) }
                }
                .from(query.page * query.size)
                .size(query.size)
        }
        
        return elasticsearchClient.search(searchRequest, CategoryDocument::class.java)
            .let { response ->
                CategorySearchResult(
                    categories = response.hits().hits().map { it.source() },
                    totalCount = response.hits().total()?.value() ?: 0,
                    searchTime = response.took()
                )
            }
    }
}
```

## 11. 모니터링

### 11.1 비즈니스 메트릭
- 카테고리별 게시글 수
- 계층 깊이 분포
- 사용자별 카테고리 접근 패턴
- 인기 카테고리 순위
- 카테고리 생성/삭제 빈도

### 11.2 기술 메트릭
- 트리 조회 성능
- 권한 계산 시간
- 캐시 히트율
- 검색 쿼리 성능
- 데이터베이스 쿼리 최적화 