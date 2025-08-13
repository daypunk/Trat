# Trat
<img width="533" height="312" alt="Image" src="https://github.com/user-attachments/assets/a24d137e-ec8b-44f5-8ecc-aacf567e04e5" />

## 개요

**트랫**은 ML Kit을 활용한 **채팅형 오프라인 번역 앱**입니다.

기존 번역 앱은 새로운 입력을 제공할 때마다 입력 필드를 비워야 하므로, 한 화면에서 번역 히스토리를 바로 확인하기 어려웠습니다.

또한 기능이 다양해질수록 UX가 복잡해져 사용에 불편함이 있었습니다.

**트랫**에서는 네트워크 연결 없이도 한국어, 영어, 일본어, 중국어 간 양방향 번역을 제공하며, 채팅 형태의 UI로 번역 히스토리를 한 화면에서 쉽게 조회할 수 있습니다.

채팅방은 로컬 DB에 저장되며, 검색 기능도 제공합니다!

무엇보다, **트랫**은 가볍고 빠르게 동작합니다.

## 핵심 기능

- **완전 오프라인 번역**: Google ML Kit 기반 로컬 번역 모델 사용
- **양방향 번역**: 설정된 두 언어 간 자동 방향 감지 및 번역
- **음성 입력**: STT 기반 음성 번역 (한국어, 영어, 일본어, 중국어)
- **음성 출력**: TTS 기반 번역 결과 음성 재생
- **채팅 기반 UI**: 메시지 형태로 번역 히스토리 관리
- **다중 채팅방**: 언어별 번역방 생성 및 관리
- **고급 검색**: 번역 내용 전체 검색 및 하이라이트
- **성능 최적화**: 번역 캐싱, DB 인덱싱, Flow 최적화


## Clean Architecture + MVVM

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
- **ViewModel & StateFlow**: MVVM 패턴
- **Navigation for Compose**: 화면 전환 관리

**번역 & AI**
- **ML Kit Translate**: 오프라인 텍스트 번역
- **Android Speech Recognizer**: 실시간 음성 인식 (STT)
- **Android TextToSpeech**: 번역 결과 음성 출력 (TTS)
- **언어 감지**: 자동 입력 언어 판별을 통해 유연한 언어 설정 가능

**성능 최적화**
- **Coroutines & Flow**: 비동기 처리 및 반응형 프로그래밍
- **캐싱 레이어**: 자주 조회되는 데이터 최소화
- **DB 인덱싱**: 검색 최적화

## 프로젝트 구조

```
app/src/main/java/com/example/trat/
├── data/
│   ├── converters/
│   ├── dao/
│   ├── database/
│   ├── entities/
│   ├── models/
│   └── repository/
├── di/
├── domain/
│   ├── repository/
│   ├── service/
│   └── usecase/
├── presentation/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   └── viewmodels/
├── services/
├── ui/
│   └── theme/
├── utils/
├── MainActivity.kt
└── TratApplication.kt
```

### 계층 역할

#### Presentation (UI)
- 선언형 UI(Compose), 화면 전환, 상태 관리(ViewModel/StateFlow)

#### Domain (비즈니스 로직)
- 순수 Kotlin 모듈 중심
- Use Cases 예시: `ChatManagementUseCase`, `TranslationUseCase`, `LanguageDetectionUseCase`, `SpeechToTextUseCase`, `TtsUseCase`, `MessageTranslationUseCase`, `MessageUseCase`

#### Data (데이터)
- Room 기반 영속성, Repository 패턴, DAO/Entity/Database 구성

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

## STT (Speech-to-Text)

STT는 온라인에서만 동작합니다. 온디바이스 STT 모델은 용량 제약으로 포함하지 않았습니다.

### 아키텍처 구현
```kotlin
// Clean Architecture 적용
interface SpeechToTextRepositoryInterface  // Domain Layer
class SpeechToTextRepository              // Data Layer  
class SpeechToTextUseCase                 // Domain Layer
class ChatViewModel                       // Presentation Layer
```


## 성능 최적화 전략

### 1. 캐싱 전략
```kotlin
// LRU 캐시를 활용한 메모리 + 디스크 캐싱
class TranslationCacheService {
    private val memoryCache = LinkedHashMap<String, String>(100, 0.75f, true)
    // 디스크 캐시는 환경에 맞게 주입해 사용할 수 있습니다.
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

## 시스템 UI/IME 처리

화면과 시스템 바/키보드가 겹치지 않도록 다음 원칙을 적용합니다.

- MainActivity에서 edge-to-edge 활성화: `WindowCompat.setDecorFitsSystemWindows(window, false)`
- `Scaffold`의 기본 인셋 비활성화: `contentWindowInsets = WindowInsets(0, 0, 0, 0)`
- 최상위 컨테이너에 인셋 처리 일원화
  - 키보드 대응: `Modifier.imePadding()`
  - 내비게이션 바 회피: `Modifier.windowInsetsPadding(WindowInsets.navigationBars)`
- 목록과 입력창 동기 스크롤
  - `LazyColumn` 하단에 앵커를 두고, IME 표시 시 하단으로 bring-into-view 처리
  - 초기 진입 시 메시지 로드 및 입력창 높이 측정 이후 1회 하단 정렬

## 개발 환경 및 빌드

### 요구사항
- **Android Studio**: Flamingo 이상
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Kotlin**: 2.0.21
- **Compose BOM**: 2024.09.00

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
- ViewModel과 StateFlow로 상태 관리

### 3. 현대적 기법
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
