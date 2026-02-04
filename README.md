# InternetSpeedTest-

**A retro internet speed tester for old 2G/GSM feature phones**  
Built with Java ME (J2ME / CLDC / MIDP) – perfect for Nokia S40, Itel, Tecno and other low-end phones from the 2000s–2010s.

![image-3](https://github.com/user-attachments/assets/0b543b11-d18a-4101-bd06-990073c40ea7)


## ✨ Features

- ⚡ **Download** speed test (parallel downloads from public test files)
- 📤 **Upload** speed test (simple POST to httpbin.org)❌
- 🏓 **Ping / Latency** measurement❌
- 🌐 Public **IP address** detection❌
- 📊 Min / Max / Average speeds per test phase
- 📡 Network type estimation (GPRS / EDGE / 3G / 4G-like)
- 💾 Automatic save to text file on TFCard / phone memory (`Result.txt` or timestamped)
- 🖥️ In-app console / log viewer (requests & responses)
- 🎨 Pixel art success (✅) / failure (❌) indicators
- 🔄 Simple loading animation during tests

## 📱 Target devices

- Java ME / MIDP 2.0 phones (CLDC 1.0 or 1.1)
- Itel, Tecno, Nokia Asha, Sagem, Alcatel, etc.
- Especially useful on **2G / GSM / EDGE** networks in 2025–2026

## 🚀 Installation

1. Download the latest release → `.jar` + `.jad` files
2. Transfer to phone via:
   - Bluetooth
   - USB data cable
   - microSD card (put in `/predefgallery/` or root)
   - OTA (if your phone supports WAP download)
3. Open the `.jad` file from the phone → install the MIDlet
4. Launch from menu → **Speed Test**

## 🛠️ Build from source

### Requirements
- Java ME SDK 3.0+ or Sun Wireless Toolkit 2.5+
- NetBeans IDE with Mobility pack (recommended) or command-line WTK
- JSR-75 (FileConnection) support on target device

### Steps
```bash
# 1. Clone the repo
git clone https://github.com/YOUR_USERNAME/j2me-speed-test.git
cd j2me-speed-test

# 2. Open in NetBeans → right-click project → Build
#    or use WTK → open project → Build → Package

# Output:
# → dist/SpeedTest.jar
# → dist/SpeedTest.jad
```

## ⚙️ How it works (technical overview)

- Uses multiple public HTTP test files (~1 MB each) for download
- Simple POST requests for upload (~1 MB random data)
- Basic timing + byte counting for speed calculation
- Integer-only math (no floating point – CLDC 1.0 friendly)
- Tries several file roots: `file:///TFCard/`, `file:///c:/`, `file:///e:/`, etc.
- No external libraries – pure MIDP / CLDC + JSR-75

## 📸 Screenshots

<img width="240" height="320" alt="2026_02_04_19_21_23_039_lcd" src="https://github.com/user-attachments/assets/f43028a3-f076-4165-9cb2-730f56073b33" />
<img width="240" height="320" alt="2026_02_04_19_20_11_137_lcd" src="https://github.com/user-attachments/assets/9fbd762b-7dab-41cf-9e93-9a28284ff038" />
<img width="240" height="320" alt="2026_02_04_19_22_49_025_lcd" src="https://github.com/user-attachments/assets/e46d4e74-1741-4cc7-9757-90fe23be395c" />


| Screen              | Description                          |
|---------------------|--------------------------------------|
| Main canvas         | Start screen + pixel art logo       |
| Testing animation   | Arrows + progress during test       |
| Results             | Download / Upload / Ping / IP       |
| Console             | Log of all HTTP requests & errors   |
| Success / Fail art  | Green checkmark or red X            |

## 🛠️ Known limitations (retro constraints)

- No HTTPS support on very old phones → only HTTP servers used
- Upload may timeout on very slow 2G connections
- File save may fail if no writable root detected
- RAM usage kept low (~1–2 MB peak)

## 🤝 Contributing

Pull requests welcome!  
Especially interested in:

- Better upload servers (free, no auth, HTTP POST)
- More reliable public IP endpoints
- Smaller test files for ultra-slow 2G
- Pixel art / UI improvements

## 📄 License

MIT License  
See the [LICENSE](LICENSE) file for details.

## ❤️ Credits

- Developed by **David** (Abidjan, 2025–2026)
- Big thanks to **Grok by xAI** for code assistance and debugging
- Public test servers: tele2.net, ovh.net, thinkbroadband.com, httpbin.org, ipify.org

---

**Enjoy testing your 2G connection in 2026!** 🚀📱
```

### Conseils rapides pour GitHub

- Mets une vraie capture d'écran en haut (remplace le placeholder)
- Ajoute le badge de licence :
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
  
