# RentIt — Platformă de Închirieri între Utilizatori

## Prezentare generală

RentIt este o platformă peer-to-peer de închirieri unde utilizatorii pot publica anunțuri pentru orice obiect neutilizat (unelte, mașini, locuințe, diverse) și alți utilizatori le pot închiria. Aplicația este construită cu **Spring Boot** (backend) și **Angular** (frontend), respectând strict ghidul de arhitectură definit mai jos.

---

## Ghid de arhitectură — obligatoriu de respectat

### Principii generale

- Cod curat, lizibil, profesionist — ca un senior engineer cu 30 de ani experiență
- Fiecare clasă are o singură responsabilitate (Single Responsibility Principle)
- Nicio logică de business în Controller sau Entity
- Nicio logică de mapare în Service — doar în Mapper
- DTO-uri separate pentru Request și Response
- Fără interfețe pentru Service (se folosește direct `@Service`)
- Comentarii doar acolo unde logica nu este auto-explicativă
- Niciun `System.out.println` — doar `@Slf4j` cu logging structurat

### Structura backend — Spring Boot

```
src/main/java/com/singularity/rentit/
│
├── config/
│   ├── SecurityConfig.java          — JWT filter chain, CORS, endpoint permissions
│   ├── JwtConfig.java               — secret, expiry, token generation/validation
│   ├── StripeConfig.java            — Stripe API key, webhook secret
│   ├── WebSocketConfig.java         — STOMP over WebSocket pentru chat real-time
│   └── OpenApiConfig.java           — Swagger/OpenAPI 3 docs
│
├── controller/
│   ├── AuthController.java          — /api/auth/** (register, login, refresh)
│   ├── UserController.java          — /api/users/** (profil, KYC upload, GDPR)
│   ├── ListingController.java       — /api/listings/** (CRUD anunțuri)
│   ├── RentalController.java        — /api/rentals/** (inchirieri, status, QR)
│   ├── PaymentController.java       — /api/payments/** (Stripe intent, webhook)
│   ├── WishlistController.java      — /api/wishlist/**
│   ├── ChatController.java          — /api/chat/** (REST pentru istoric)
│   ├── ReviewController.java        — /api/reviews/**
│   ├── AdminController.java         — /api/admin/** (users, rapoarte, support)
│   └── GeoController.java           — /api/geo/** (geocoding, listings by location)
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── KycService.java              — validare identitate, selfie, status verificare
│   ├── GdprService.java             — generare acord, semnare, stocare
│   ├── ListingService.java
│   ├── CategoryService.java
│   ├── RentalService.java
│   ├── PaymentService.java          — Stripe Connect, split plată, garanție
│   ├── WishlistService.java
│   ├── ChatService.java
│   ├── ReviewService.java
│   ├── NotificationService.java     — email + in-app notificări
│   ├── EmailService.java            — trimitere emailuri tranzacționale via Thymeleaf + JavaMailSender
│   ├── SmtpConfigService.java       — CRUD config SMTP, criptare parolă, reinit JavaMailSender runtime
│   ├── GeoService.java              — geocoding Nominatim, distanță haversine
│   ├── QrCodeService.java           — generare/validare QR pentru returnare
│   ├── ReportService.java           — rapoarte financiare user și admin
│   └── AdminService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── KycRepository.java
│   ├── GdprAgreementRepository.java
│   ├── ListingRepository.java
│   ├── ListingAttributeRepository.java
│   ├── RentalRepository.java
│   ├── PaymentRepository.java
│   ├── WishlistRepository.java
│   ├── ChatMessageRepository.java
│   ├── ChatRoomRepository.java
│   ├── ReviewRepository.java
│   └── AdminReportRepository.java
│
├── entity/
│   ├── User.java
│   ├── KycVerification.java
│   ├── GdprAgreement.java
│   ├── Listing.java
│   ├── ListingImage.java
│   ├── ListingAttribute.java        — câmpuri dinamice per categorie (JSON sau EAV)
│   ├── Category.java
│   ├── Rental.java
│   ├── Payment.java
│   ├── Guarantee.java
│   ├── WishlistItem.java
│   ├── SavedOwner.java
│   ├── ChatRoom.java
│   ├── ChatMessage.java
│   ├── Review.java
│   ├── Notification.java
│   └── SmtpConfig.java              — configurație SMTP stocată în DB, parolă AES-256 encrypted
│
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── UpdateProfileRequest.java
│   │   ├── KycSubmitRequest.java
│   │   ├── GdprSignRequest.java
│   │   ├── CreateListingRequest.java
│   │   ├── UpdateListingRequest.java
│   │   ├── CreateRentalRequest.java
│   │   ├── UpdateRentalStatusRequest.java
│   │   ├── SendMessageRequest.java
│   │   ├── CreateReviewRequest.java
│   │   ├── AdminUserFilterRequest.java
│   │   └── ReportPeriodRequest.java
│   │
│   └── response/
│       ├── AuthResponse.java
│       ├── UserProfileResponse.java
│       ├── KycStatusResponse.java
│       ├── ListingResponse.java
│       ├── ListingDetailResponse.java
│       ├── ListingSummaryResponse.java
│       ├── RentalResponse.java
│       ├── PaymentIntentResponse.java
│       ├── PaymentSplitPreviewResponse.java  — preview comision în timp real
│       ├── ChatMessageResponse.java
│       ├── ChatRoomResponse.java
│       ├── ReviewResponse.java
│       ├── WishlistResponse.java
│       ├── DashboardResponse.java
│       ├── FinancialReportResponse.java
│       ├── AdminReportResponse.java
│       └── GeoSearchResponse.java
│
├── mapper/
│   ├── UserMapper.java
│   ├── ListingMapper.java
│   ├── RentalMapper.java
│   ├── ChatMapper.java
│   ├── ReviewMapper.java
│   └── PaymentMapper.java
│
├── exception/
│   ├── GlobalExceptionHandler.java  — @ControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   ├── UnauthorizedException.java
│   ├── KycNotVerifiedException.java
│   ├── ListingUnavailableException.java
│   └── PaymentException.java
│
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── CustomUserDetailsService.java
│
├── websocket/
│   ├── ChatWebSocketController.java — @MessageMapping pentru STOMP
│   └── WebSocketEventListener.java
│
├── enums/
│   ├── UserRole.java                — USER, ADMIN
│   ├── KycStatus.java               — PENDING, VERIFIED, REJECTED
│   ├── ListingStatus.java           — DRAFT, ACTIVE, RENTED, INACTIVE
│   ├── RentalStatus.java            — PENDING_PAYMENT, PAID, READY_TO_PICKUP,
│   │                                   ACTIVE, RETURNED, CANCELLED, DISPUTED
│   ├── PaymentStatus.java           — PENDING, HELD, RELEASED, REFUNDED
│   ├── CategoryType.java            — TOOLS, VEHICLES, REAL_ESTATE, ELECTRONICS,
│   │                                   SPORTS, OTHER
│   └── MessageType.java             — TEXT, IMAGE, ATTACHMENT, SYSTEM
│
└── RentItApplication.java
```

### Structura frontend — Angular

```
src/app/
│
├── core/                            — singleton, importat o singură dată în AppModule
│   ├── interceptors/
│   │   ├── auth.interceptor.ts      — adaugă JWT header la fiecare request
│   │   └── error.interceptor.ts     — tratare globală erori HTTP
│   ├── guards/
│   │   ├── auth.guard.ts
│   │   ├── kyc.guard.ts             — redirect dacă KYC necompletat
│   │   └── admin.guard.ts
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── storage.service.ts       — localStorage wrapper type-safe
│   │   ├── notification.service.ts  — toast/snackbar global
│   │   ├── theme.service.ts         — dark/light mode, persistat în localStorage
│   │   └── language.service.ts      — RO/EN switch, persistat în localStorage
│   └── core.module.ts
│
├── shared/                          — componente reutilizabile
│   ├── components/
│   │   ├── map-picker/              — OpenLayers hartă + geocoding Nominatim
│   │   ├── image-upload/            — drag & drop, preview, compresie
│   │   ├── verification-badge/      — bulina verde/gri KYC
│   │   ├── star-rating/
│   │   ├── price-split-preview/     — calcul comision în timp real
│   │   ├── qr-scanner/              — jsQR pentru scanare returnare
│   │   ├── language-toggle/         — switch RO / EN în navbar
│   │   ├── theme-toggle/            — switch dark / light mode în navbar
│   │   └── skeleton-loader/
│   ├── pipes/
│   │   ├── currency-ron.pipe.ts
│   │   └── time-ago.pipe.ts
│   ├── directives/
│   │   └── lazy-image.directive.ts
│   └── shared.module.ts
│
├── features/
│   ├── auth/
│   │   ├── components/
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   └── forgot-password/
│   │   ├── services/auth-api.service.ts
│   │   └── auth.module.ts
│   │
│   ├── profile/
│   │   ├── components/
│   │   │   ├── profile-view/
│   │   │   ├── profile-edit/
│   │   │   ├── kyc-wizard/          — selfie + CI upload, pas cu pas
│   │   │   └── gdpr-agreement/      — afișare + semnare acord GDPR
│   │   ├── services/profile-api.service.ts
│   │   └── profile.module.ts
│   │
│   ├── listings/
│   │   ├── components/
│   │   │   ├── listing-list/        — pagina principală cu anunțuri
│   │   │   ├── listing-card/        — card anunț în listă
│   │   │   ├── listing-detail/      — pagina completă a unui anunț
│   │   │   ├── listing-create/      — wizard creare anunț
│   │   │   ├── listing-edit/
│   │   │   ├── listing-filters/     — filtre + search + arie km
│   │   │   └── category-fields/     — câmpuri dinamice per categorie
│   │   │       ├── tools-fields/
│   │   │       ├── vehicle-fields/
│   │   │       ├── real-estate-fields/
│   │   │       └── generic-fields/
│   │   ├── services/listings-api.service.ts
│   │   └── listings.module.ts
│   │
│   ├── rentals/
│   │   ├── components/
│   │   │   ├── rental-checkout/     — selectare date + confirmare
│   │   │   ├── rental-payment/      — Stripe Elements
│   │   │   ├── rental-status/       — status curent + QR display
│   │   │   └── qr-return/           — scanare QR la returnare
│   │   ├── services/rentals-api.service.ts
│   │   └── rentals.module.ts
│   │
│   ├── dashboard/
│   │   ├── components/
│   │   │   ├── dashboard-home/      — overview cards
│   │   │   ├── my-listings/
│   │   │   ├── my-rentals/          — ce am închiriat eu
│   │   │   ├── my-earnings/         — ce am dat spre chirie + câștiguri
│   │   │   ├── financial-report/    — raport cu selector perioadă
│   │   │   └── wishlist/
│   │   ├── services/dashboard-api.service.ts
│   │   └── dashboard.module.ts
│   │
│   ├── chat/
│   │   ├── components/
│   │   │   ├── chat-list/           — lista conversațiilor
│   │   │   ├── chat-room/           — conversație în timp real (WebSocket)
│   │   │   └── chat-bubble/
│   │   ├── services/
│   │   │   ├── chat-api.service.ts
│   │   │   └── chat-websocket.service.ts  — STOMP client
│   │   └── chat.module.ts
│   │
│   └── admin/
│       ├── components/
│       │   ├── admin-dashboard/
│       │   ├── admin-users/         — tabel users + detalii + KYC documente
│       │   ├── admin-reports/       — rapoarte financiare admin (brut/cheltuieli/net)
│       │   ├── admin-support/       — chat support cu userii
│       │   └── admin-smtp/          — configurator SMTP: host, port, user, parolă, display name, test
│       ├── services/admin-api.service.ts
│       └── admin.module.ts
│
├── api/
│   └── endpoints.ts                 — toate URL-urile centralizate, niciun string hardcodat
│
├── i18n/
│   ├── ro.json                      — toate textele în română (limba implicită)
│   └── en.json                      — toate textele în engleză
│
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
│
└── app-routing.module.ts
```

---

## Stack tehnic

### Backend
- **Java 17+**, **Spring Boot 3.x**
- **Spring Security** cu JWT (io.jsonwebtoken / jjwt)
- **Spring Data JPA** + **Hibernate**
- **MySQL 8** (sau PostgreSQL)
- **Spring WebSocket** + STOMP pentru chat real-time
- **Stripe Java SDK** — Stripe Connect pentru plăți split
- **ZXing** — generare cod QR
- **MapStruct** — mapare Entity ↔ DTO (sau mapare manuală în Mapper)
- **Lombok** — @Getter, @Setter, @Builder, @Slf4j
- **AWS S3 / MinIO** — stocare imagini și documente KYC
- **Flyway** — migrații baze de date versionate
- **Springdoc OpenAPI** — documentație Swagger
- **Maven** build tool

### Frontend
- **Angular 17+** (standalone components sau module-based — alege unul și rămâi consistent)
- **TypeScript strict mode** activat
- **OpenLayers** — hartă interactivă open-source (nu Google Maps)
- **Nominatim API** (OpenStreetMap) — geocoding gratuit, adresă → coordonate și invers
- **Stripe.js + Angular Elements** — plăți securizate
- **@stomp/stompjs** + **sockjs-client** — WebSocket chat
- **jsQR** — scanare cod QR în browser
- **RxJS** — state management reactiv
- **Angular Animations** — tranziții fluide stil Apple
- **@ngx-translate/core** + **@ngx-translate/http-loader** — internationalizare (RO / EN)
- Design: **CSS custom properties**, fără framework UI extern (Tailwind opțional)

---

## Funcționalități detaliate

### 1. Autentificare și cont

- **Înregistrare**: email, parolă, prenume, nume, telefon
- **Login**: JWT cu refresh token (access token 15 min, refresh token 30 zile)
- La primul login, utilizatorul este redirecționat către completarea profilului și acordul GDPR
- **Resetare parolă** prin email (token one-time, valabil 1 oră)

### 2. KYC — Verificare identitate

Wizard în 3 pași:

**Pas 1 — Selfie**
- Captură selfie direct din camera dispozitivului (`getUserMedia`)
- Upload la server, stocat securizat, vizibil doar adminului

**Pas 2 — Date buletin**
- Câmpuri: serie și număr CI, CNP, data nașterii, data expirării CI
- Poza față CI (upload)
- Poza spate CI (upload)

**Pas 3 — Confirmare**
- Status inițial: `PENDING`
- Adminul verifică din dashboard și setează `VERIFIED` sau `REJECTED`
- Pe profilul utilizatorului apare **bulina de verificare** (verde = verificat, gri = în așteptare)
- Utilizatorii neverficați pot naviga dar nu pot crea anunțuri sau închiria

### 3. Acord GDPR

- Generat dinamic cu data curentă și datele utilizatorului
- Conținut: prelucrare date personale, date KYC, date financiare, dreptul la ștergere
- Utilizatorul bifează „Am citit și sunt de acord" și semnează electronic (timestamp + IP)
- PDF generat și stocat, trimis pe email la utilizator
- Legislație: **Regulamentul (UE) 2016/679 (GDPR)**, Legea nr. 190/2018

### 4. Emailuri tranzacționale automate

Toate emailurile sunt trimise prin configurația SMTP salvată de admin (vezi secțiunea „Configurator SMTP email" din dashboard admin). `EmailService` folosește template-uri HTML curate, stil Apple (alb, font Inter, accent indigo), responsive pentru mobile.

#### Email 1 — Confirmare semnare acord GDPR
**Destinatar**: utilizatorul care tocmai a semnat  
**Declanșator**: imediat după `GdprService.signAgreement()`  
**Subiect**: `Acord GDPR semnat — RentIt`  
**Conținut**:
- Confirmare că acordul a fost semnat electronic
- Data și ora semnării (cu timezone Romania)
- IP-ul de la care s-a semnat
- Atașament PDF: acordul GDPR complet generat (via iText sau Apache PDFBox)
- Link: „Vizualizează acordul tău în cont"
- Paragraf: drepturile utilizatorului (acces, rectificare, ștergere, portabilitate)
- Footer: date de contact DPO, temeiul legal (GDPR Art. 6(1)(a))

#### Email 2 — Confirmare plată și detalii închiriere (chiriaș)
**Destinatar**: utilizatorul care a plătit închirierea  
**Declanșator**: după confirmarea plății Stripe (webhook `payment_intent.succeeded`)  
**Subiect**: `Plată confirmată — [Titlu anunț] · RentIt`  
**Conținut**:
- Banner: „Plata ta a fost procesată cu succes"
- Detalii închiriere:
  - Produs: titlu anunț + prima poză (inline)
  - Proprietar: nume + bulina KYC dacă e verificat
  - Perioadă: `[data start] → [data end]` (ex: `15 mai 2025 → 18 mai 2025`)
  - Locație de ridicare: adresa proprietarului (sau adresa din anunț)
  - Număr zile: X zile
  - Preț per zi: X RON
  - Subtotal chirie: X RON
  - Garanție (dacă aplicabil): X RON
  - **Total plătit**: X RON (bold, evidențiat)
  - ID tranzacție: `#RNT-XXXXXXXXX`
- Secțiune „Ce urmează":
  1. Proprietarul pregătește produsul
  2. Vei fi notificat când e gata de ridicat
  3. La ridicare, prezintă ID-ul comenzii
  4. La returnare, scanează codul QR al proprietarului pentru a recupera garanția
- Buton: „Vezi comanda în aplicație"
- Footer: contact support, politica de anulare

#### Email 3 — Notificare primire plată (proprietar)
**Destinatar**: proprietarul anunțului  
**Declanșator**: același webhook `payment_intent.succeeded`  
**Subiect**: `Ai primit o rezervare pentru [Titlu anunț] · RentIt`  
**Conținut**:
- Banner: „Cineva vrea să închirieze produsul tău!"
- Detalii rezervare:
  - Produs: titlu anunț
  - Chiriaș: prenume + nume (fără date de contact complete din motive de privacy)
  - Perioadă: `[data start] → [data end]`
  - Suma primită (net după comision): X RON
  - Comision aplicație (5%): X RON
  - ID rezervare: `#RNT-XXXXXXXXX`
- Status: „Suma este în așteptare până la confirmarea ridicării"
- Secțiune „Ce trebuie să faci":
  1. Pregătește produsul până la data de start
  2. Marchează comanda ca „Gata de ridicare" în aplicație
  3. La ridicare, afișează codul QR pentru scanare la returnare
- Buton: „Gestionează comanda"
- Footer: contact support

#### Email 4 — Returnare confirmată și garanție eliberată (chiriaș)
**Destinatar**: chiriașul  
**Declanșator**: după scanarea cu succes a QR-ului de returnare  
**Subiect**: `Returnare confirmată — garanția ta a fost eliberată · RentIt`  
**Conținut**:
- Confirmare returnare produs
- Suma garanției returnate: X RON (în 3–5 zile lucrătoare pe card)
- Invitație de a lăsa o recenzie proprietarului
- Buton: „Lasă o recenzie"

#### Email 5 — Cont verificat (KYC aprobat)
**Destinatar**: utilizatorul  
**Declanșator**: când adminul setează KYC status = `VERIFIED`  
**Subiect**: `Contul tău a fost verificat · RentIt`  
**Conținut**:
- Felicitare + confirmare că bulina de verificare e activă pe profil
- Ce poate face acum: posta anunțuri, închiria de la alți utilizatori
- Buton: „Postează primul tău anunț"

#### Implementare tehnică emailuri

```java
// service/EmailService.java — @Service
// Folosește JavaMailSender reinițializat dinamic din SmtpConfigService
// Template engine: Thymeleaf (spring-boot-starter-thymeleaf) pentru HTML emails

public void sendGdprAgreement(User user, byte[] pdfBytes)
public void sendRentalConfirmationToTenant(Rental rental, Payment payment)
public void sendRentalNotificationToOwner(Rental rental, Payment payment)
public void sendReturnConfirmationToTenant(Rental rental)
public void sendKycApprovedNotification(User user)
```

Template-uri Thymeleaf în `src/main/resources/templates/email/`:
```
email/
├── layout.html              — template de bază (header logo, footer, stiluri inline)
├── gdpr-signed.html
├── rental-confirmation-tenant.html
├── rental-notification-owner.html
├── return-confirmed.html
└── kyc-approved.html
```

Stiluri CSS **inline** în template-uri (nu externe) — necesar pentru compatibilitate Gmail/Outlook.  
Toate emailurile sunt **responsive** (max-width 600px, fluid pe mobile).  
Logo RentIt în header, footer cu adresa legală a companiei, link dezabonare (conform GDPR).

---

### 5. Anunțuri de închiriere

#### Câmpuri comune (toate categoriile)
- Titlu anunț
- Categorie (select din enum)
- Descriere (textarea rich)
- Poze produs (minim 1, maxim 10 — upload multiple)
- Locație: selector hartă OpenLayers SAU câmp adresă cu geocoding Nominatim
- Preț pe zi (RON)
- Preț pe săptămână (opțional, autocalculat cu discount implicit)
- Preț pe lună (opțional)
- Garanție: calculată automat 50% din valoarea totală (nu se aplică la Real Estate)
- Disponibilitate: calendar cu perioadele blocate

#### Câmpuri specifice per categorie

**TOOLS — Unelte și scule**
- Brand (text)
- Model (text)
- Putere (W sau kW)
- Alimentare: `Baterie` | `Priză 230V` | `Trifazat 380V` | `Termic`
- Tensiune baterie (dacă baterie): 12V / 18V / 36V / 54V
- Capacitate baterie (Ah) — opțional
- Funcții principale (multi-select sau tags)
- Stare: `Nou` | `Ca nou` | `Folosit bine` | `Folosit cu urme`
- Tip sculă: `Bormaşină` | `Flex` | `Fierăstrău` | `Polizor` | `Compresor` | `Generator` | `Sudură` | `Altele`
- Include accesorii (textarea)

**VEHICLES — Mașini și vehicule**
- Marcă
- Model
- An fabricație
- Caroserie: `Berlină` | `Hatchback` | `Break` | `SUV` | `Van` | `Dubă` | `Pick-up` | `Auto-utilitară` | `Decapotabilă` | `Coupé`
- Motor: cilindree (cm³), putere (CP/kW)
- Combustibil: `Benzină` | `Diesel` | `Electric` | `Hibrid` | `GPL`
- Transmisie: `Manuală` | `Automată` | `Semi-automată`
- Rulaj (km)
- Număr locuri
- Capacitate portbagaj (L) — opțional
- Tracțiune: `2WD` | `4WD` | `AWD`
- Dotări incluse (multi-select): AC, GPS, Scaun copil, Bluetooth, Cruise control, etc.
- Permis necesar: `B` | `B+E` | `C` | `D` | `Fără permis`
- Asigurare inclusă: Da / Nu
- Franciză (RON) — dacă asigurare inclusă

**REAL_ESTATE — Locuințe și spații**
- Tip: `Apartament` | `Casă` | `Vilă` | `Cameră` | `Studio` | `Spațiu comercial` | `Teren`
- Suprafață utilă (m²)
- Suprafață totală (m²)
- Număr camere
- Număr băi
- Etaj / Total etaje
- Facilități (multi-select): WiFi, Parcare, AC, Încălzire centrală, Terasă, Grădină, Piscină, Lift, Mașină spălat, Uscător, Bucătărie echipată, Animale permise, Fumători
- Regim: `Zilnic` | `Săptămânal` | `Lunar`
- Check-in / Check-out time
- Reguli casă (textarea)
- Număr maxim oaspeți
- **Notă**: categoria Real Estate NU generează garanție

**ELECTRONICS — Electronice și echipamente**
- Tip: `Laptop` | `Camera foto/video` | `Dronă` | `Proiector` | `Console jocuri` | `Echipament audio` | `Altele`
- Brand, Model
- Stare
- Specificații tehnice (textarea structurată)
- Accesorii incluse

**SPORTS — Echipament sportiv**
- Tip sport / activitate
- Dimensiune / mărime (dacă aplicabil)
- Stare
- Include echipament de protecție: Da / Nu

**OTHER — Diverse**
- Câmpuri libere: tip obiect, stare, dimensiuni, greutate, note

### 5. Pagina principală și căutare

- **Detectare locație** automată (`navigator.geolocation`) la primul acces — cerere permisiune explicită
- **Selector arie**: slider 1–100 km față de locația curentă sau față de orașul selectat
- **Selector județ + oraș** — fallback dacă geolocation refuzat
- **Filtre disponibile**:
  - Categorie produs
  - Preț min–max per zi
  - Distanță (km)
  - Județ / Oraș
  - Disponibil în perioada (date picker range)
  - Tip specific per categorie (ex: Tip caroserie pentru vehicule)
  - KYC verificat (doar proprietari verificați)
  - Rating minim proprietar
- **Bară de search**: căutare full-text în titlu, descriere, brand, model
- **Sortare**: Relevanță | Cel mai aproape | Preț crescător | Preț descrescător | Cel mai nou | Rating
- **Harta anunțurilor**: toggle listă / hartă — pe hartă se văd pin-urile anunțurilor din zona selectată
- Anunțurile indisponibile în perioada selectată NU apar în rezultate

### 6. Fluxul de închiriere și plată

**Stripe Connect** (nu Stripe standard) — permite plăți split direct între utilizatori.

#### Calculul comisionului (afișat în timp real)

```
Preț total plătit de chiriaș:  P (ex: 100 RON)
Fee aplicație (5%):            P × 0.05
Fee Stripe (actual din API):   calculat pe suma totală P
Suma primită de proprietar:    P - (P × 0.05)
Net după Stripe:               P - (P × 0.05) - stripe_fee

Exemplu real (fee Stripe = 1.4% + 1 RON per tranzacție):
  P = 100 RON
  Fee aplicație = 5 RON
  Fee Stripe ≈ 100 × 0.014 + 1 = 2.40 RON
  Proprietar primește = 100 - 5 = 95 RON
  Profit net aplicație = 5 - 2.40 = 2.60 RON
```

Afișează aceste valori live în formularul de creare anunț (proprietar) și în pagina de checkout (chiriaș).

#### Fluxul complet

1. Chiriașul selectează produsul + perioada dorită
2. Sistem verifică disponibilitatea (nu există alte rezervări suprapuse)
3. Checkout: confirmare detalii + preview plată (chirie + garanție dacă aplicabil)
4. Plată prin Stripe Elements (card securizat)
5. Suma = chirie + garanție (50% din chirie, dacă categoria o cere)
6. **Proprietarul primește notificare**: „Plată primită — pregătește produsul"
7. Status rental: `PAID → READY_TO_PICKUP` (setat manual de proprietar)
8. La confirmare `READY_TO_PICKUP` → banii de chirie sunt virate proprietarului (garanția rămâne hold)
9. Proprietarul afișează **cod QR** în aplicație (generat de backend, unic per rental)
10. La returnare: chiriașul scanează QR-ul proprietarului → sistem confirmă returnarea
11. Garanția este returnată automat pe cardul chiriașului
12. Status final: `RETURNED`

#### Garanție
- 50% din valoarea totală a închirierii
- Aplicabil pentru: TOOLS, VEHICLES, ELECTRONICS, SPORTS, OTHER
- **Nu se aplică** pentru: REAL_ESTATE

### 7. Dashboard utilizator

**Secțiunea „Ce am închiriat"**
- Tabel: produs, proprietar, perioadă, sumă plătită, garanție, status, acțiuni

**Secțiunea „Ce am dat spre chirie"**
- Tabel: produs, chiriaș, perioadă, sumă primită, status, acțiuni
- Buton schimbare status (`READY_TO_PICKUP`)
- Afișare cod QR pentru returnare

**Raport financiar**
- Selector perioadă: Luna curentă | Luna trecută | Anul curent | Anul trecut | Custom (date picker)
- Câștiguri din închirieri (net după comision aplicație)
- Cheltuieli cu propriile închirieri
- Sold net

**Wishlist**
- Anunțuri salvate
- Proprietari salvați (utilizatori de la care vrei să mai închiriezi)

### 8. Chat în timp real

- WebSocket (STOMP over SockJS)
- O cameră de chat per rental (se deschide automat la confirmare plată)
- Mesaje text + upload imagini + atașamente (PDF, doc)
- Indicatori: citit / necitit, online / offline, „scrie..."
- Istoricul mesajelor paginat (REST API)
- Notificări push browser pentru mesaje noi

### 9. Dashboard Admin

**Acces**: `/admin` — protejat de `AdminGuard`, rolul `ADMIN`

**Gestionare utilizatori**
- Tabel cu toți userii (paginat, filtrabil, sortabil)
- Detalii user: date personale, status KYC, documente (selfie + CI față/spate)
- Acțiuni: Verifică KYC (approve/reject), Suspendă cont, Șterge cont
- Export CSV

**Rapoarte financiare admin**
- Selector perioadă: Luna curentă | Luna trecută | Anul curent | Anul trecut | Custom
- **Venituri brute**: total fee 5% colectat
- **Cheltuieli**: fee-uri Stripe plătite
- **Profit net**: brut − cheltuieli
- Grafic lunar (bar chart)
- Export PDF / CSV

**Chat support**
- Lista conversațiilor de support deschise de utilizatori
- Răspuns direct din dashboard
- Status: Deschis | În lucru | Rezolvat

**Configurator SMTP email**
- Secțiune dedicată în admin dashboard: `Setări → Email SMTP`
- Configurația este stocată în baza de date (tabel `smtp_config`), nu în `application.yml` — poate fi modificată din UI fără restart
- Câmpuri configurabile:
  - **Host SMTP** (ex: `smtp.zoho.com`, `smtp.gmail.com`, `mail.example.com`)
  - **Port** (ex: 587 pentru TLS, 465 pentru SSL, 25 pentru plain)
  - **Securitate**: `NONE` | `STARTTLS` | `SSL/TLS`
  - **Email expeditor** (adresa de la care se trimit emailurile, ex: `noreply@rentit.ro`)
  - **Parolă** (stocată criptat în DB cu AES-256, niciodată în plaintext)
  - **Nume afișat** (display name — ex: `RentIt Platform`, apare în câmpul „From" al emailului)
  - **Email de test**: buton „Trimite email de test" care trimite un email de verificare la adresa adminului logat
- La salvare configurație: Spring `JavaMailSender` este reinițializat dinamic cu noile setări, fără restart aplicație
- Dacă nu există o configurație salvată în DB, se folosesc valorile fallback din `application.yml`
- Status vizibil în dashboard: `Configurat` (verde) | `Neconfigurat` (portocaliu) | `Eroare conexiune` (roșu) — verificat la interval de 5 minute

**Entitate și backend pentru SMTP config:**

```java
// entity/SmtpConfig.java
@Entity
@Table(name = "smtp_config")
public class SmtpConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String host;
    private Integer port;
    private String security;          // NONE, STARTTLS, SSL
    private String username;          // email expeditor
    private String encryptedPassword; // AES-256 encrypted
    private String displayName;       // nume afișat în From
    private boolean active;
    private LocalDateTime updatedAt;
    private String updatedBy;         // email admin care a făcut modificarea
}
```

```java
// service/SmtpConfigService.java — @Service
// - saveConfig(SmtpConfigRequest): criptează parola, salvează, reinițializează JavaMailSender
// - testConnection(): trimite email de test, returnează succes/eroare
// - getCurrentConfig(): returnează config fără parola decriptată (doar *****)
// - reinitializeMailSender(): construiește Properties și reinit bean-ul runtime
```

```java
// controller/AdminController.java — adaugă endpoint-urile:
// GET  /api/admin/smtp           — obține config curentă (fără parolă)
// POST /api/admin/smtp           — salvează/actualizează config
// POST /api/admin/smtp/test      — trimite email de test
// GET  /api/admin/smtp/status    — verifică starea conexiunii SMTP
```

Frontend — `admin/components/admin-smtp/`:
- Formular cu toate câmpurile de mai sus
- Toggle show/hide parolă
- Indicator stare conexiune (live ping la `/api/admin/smtp/status`)
- Buton „Salvează și testează" — salvează + trimite email de test în același pas
- Mesaj confirmare cu timestamp ultimei modificări și adminul care a făcut-o

### 10. Cont admin implicit

La pornirea aplicației (seed la primul start via `ApplicationRunner`):

```java
// AdminSeeder.java — rulat o singură dată dacă userul nu există
email:    admin@singularity-cloud.com
password: rgbiuli1  (bcrypt encoded)
role:     ADMIN
kyc:      VERIFIED  (skip KYC pentru admin)
```

---

## Design și UI — stil Apple

- **Fonturi**: SF Pro Text / Inter ca fallback — clean, geometric
- **Culori**: alb dominant, accente subtle (indigo sau coral), gri neutru pentru suprafețe
- **Spațiere**: generosă, breathing room — niciun element aglomerat
- **Colțuri**: `border-radius` consistent (8px carduri mici, 16px carduri mari, 24px modals)
- **Umbre**: subtile, o singură umbră per nivel de adâncime
- **Animații**: tranziții fluide 200–300ms, `ease-in-out` — niciun efect dramatic
- **Iconițe**: Lucide Icons sau SF Symbols web — outline, nu filled
- **Responsive**: Mobile-first. Breakpoints: 375px (mobile) | 768px (tablet) | 1280px (desktop)
- **Navigare mobilă**: bottom navigation bar pe mobile cu 5 secțiuni principale
- **Carduri anunțuri**: imagine mare, titlu, preț/zi, distanță, rating, bulina KYC
- **Loading states**: skeleton screens (nu spinner generic)
- **Formulare**: label flotant Apple-style, validare inline, mesaje eroare clare

---

## Internaționalizare (i18n) — Română și Engleză

### Implementare

Folosește **`@ngx-translate/core`** cu fișiere JSON per limbă în `src/app/i18n/`.

```typescript
// app.module.ts
TranslateModule.forRoot({
  defaultLanguage: 'ro',
  loader: {
    provide: TranslateLoader,
    useFactory: HttpLoaderFactory,
    deps: [HttpClient]
  }
})
```

### Reguli obligatorii

- **Zero text hardcodat** în template-uri HTML sau componente TypeScript — totul prin `translate` pipe sau `TranslateService.instant()`
- Fișierele `ro.json` și `en.json` trebuie să aibă **exact aceleași chei** — orice cheie lipsă înseamnă bug vizibil
- Structura cheilor: ierarhică, cu prefix per modul (ex: `auth.login.title`, `listing.create.step1`)
- Valorile pot conține **interpolări**: `"greeting": "Bună ziua, {{name}}!"` → `{{ 'greeting' | translate:{ name: user.firstName } }}`
- **Mesajele de eroare** din backend (excepții, validări) sunt returnate cu un cod (ex: `error.listing.unavailable`) și traduse pe frontend
- Emailurile tranzacționale se trimit în **limba preferată a utilizatorului** (câmp `preferredLanguage` pe entitatea `User`)

### Limbă implicită și persistare

- Limba implicită: **Română** (`ro`)
- La prima vizită: detectare automată din `navigator.language` — dacă browserul e în engleză, se setează EN automat
- Alegerea utilizatorului se salvează în **localStorage** (`rentit_lang`) și, dacă e autentificat, în profilul lui pe server (câmp `preferredLanguage` în `User`)
- La login: se încarcă limba din profilul de pe server, suprascrie localStorage

### Switch limbă în UI

- **Navbar** (desktop): două butoane pill `RO` | `EN` — cel activ e evidențiat (fill solid, cel inactiv outline)
- **Bottom nav** (mobile): switch în meniul de profil / settings
- Schimbarea limbii este **instantanee**, fără reload — `TranslateService.use('en')`
- Animație subtilă la schimbare: fade-out 100ms → swap text → fade-in 100ms

### Structura fișierelor JSON (exemple reprezentative)

```json
// ro.json — fragment
{
  "common": {
    "save": "Salvează",
    "cancel": "Anulează",
    "delete": "Șterge",
    "edit": "Editează",
    "back": "Înapoi",
    "next": "Continuă",
    "loading": "Se încarcă...",
    "error": "A apărut o eroare",
    "success": "Succes",
    "confirm": "Confirmă",
    "search": "Caută",
    "filter": "Filtrează",
    "close": "Închide",
    "yes": "Da",
    "no": "Nu"
  },
  "nav": {
    "home": "Acasă",
    "dashboard": "Contul meu",
    "post": "Postează anunț",
    "chat": "Mesaje",
    "wishlist": "Favorite"
  },
  "auth": {
    "login": {
      "title": "Bine ai revenit",
      "subtitle": "Autentifică-te în contul tău",
      "email": "Adresă de email",
      "password": "Parolă",
      "submit": "Autentifică-te",
      "forgot": "Ai uitat parola?",
      "noAccount": "Nu ai cont? Înregistrează-te"
    },
    "register": {
      "title": "Creează-ți contul",
      "firstName": "Prenume",
      "lastName": "Nume",
      "email": "Adresă de email",
      "phone": "Număr de telefon",
      "password": "Parolă",
      "confirmPassword": "Confirmă parola",
      "submit": "Creează cont",
      "hasAccount": "Ai deja cont? Autentifică-te",
      "terms": "Prin înregistrare ești de acord cu Termenii și Condițiile"
    }
  },
  "listing": {
    "rent": "Închiriază",
    "perDay": "/ zi",
    "distance": "{{km}} km distanță",
    "verified": "Proprietar verificat",
    "available": "Disponibil",
    "unavailable": "Indisponibil în perioada selectată",
    "guarantee": "Garanție: {{amount}} RON",
    "create": {
      "title": "Postează un anunț",
      "step1": "Informații generale",
      "step2": "Locație",
      "step3": "Specificații",
      "step4": "Poze și preț"
    }
  },
  "rental": {
    "status": {
      "PENDING_PAYMENT": "Așteptare plată",
      "PAID": "Plătit",
      "READY_TO_PICKUP": "Gata de ridicare",
      "ACTIVE": "În curs",
      "RETURNED": "Returnat",
      "CANCELLED": "Anulat",
      "DISPUTED": "În litigiu"
    }
  },
  "kyc": {
    "badge": {
      "verified": "Identitate verificată",
      "pending": "Verificare în așteptare",
      "unverified": "Neverificat"
    }
  },
  "errors": {
    "listing.unavailable": "Produsul nu este disponibil în perioada selectată",
    "payment.failed": "Plata nu a putut fi procesată. Verifică datele cardului.",
    "kyc.required": "Trebuie să îți verifici identitatea înainte de a continua"
  }
}
```

```json
// en.json — fragment (aceleași chei, valori în engleză)
{
  "common": {
    "save": "Save",
    "cancel": "Cancel",
    "delete": "Delete",
    "edit": "Edit",
    "back": "Back",
    "next": "Continue",
    "loading": "Loading...",
    "error": "An error occurred",
    "success": "Success",
    "confirm": "Confirm",
    "search": "Search",
    "filter": "Filter",
    "close": "Close",
    "yes": "Yes",
    "no": "No"
  },
  "nav": {
    "home": "Home",
    "dashboard": "My Account",
    "post": "Post listing",
    "chat": "Messages",
    "wishlist": "Favorites"
  },
  "auth": {
    "login": {
      "title": "Welcome back",
      "subtitle": "Sign in to your account",
      "email": "Email address",
      "password": "Password",
      "submit": "Sign in",
      "forgot": "Forgot your password?",
      "noAccount": "Don't have an account? Sign up"
    },
    "register": {
      "title": "Create your account",
      "firstName": "First name",
      "lastName": "Last name",
      "email": "Email address",
      "phone": "Phone number",
      "password": "Password",
      "confirmPassword": "Confirm password",
      "submit": "Create account",
      "hasAccount": "Already have an account? Sign in",
      "terms": "By registering you agree to our Terms and Conditions"
    }
  },
  "listing": {
    "rent": "Rent",
    "perDay": "/ day",
    "distance": "{{km}} km away",
    "verified": "Verified owner",
    "available": "Available",
    "unavailable": "Unavailable for selected period",
    "guarantee": "Deposit: {{amount}} RON",
    "create": {
      "title": "Post a listing",
      "step1": "General information",
      "step2": "Location",
      "step3": "Specifications",
      "step4": "Photos & pricing"
    }
  },
  "rental": {
    "status": {
      "PENDING_PAYMENT": "Awaiting payment",
      "PAID": "Paid",
      "READY_TO_PICKUP": "Ready for pickup",
      "ACTIVE": "Active",
      "RETURNED": "Returned",
      "CANCELLED": "Cancelled",
      "DISPUTED": "Disputed"
    }
  },
  "kyc": {
    "badge": {
      "verified": "Identity verified",
      "pending": "Verification pending",
      "unverified": "Unverified"
    }
  },
  "errors": {
    "listing.unavailable": "The item is not available for the selected period",
    "payment.failed": "Payment could not be processed. Please check your card details.",
    "kyc.required": "You need to verify your identity before continuing"
  }
}
```

---

## Dark Mode

### Implementare

Dark mode este implementat exclusiv prin **CSS custom properties** pe elementul `<html>` — fără librării externe, fără duplicare de clase.

```css
/* styles.css — global */
:root {
  /* Light mode (implicit) */
  --color-bg-primary:      #ffffff;
  --color-bg-secondary:    #f5f5f7;
  --color-bg-tertiary:     #e8e8ed;
  --color-bg-elevated:     #ffffff;
  --color-text-primary:    #1d1d1f;
  --color-text-secondary:  #6e6e73;
  --color-text-tertiary:   #aeaeb2;
  --color-accent:          #4f46e5;   /* indigo */
  --color-accent-hover:    #4338ca;
  --color-success:         #34c759;
  --color-warning:         #ff9f0a;
  --color-danger:          #ff3b30;
  --color-border:          rgba(0, 0, 0, 0.1);
  --color-border-strong:   rgba(0, 0, 0, 0.2);
  --color-shadow:          rgba(0, 0, 0, 0.08);
  --color-overlay:         rgba(0, 0, 0, 0.4);
  --color-card-bg:         #ffffff;
  --color-input-bg:        #f5f5f7;
  --color-input-border:    rgba(0, 0, 0, 0.15);

  /* Tranziție globală — schimbare monocromatică fluidă */
  --transition-theme: background-color 200ms ease, color 200ms ease,
                      border-color 200ms ease, box-shadow 200ms ease;
}

html[data-theme="dark"] {
  --color-bg-primary:      #000000;
  --color-bg-secondary:    #1c1c1e;
  --color-bg-tertiary:     #2c2c2e;
  --color-bg-elevated:     #1c1c1e;
  --color-text-primary:    #f5f5f7;
  --color-text-secondary:  #98989d;
  --color-text-tertiary:   #636366;
  --color-accent:          #6366f1;
  --color-accent-hover:    #818cf8;
  --color-success:         #30d158;
  --color-warning:         #ffd60a;
  --color-danger:          #ff453a;
  --color-border:          rgba(255, 255, 255, 0.1);
  --color-border-strong:   rgba(255, 255, 255, 0.2);
  --color-shadow:          rgba(0, 0, 0, 0.4);
  --color-overlay:         rgba(0, 0, 0, 0.6);
  --color-card-bg:         #1c1c1e;
  --color-input-bg:        #2c2c2e;
  --color-input-border:    rgba(255, 255, 255, 0.12);
}

/* Toate elementele moștenesc tranziția */
*, *::before, *::after {
  transition: var(--transition-theme);
}
```

### ThemeService

```typescript
// core/services/theme.service.ts
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'rentit_theme';
  private readonly darkMode$ = new BehaviorSubject<boolean>(false);

  constructor() {
    // 1. Verifică localStorage
    // 2. Fallback: detectează preferința sistemului (prefers-color-scheme: dark)
    // 3. Aplică tema la init
    const saved = localStorage.getItem(this.STORAGE_KEY);
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const isDark = saved ? saved === 'dark' : prefersDark;
    this.apply(isDark);
  }

  toggle(): void {
    this.apply(!this.darkMode$.value);
  }

  isDark$(): Observable<boolean> {
    return this.darkMode$.asObservable();
  }

  private apply(dark: boolean): void {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    localStorage.setItem(this.STORAGE_KEY, dark ? 'dark' : 'light');
    this.darkMode$.next(dark);
  }
}
```

### Toggle în UI

**Navbar desktop** — buton icon-only lângă switch-ul de limbă:
- Lună (`ti-moon`) în light mode → click → soare (`ti-sun`) în dark mode
- Animație rotație 180° la switch
- Tooltip: „Mod întunecat" / „Dark mode"

**Mobile** — același buton în header sau în meniul de profil

**Reguli CSS pentru dark mode**:
- **Niciun `#hex` hardcodat** în componente — totul prin `var(--color-*)` definite mai sus
- Imaginile de produs nu se modifică (sunt fotografii reale)
- Harta OpenLayers: folosește tile layer întunecat când dark mode e activ (ex: `https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png`)
- Graficele din rapoarte: culori adaptate automat (fundal transparent, axe și text din variabile CSS)
- Codul QR: background alb forțat indiferent de temă (altfel QR-ul devine ilizibil pentru scaner)
- Skeleton loaders: culori adaptate (`#2c2c2e` → `#3a3a3c` în dark, `#e8e8ed` → `#f5f5f7` în light)

### Persistare teme per utilizator autentificat

Câmp `preferredTheme` (`LIGHT` / `DARK` / `SYSTEM`) pe entitatea `User`, sincronizat la login/logout:
- La login: se încarcă tema din server, se aplică
- La logout: se păstrează ultima temă din localStorage (nu se resetează)
- La schimbare temă când e autentificat: se face PATCH la `/api/users/me/preferences` în background (fire-and-forget, fără să blocheze UI)

---

## Baza de date — note importante

### Câmpuri dinamice per categorie

Folosește abordarea **JSON column** în Entity `Listing`:

```java
@Column(columnDefinition = "JSON")
private String categoryAttributes; // stocat ca JSON, deserializat în service
```

Sau alternativ tabel `listing_attribute (id, listing_id, attribute_key, attribute_value, attribute_type)`.

**Recomandare**: JSON column — mai simplu, suficient pentru acest caz de utilizare.

### Disponibilitate anunțuri

Tabel `rental` cu `start_date` și `end_date`. La căutare:

```sql
WHERE l.id NOT IN (
  SELECT r.listing_id FROM rental r
  WHERE r.status NOT IN ('CANCELLED', 'RETURNED')
  AND r.start_date < :endDate
  AND r.end_date > :startDate
)
```

### Geolocation

Stochează în `listing`: `latitude DOUBLE`, `longitude DOUBLE`, `address VARCHAR`, `city VARCHAR`, `county VARCHAR`.

Distanța în SQL (Haversine aproximat) sau calculată în Java:

```java
// GeoService.java
public double haversineDistance(double lat1, double lon1, double lat2, double lon2)
```

---

## Variabile de mediu (application.yml / .env)

```yaml
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/rentit
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}

# JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=900000        # 15 minute
app.jwt.refresh-expiration=2592000000  # 30 zile

# Stripe
stripe.api-key=${STRIPE_SECRET_KEY}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET}
stripe.platform-fee-percent=5

# Storage (MinIO sau AWS S3)
storage.endpoint=${STORAGE_ENDPOINT}
storage.access-key=${STORAGE_ACCESS_KEY}
storage.secret-key=${STORAGE_SECRET_KEY}
storage.bucket=rentit-files

# Email (SMTP — fallback dacă nu există config salvată în DB de admin)
spring.mail.host=${MAIL_HOST:smtp.zoho.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USER:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
# Configurația reală se gestionează din Admin → Setări → Email SMTP
# SmtpConfigService suprascrie aceste valori la runtime din tabelul smtp_config

# App
app.frontend-url=http://localhost:4200
app.admin.email=admin@singularity-cloud.com
app.admin.password=rgbiuli1
```

---

## Ordine de implementare recomandată

1. **Setup proiect**: Spring Boot + Angular, structură foldere, DB schema, Flyway migrații
2. **Auth**: register, login, JWT, refresh token
3. **i18n + Dark mode**: setup `@ngx-translate`, fișiere `ro.json` / `en.json`, `ThemeService`, CSS custom properties, toggle-uri în navbar — **se face de la început**, nu la final
4. **GDPR + KYC**: wizard frontend, upload documente, status verificare
4. **SMTP Configurator**: entitate `SmtpConfig`, `SmtpConfigService` cu reinit dinamic, UI admin
5. **Email templates**: Thymeleaf layout + toate cele 5 template-uri tranzacționale
6. **Categories + Listings**: CRUD complet, câmpuri dinamice per categorie, upload imagini
7. **Geo + Search**: integrare OpenLayers + Nominatim, filtre, căutare full-text
8. **Stripe Connect**: onboarding proprietari, payment intent, split automat
9. **Rentals**: flux complet (rezervare → plată → pickup → returnare → QR) cu trimitere emailuri la fiecare pas
10. **Garanție**: hold fonduri, eliberare la scanare QR
11. **Dashboard user**: istoric, rapoarte financiare, wishlist
12. **Chat**: WebSocket STOMP, upload fișiere, notificări
13. **Admin dashboard**: gestionare useri, rapoarte, support chat, SMTP configurator
14. **Polish UI**: animații, responsive, skeleton loaders, dark mode opțional

---

## Reguli absolute — Claude Code trebuie să le respecte întotdeauna

1. **Niciun `TODO` neimplementat** în codul livrat — dacă nu e gata, nu e scris
2. **Niciun secret hardcodat** — totul prin variabile de mediu
3. **Validare pe ambele straturi**: `@Valid` pe DTO în Controller + validare business în Service
4. **Erori HTTP corecte**: 400 bad request, 401 unauthorized, 403 forbidden, 404 not found, 409 conflict, 422 unprocessable
5. **Paginare obligatorie** pe orice endpoint care returnează liste (`Pageable`)
6. **Logging structurat**: `@Slf4j`, nivel `INFO` pentru acțiuni business, `ERROR` pentru excepții cu stack trace
7. **Tranzacții explicite**: `@Transactional` pe metode Service care modifică date
8. **Security by default**: niciun endpoint public dacă nu este explicit declarat în `SecurityConfig`
9. **Imagini**: redimensionate și comprimate la upload (max 2MB per imagine stocată)
10. **Cod consistent**: același stil în toate fișierele — dacă primul Controller e scris într-un fel, toate la fel
11. **Zero text hardcodat în template-uri**: orice string vizibil utilizatorului trece obligatoriu prin `translate` pipe — niciodată text direct în HTML
12. **Zero culori hardcodate în componente**: orice culoare folosește `var(--color-*)` din sistemul de design — niciodată `#hex` sau `rgb()` în CSS-ul componentelor
