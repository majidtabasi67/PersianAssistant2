# PersianAssistant (پرشیان)

پروژه آغازین اندروید با رابط فارسی و راست‌به‌چپ. نسخه فعلی یک یادداشت ساده را به‌صورت محلی روی گوشی ذخیره می‌کند.

## ساختار
- `app/`: کد برنامه اندروید
- `gradle/wrapper/`: تنظیمات راه‌انداز Gradle
- `gradlew` و `gradlew.bat`: راه‌انداز پروژه
- `.github/workflows/build.yml`: ساخت خودکار APK در GitHub Actions

## ساخت APK
در GitHub Actions گردش‌کار `Build PersianAssistant APK` را اجرا کنید. خروجی در بخش Artifacts با نام `PersianAssistant-debug-apk` قرار می‌گیرد.

## نیازمندی‌ها
ساخت به JDK 17، اتصال اینترنت برای دریافت Gradle و وابستگی‌های اندروید، و Android SDK (در محیط CI) نیاز دارد.

توجه: فایل `gradle-wrapper.jar` این بسته یک راه‌انداز سبک اختصاصی است که Gradle 8.9 را از نشانی رسمی distribution دریافت و اجرا می‌کند؛ فایل رسمی باینری Wrapper متعلق به توزیع Gradle نیست.
