# Trat - 트랫. 졸라 빠른 번역기

## 개요

기존 번역 앱은 새로운 입력을 제공할 때마다 입력 필드를 비워야 하므로, 한 화면에서 번역 히스토리를 바로 확인하기 어려웠습니다.

또한 기능이 다양해질수록 UX가 복잡해져 사용에 불편함이 있었습니다.

Trat은 ML Kit을 활용한 **오프라인 실시간 번역 앱**입니다.

네트워크 연결 없이도 한국어, 영어, 일본어, 중국어 간 양방향 번역을 제공하며, 채팅 형태의 UI로 번역 히스토리를 한 화면에서 쉽게 조회할 수 있습니다.

채팅방은 로컬 DB에 저장되며, 검색 기능도 제공합니다!

무엇보다, 트랫은 빠르고 깔끔합니다 😜

## 핵심 기능

- **완전 오프라인 번역**: Google ML Kit 기반 로컬 번역 모델 사용
- **양방향 번역**: 설정된 두 언어 간 자동 방향 감지 및 번역
- **채팅 기반 UI**: 메시지 형태로 번역 히스토리 관리
- **다중 채팅방**: 언어별 번역방 생성 및 관리
- **고급 검색**: 번역 내용 전체 검색 및 하이라이트
- **성능 최적화**: 번역 캐싱, DB 인덱싱, Flow 최적화

## 기술 스택 및 아키텍처

### 아키텍처 패턴: Clean Architecture + MVVM

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Presentation  │    │     Domain      │    │      Data       │
│                 │    │                 │    │                 │
│  • UI (Compose) │◄──►│   • Use Cases   │◄──►│  • Repository   │
│  • ViewModels   │    │   • Entities    │    │  • Data Source  │
│  • Navigation   │    │   • Interfaces  │    │  • Database     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

**UI & Framework**
- **Jetpack Compose**
- **Material Design 3**

**Architecture Components**
- **Hilt**: 의존성 주입 (Dependency Injection)
- **Room**: 로컬 데이터베이스 (SQLite 기반)
- **ViewModel & LiveData**: MVVM 패턴
- **Navigation Component**: 화면 전환 관리

**번역 & AI**
- **ML Kit Translate**
- **언어 감지**: 자동 입력 언어 판별을 통해 유연한 언어 설정 가능

**성능 최적화**
- **Coroutines & Flow**: 비동기 처리 및 반응형 프로그래밍
- **DataStore**: 캐싱 시스템
- **DB 인덱싱**: 검색 최적화

## 프로젝트 구조

#### 1. Presentation Layer (UI 계층)
```
presentation/
├── components/          # 재사용 가능한 UI 컴포넌트
├── screens/            # 화면별 Composable
├── navigation/         # 앱 내 네비게이션 관리
└── viewmodels/         # MVVM의 ViewModel (UI 상태 관리)
```

**역할**: 사용자 인터페이스와 사용자 상호작용 처리
- **ViewModels**: UI 상태 관리, 비즈니스 로직 호출
- **Composables**: 선언형 UI
- **Navigation**: 화면 전환 로직

#### 2. Domain Layer (비즈니스 로직 계층)
```
domain/
├── usecase/           # 비즈니스 로직 구현체
└── repository/        # 데이터 접근 인터페이스
```

**역할**: 앱의 핵심 비즈니스 로직을 담당하는 순수한 코틀린 모듈
- **Use Cases**: 단일 책임 원칙에 따른 기능별 로직 분리
  - `ChatManagementUseCase`: 채팅방 CRUD
  - `TranslationUseCase`: 번역 로직
  - `LanguageDetectionUseCase`: 언어 감지
  - `MessageTranslationUseCase`: 메시지 번역 통합 처리

#### 3. Data Layer (데이터 계층)
```
data/
├── entities/          # Room 데이터베이스 엔티티
├── dao/              # 데이터 접근 객체
├── database/         # 데이터베이스 설정
├── repository/       # Repository 패턴 구현
└── models/           # 데이터 모델
```

**역할**: 데이터 저장, 조회, 관리
- **Repository Pattern**: 데이터 소스 추상화
- **Room Database**: 로컬 데이터 영속성
- **DAO**: 타입 안전한 데이터베이스 접근

### 의존성 주입 (DI)
```
di/
├── DatabaseModule.kt    # 데이터베이스 의존성
├── RepositoryModule.kt  # Repository 의존성
└── UseCaseModule.kt     # UseCase 의존성
```

**Hilt**를 사용한 의존성 주입으로 객체 생성과 생명주기를 자동 관리합니다.

## 아키텍처의 장점

### 1. 관심사의 분리 (Separation of Concerns)
- **UI 로직**: Presentation Layer에서만 처리
- **비즈니스 로직**: Domain Layer에서 순수하게 처리
- **데이터 처리**: Data Layer에서 독립적으로 처리

### 2. 테스트 용이성
- 각 계층이 독립적이어서 단위 테스트 작성이 쉬움
- Use Case는 순수 함수로 구현되어 테스트하기 간단함

### 3. 유지보수성
- 각 파일이 단일 책임을 가져 코드 변경 시 영향 범위가 제한됨
- 새로운 기능 추가 시 기존 코드 수정 최소화

### 4. 확장성
- 새로운 언어 추가, 번역 엔진 변경 등이 용이함
- 모듈별 독립 개발 가능

### 5. 성능 최적화
- **번역 캐싱**: 중복 번역 방지로 응답 시간 단축
- **DB 인덱싱**: 검색 성능 대폭 향상
- **Flow 최적화**: 메모리 효율적인 반응형 프로그래밍

## 주요 패턴 및 원칙

### Design Patterns
- **Repository Pattern**: 데이터 소스 추상화
- **Observer Pattern**: Flow를 통한 반응형 프로그래밍
- **Strategy Pattern**: 언어별 번역 전략
- **Factory Pattern**: Use Case 생성

### SOLID 원칙 적용
- **SRP**: 각 클래스와 함수가 단일 책임을 가짐
- **OCP**: 인터페이스를 통한 확장 가능한 구조
- **DIP**: 추상화에 의존하는 의존성 역전

### Clean Code 원칙
- 의미있는 네이밍
- 작은 함수와 클래스
- 중복 코드 제거
- 일관된 코딩 스타일

## 성능 최적화 전략

### 1. 번역 캐싱 시스템
```kotlin
// LRU 캐시를 활용한 메모리 + 디스크 캐싱
class TranslationCacheService {
    private val memoryCache = LruCache<String, String>(100)
    private val diskCache = DataStore // 영구 저장
}
```

### 2. 데이터베이스 최적화
```kotlin
// 검색 성능을 위한 인덱스 추가
@Entity(
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["originalText"]),
        Index(value = ["translatedText"])
    ]
)
```

### 3. 비동기 처리 최적화
```kotlin
// Flow를 활용한 반응형 데이터 스트림
class ChatViewModel : BaseViewModel() {
    val messages = chatRepository.getMessagesFlow(chatId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
```

## 개발 환경 및 빌드

### 요구사항
- **Android Studio**: Flamingo 이상
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.0
- **Compose**: 2024.02.00

### 빌드 명령어
```bash
# 디버그 빌드
./gradlew assembleDebug

# 릴리즈 빌드
./gradlew assembleRelease

# 테스트 실행
./gradlew test
```

## 학습

### 1. 아키텍처 패턴
- Clean Architecture의 실제 적용법
- MVVM 패턴의 올바른 구현
- 의존성 주입의 활용

### 2. Jetpack Components
- Compose를 활용한 선언형 UI
- Room을 통한 로컬 데이터베이스 관리
- Navigation Component로 화면 전환
- ViewModel과 LiveData로 상태 관리

### 3. 현대적 프로그래밍 기법
- Coroutines와 Flow를 통한 비동기 처리
- 함수형 프로그래밍 원칙 적용
- 반응형 프로그래밍 구현

### 4. 성능 최적화
- 캐싱 전략 수립
- 데이터베이스 최적화
- 메모리 관리

### 5. AI/ML 통합
- 온디바이스 ML 모델 활용
- 언어 처리 및 번역 시스템

## 향후 확장 가능성

이 아키텍처는 다음과 같은 기능 확장이 용이합니다:

- **새로운 번역 엔진 추가** (OpenAI, DeepL 등)
- **음성 번역 기능** (Speech-to-Text + TTS)
- **이미지 번역** (OCR + Translation)
- **실시간 대화 번역** (WebSocket 활용)
- **클라우드 동기화** (Firebase, AWS 등)

Trat은 확장 가능하고 유지보수하기 쉬운 코드베이스를 구축했습니다. 
