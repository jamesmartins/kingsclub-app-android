# Adequação ao Modo Edge-to-Edge (Android 15 / SDK 35 e SDK 36) - Kings Club App

O Android 15 (API 35) e Android 16 (API 36) tornam o modo **Edge-to-Edge** obrigatório por padrão para apps com `targetSdkVersion >= 35`. O app Kings Club (`KingsClubApp`, pacote `br.com.android.kingsclubapp`) está configurado com `targetSdkVersion 35`. Sem o tratamento de `WindowInsets`, as barras do sistema (status bar, relógio, bateria, notch de câmera e a barra de navegação por gestos) sobrepõem o topo e o rodapé de telas essenciais como Splash, Intro, Login e WebViews.

Este plano define as etapas necessárias para aplicar o tratamento correto de `WindowInsets` via `enableEdgeToEdge()` e `ViewCompat.setOnApplyWindowInsetsListener` em todas as Activities do app Kings Club.

---

## Estrutura de Ambiente e Dependências (Kings Club)

- **Gradle Wrapper**: `8.10.2`
- **Android Gradle Plugin (AGP)**: `8.8.2`
- **Kotlin**: `1.9.24`
- **Compile / Target SDK**: `36`
- **AndroidX Core**: `androidx.core:core-ktx:1.13.1`
- **AndroidX Activity**: `androidx.activity:activity-ktx:1.9.2` (fornece `enableEdgeToEdge()`)

---

## Proposed Changes

### 1. Layouts XML (Identificadores e Estrutura)

#### [MODIFY] [activity_login5.xml](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/res/layout/activity_login5.xml)
- Adicionar `android:id="@+id/nested_scroll_login"` no `NestedScrollView` para permitir ajuste programático do `topMargin` correspondente à altura expandida da Toolbar com a barra de status.
- Confirmar que o container raiz possui `android:id="@+id/login_container"` e a Toolbar `android:id="@+id/toolbar_login"`.

#### [VERIFY] [activity_splash.xml](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/res/layout/activity_splash.xml)
- Confirmar presença de `android:id="@+id/splash_container"`.

#### [VERIFY] [activity_intro2.xml](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/res/layout/activity_intro2.xml)
- Confirmar presença de `android:id="@+id/intro_container"`.

#### [VERIFY] [activity_webview.xml](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/res/layout/activity_webview.xml)
- Confirmar presença de `android:id="@+id/main_webview_container"`.

#### [VERIFY] [activity_webview2.xml](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/res/layout/activity_webview2.xml)
- Confirmar presença de `android:id="@+id/login_container"`.

---

### 2. Activities (Aplicação de Edge-to-Edge e WindowInsets)

#### [MODIFY] [SplashActivity.kt](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/java/br/com/android/kingsclubapp/SplashActivity.kt)
- Chamar `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` em `splash_container` consumindo `systemBars() or displayCutout()`.

#### [MODIFY] [IntroActivity2.kt](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/java/br/com/android/kingsclubapp/IntroActivity2.kt)
- Chamar `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` em `intro_container` aplicando insets de `systemBars() or displayCutout()`, preservando o fundo e garantindo que o logo e os botões inferiores não fiquem sobrepostos pela barra de navegação/gestos.

#### [MODIFY] [LoginActivity2.kt](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/java/br/com/android/kingsclubapp/LoginActivity2.kt)
- Chamar `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` em `login_container`:
  - Insets superiores (`statusBars() or displayCutout()`): aplicados na Toolbar com padding top correspondente e altura recalculada (`systemBars.top + defaultActionBarHeight`), ajustando a margem superior do `NestedScrollView` (`nested_scroll_login`).
  - Insets laterais e inferiores (`systemBars.left, systemBars.right, systemBars.bottom`): aplicados como padding no container da tela, garantindo que botões e links de rodapé não fiquem sob a barra de gestos.

#### [MODIFY] [WebViewMainActivity.kt](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/java/br/com/android/kingsclubapp/WebViewMainActivity.kt)
- Chamar `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` em `main_webview_container` aplicando insets completos de status bar e navigation bar.

#### [MODIFY] [WebViewActivity.kt](file:///Users/james.martins/Documents/Projects/External/kingsclub-app-android/KingsClubApp/app/src/main/java/br/com/android/kingsclubapp/WebViewActivity.kt)
- Chamar `enableEdgeToEdge()` antes de `super.onCreate(savedInstanceState)`.
- Aplicar `ViewCompat.setOnApplyWindowInsetsListener` em `login_container`, compensando a altura da ActionBar e os insets de sistema.

---

## Verification Plan

### Automated / Build Tests
- Executar `./gradlew compileDebugKotlin` para validar que todas as classes Kotlin compilam com as novas chamadas e dependências.
- Executar `./gradlew assembleDebug` para validar a geração completa do APK.

### Manual Verification
- Validar se o splash e intro iniciam em tela cheia com visual imersivo e sem corte de elementos.
- Validar se a Toolbar de `LoginActivity2` fica alinhada com a barra de status sem sobreposição dos botões de voltar e título.
- Validar se a navegação web nas telas `WebViewMainActivity` e `WebViewActivity` respeita os insets inferiores e superiores.
