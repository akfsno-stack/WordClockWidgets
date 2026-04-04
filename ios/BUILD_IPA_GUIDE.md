# Как собрать рабочий IPA файл для WordClockWidgets

## ❌ Почему текущий IPA не работает

В текущем IPA файле была проблема:
- **Название приложения**: показывалось как `$Product_Name` вместо реального имени
- **Отсутствовало скомпилированное приложение**: был только исходный код и данные
- **Невалидный пакет**: сборка происходила неправильно

## ✅ Исправления

Я уже исправил:
1. **Info.plist** файлы - заменены переменные на реальные имена:
   - `WordClockWidgets` для основного приложения
   - `NumberToWords` для текстового конвертера
   - `WordClockWidgetsWidget` для виджета

2. Создал **build_ipa.sh** скрипт для правильной сборки на Mac

## 🔧 Как собрать рабочий IPA файл

### Требования
- **macOS** (любая современная версия)
- **Xcode** (установить из App Store)
- **Проект WordClockWidgets** (ios-port ветка)

### Шаги

#### 1. Установите Xcode (если не установлен)
```bash
# Установить Xcode из App Store
open "macappstore://apps.apple.com/app/xcode/id497799835"

# Или можно установить Command Line Tools
xcode-select --install
```

#### 2. Запустите скрипт сборки

```bash
# Перейдите в папку проекта
cd /path/to/WordClockWidgets/ios

# Дайте права на выполнение скрипту
chmod +x build_ipa.sh

# Запустите сборку
./build_ipa.sh
```

#### 3. Дождитесь завершения
Процесс займет **3-10 минут** (зависит от Mac):
1. Очистка старых файлов
2. Компиляция Swift кода
3. Линковка бинарного файла
4. Создание IPA пакета
5. Проверка целостности

### 4. Установка на iPhone

После успешной сборки вы получите файл:
```
build/WordClockWidgets.ipa
```

**Способы установки:**

#### Способ 1: Xcode (Рекомендуется)
```bash
# Откройте Xcode
open -a Xcode

# Menu: Window > Devices and Simulators
# Выберите ваш iPhone
# Перетащите IPA файл на аппарат
```

#### Способ 2: Apple Configurator 2
1. Откройте Apple Configurator 2
2. Подключите iPhone USB кабелем
3. Перетащите IPA файл на изображение iPhone

#### Способ 3: Через Finder (macOS 11+)
1. Откройте Finder
2. Подключите iPhone
3. Найдите iPhone в боковой панели
4. Выберите вкладку "Files"
5. Перетащите IPA файл

#### Способ 4: Alt Server (сложнее, но работает)
1. Установите [AltServer](https://altstore.io/)
2. Запустите alt server
3. Выберите iPhone и установите через AltStore

### 5. Первый запуск на iPhone

После установки:
1. На iPhone откройте: **Settings > General > VPN & Device Management**
2. Найдите Apple ID/Developer
3. Нажмите **Trust**

Теперь приложение готово к использованию!

## 🐛 Если что-то не работает

### Ошибка: "Xcode не найден"
```bash
# Установите Xcode из App Store или скачайте с developer.apple.com
```

### Ошибка: "App bundle not found"
```bash
# Попробуйте запустить:
cd ios
open WordClockWidgets.xcworkspace

# Затем в Xcode: Product > Build
# Проверьте консоль на ошибки
```

### IPA файл не создается
```bash
# Проверьте наличие workspace файла
ls -la WordClockWidgets.xcworkspace

# Если его нет, создайте проект из Package.swift
swift package generate-xcodeproj --output "WordClockWidgets.xcodeproj"
```

### iPhone не распознается на Mac
```bash
# Переподключите USB кабель
# Нажмите "Trust" на iPhone
# Перезагрузите iPhone и Mac
# Проверьте: xcode-select --install
```

## 📊 Что происходит при сборке

1. **Clean** - удаляет старые файлы сборки
2. **Build** - компилирует Swift код в ARM64 бинарный файл
3. **Archive** - создает архив с приложением
4. **Package** - упаковывает в IPA формат (это обычный ZIP)
5. **Verify** - проверяет целостность IPA файла

## 📁 Структура создаваемого IPA

```
WordClockWidgets.ipa
└── Payload/
    └── WordClockWidgets.app/
        ├── info.plist          (конфигурация)
        ├── WordClockWidgets    (скомпилированный бинарный файл)
        ├── NumberToWords       (текстовы конвертер)
        ├── Resources/          (ресурсы)
        └── _CodeSignature/     (подпись)
```

## ✨ Особенности сборки

- **Без подписи**: IPA собирается без код-подписи (работает для установки через Xcode)
- **Release конфигурация**: оптимизирована для скорости
- **ARM64 архитектура**: поддерживает все современные iPhone
- **Проверка целостности**: скрипт проверяет что все файлы на месте

## 🔗 Полезные ссылки

- [Apple Developer](https://developer.apple.com)
- [Xcode Documentation](https://developer.apple.com/documentation/Xcode)
- [iOS Deployment Guide](https://developer.apple.com/library/archive/documentation/IDEs/Conceptual/AppDistributionGuide)
- [Swift.org](https://swift.org)

## 💡 Советы

1. **Используйте WiFi** для первой установки (надежнее чем USB)
2. **Доверяйте разработчику** в настройках iPhone
3. **Обновите Xcode** если видите странные ошибки
4. **Используйте свежий Mac** для сборки (может быть медленно на старых машинах)
5. **Проверьте место на диске** - нужно минимум 5 GB свободного места

## ❓ Часто задаваемые вопросы

**Q: Нужны ли специальные сертификаты для сборки?**
A: Нет для debug сборки. Для App Store нужны сертификаты.

**Q: Почему нельзя собрать на Linux?**
A: iOS приложения требуют macOS и Xcode для компиляции.

**Q: Сколько раз я могу переустанавливать приложение?**
A: Неограниченно, пока вы "доверяете" разработчику на iPhone.

**Q: Как я могу поделиться IPA файлом с другом?**
A: Отправьте IPA файл. Друг сможет установить его через свой Mac с Xcode.

---

**Если у вас остались вопросы, откройте Issue на GitHub с ошибкой от скрипта сборки.**
