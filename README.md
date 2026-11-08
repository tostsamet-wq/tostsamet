# Counter Flame Android menü dosyaları

Bu klasör, GitHub reposuna yükleyebileceğin başlangıç dosyalarını içerir. İncelenen APK, **ARM64 Unity IL2CPP** derlemesi. Oyun mantığı `lib/arm64-v8a/libil2cpp.so` içinde; DEX dosyaları Android/Unity Java katmanını içeriyor.

## Dosyalar

- `src/main/java/com/ScarletWork/CounterFlame/menu/MenuOverlay.java`: Oyun ekranına yüzen `CF` düğmesi ve menü paneli ekler. Ayarları cihazda `SharedPreferences` içine kaydeder.
- `Assets/Scripts/MenuSettings.cs`: Unity C# kodunun kaydedilen ayarları okuması için yardımcı sınıf.
- `menu-config.json`: Menü başlıkları ve seçenekler. Dosya GitHub’dan JSON veri olarak okunur.

Menüdeki anahtarlar şimdilik arayüz ayarlarını kaydeder. Oyun içi davranışa bağlamak için kendi Unity C# kaynak kodunda `MenuSettings.GetBool(...)` değerlerini ilgili sistemlere bağlayıp oyunu yeniden derlemen gerekir. iOS `.dylib` dosyası Android’de kullanılamaz; DEX dosyaları da IL2CPP oyun mantığını içermez.

## GitHub’a yükleme

`menu-config.json` dosyasını GitHub reposunun köküne koy. Sonra entegrasyon çağrısındaki URL’yi kendi repo adresinle değiştir:

```java
MenuOverlay.attach(this,
    "https://raw.githubusercontent.com/SAHIP/REPO/main/menu-config.json");
```

GitHub’dan yalnızca JSON ayar verisi indirilir. Menü arayüzü APK’ye derlenmiş halde bulunmalıdır; APK GitHub’dan Java kodu indirip çalıştırmaz.

## APK’ye entegrasyon

`MenuOverlay.attach(...)` çağrısı Unity etkinliğinin `onCreate` metodunda `super.onCreate(...)` sonrasında yapılmalı. Uzak JSON ayarları için manifestte `android.permission.INTERNET` izni olmalı.

Bu Java dosyası mevcut `classes.dex` dosyasının yerine doğrudan konamaz. Android SDK ile derlenip uygulamanın DEX’lerine eklenmeli; ardından APK yeniden paketlenip imzalanmalı. Var olan uygulamanın üstüne güncelleme kuracaksan aynı imza anahtarını kullan.
