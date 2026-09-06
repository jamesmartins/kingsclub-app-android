# ADR 001 - Adequação ao Modo Edge-to-Edge e Compatibilidade Android 15/16 (SDK 35+)

- **Status**: Aceito e Implementado (Accepted)
- **Data**: 2026-09-06
- **Autor**: James Martins / Antigravity AI
- **Projeto**: Kings Club Android App (`KingsClubApp`)
- **Pacote**: `br.com.android.kingsclubapp`

---

## 1. Contexto

A partir do Android 15 (API 35) e Android 16 (API 36), o Google tornou obrigatório o comportamento **Edge-to-Edge** para todas as aplicações com `targetSdkVersion >= 35`.

Nesse cenário:
1. O sistema operacional não insere mais barras pretas ou opacas nas regiões da Status Bar e Navigation Bar.
2. O conteúdo das telas é estendido até as bordas físicas da tela por padrão, estendendo-se por trás das barras do sistema e de recortes de câmera (*cutout/notch*).
3. Sem o tratamento explícito de `WindowInsets`, elementos vitais do app (como botões de voltar, títulos, botões de login, links de cadastro e conteúdos de páginas web) sofrem sobreposição e perda de usabilidade.
4. Além disso, o projeto Kings Club utilizava target Java 11 em um ambiente Gradle 8.10.2 / AGP 8.8.2 e dependências com versão dinâmica (OneSignal `[5.0.0, 5.99.99]`), que traziam bibliotecas compiladas com Kotlin 2.2 incompatíveis com o compilador Kotlin 1.9.24.

---

## 2. Decisão Arquitetural

1. **Habilitação Global do Edge-to-Edge via AndroidX**:
   - Utilização do método oficial `enableEdgeToEdge()` da biblioteca `androidx.activity:activity-ktx:1.9.2`.
   - Invocação imediata no `onCreate(savedInstanceState)` antes do `setContentView()` em todas as Activities (`SplashActivity`, `IntroActivity2`, `LoginActivity2`, `WebViewMainActivity`, `WebViewActivity`).

2. **Tratamento Inteligente de Insets por Tipo de Layout**:
   - **Telas com Imagem de Fundo (SplashActivity e IntroActivity2)**:
     - O container raiz mantém seu background preenchendo a tela inteira.
     - `ViewCompat.setOnApplyWindowInsetsListener` aplica paddings nas margens seguras para que logotipos superiores fiquem abaixo da câmera e botões de ação permaneçam com folga segura da barra de gestos inferior.
   - **Telas com Toolbar e Formulário com Rolagem (LoginActivity2)**:
     - A `Toolbar` (`toolbar_login`) tem seu padding superior ajustado para `systemBars.top` e sua altura recalculada para `systemBars.top + 56dp`, preenchendo a Status Bar com a cor primária enquanto mantém o título e botão de fechar alinhados.
     - O `NestedScrollView` (`nested_scroll_login`) tem sua margem superior ajustada dinamicamente para corresponder à altura total da Toolbar.
     - O container raiz recebe os insets laterais e inferiores para que o rodapé com link de cadastro não colida com a barra de navegação/gestos.
   - **Telas Baseadas em WebView (WebViewMainActivity e WebViewActivity)**:
     - Insets aplicados no container para garantir que páginas web carregadas não tenham cabeçalhos ocultos pelo notch nem botões de formulário bloqueados na base da tela. Em `WebViewActivity`, a altura da ActionBar existente é somada aos insets superiores.

3. **Alinhamento do Toolchain de Build e Dependências**:
   - Atualização de `compileOptions` para `JavaVersion.VERSION_17`.
   - Atualização de `kotlinOptions.jvmTarget` para `"17"`.
   - Inclusão de `android.suppressUnsupportedCompileSdk=36` em `gradle.properties`.
   - Inclusão de `androidx.activity:activity-ktx:1.9.2` e `coreKtx = "1.13.1"` no catálogo de versões `libs.versions.toml`.
   - Fixação do OneSignal na versão estável `5.1.25` (`com.onesignal:OneSignal:5.1.25`), eliminando a importação de binários com metadados Kotlin 2.2.

---

## 3. Arquivos Modificados

| Componente | Arquivo | Modificação |
|---|---|---|
| **Build** | `gradle/libs.versions.toml` | Adição de `activityKtx:1.9.2`, `coreKtx:1.13.1` e fixação `onesignal:5.1.25` |
| **Build** | `app/build.gradle.kts` | Inclusão de `libs.androidx.activity.ktx`, `VERSION_17`, `jvmTarget = "17"`, `compileSdk = 36` e `targetSdk = 36` |
| **Build** | `gradle.properties` | Adicionado `android.suppressUnsupportedCompileSdk=36` |
| **Layout** | `app/src/main/res/layout/activity_login5.xml` | Adicionado `android:id="@+id/nested_scroll_login"` |
| **Layout** | `app/src/main/res/layout/activity_splash.xml` | Adicionado `android:id="@+id/splash_container"` |
| **Layout** | `app/src/main/res/layout/activity_intro2.xml` | Adicionado `android:id="@+id/intro_container"` |
| **Layout** | `app/src/main/res/layout/activity_webview.xml` | Adicionado `android:id="@+id/main_webview_container"` |
| **Layout** | `app/src/main/res/layout/activity_main.xml` | Adicionado `android:id="@+id/main_container"` |
| **Activity**| `SplashActivity.kt` | `enableEdgeToEdge()` e insets em `splash_container` |
| **Activity**| `IntroActivity2.kt` | `enableEdgeToEdge()` e insets em `intro_container` |
| **Activity**| `LoginActivity2.kt` | `enableEdgeToEdge()`, Toolbar expansiva e insets |
| **Activity**| `WebViewMainActivity.kt` | `enableEdgeToEdge()` e insets em `main_webview_container` |
| **Activity**| `WebViewActivity.kt` | `enableEdgeToEdge()` e insets em `login_container` |

---

## 4. Consequências e Resultados

- **Compilação**: `./gradlew compileDebugKotlin` - **Sucesso (0 erros)**.
- **Empacotamento**: `./gradlew assembleDebug` - **Sucesso** gerando `app/build/outputs/apk/debug/app-debug.apk`.
- **Compatibilidade**: Aplicativo pronto para publicação e execução fluida no Android 15 (API 35) e testes em Android 16 (API 36).
