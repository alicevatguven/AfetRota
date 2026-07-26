[English](#english) | [Türkçe](#türkçe)

# 📍 AfetRota - Çevrim Dışı Acil Durum Navigasyonu

**AfetRota**, doğal afet ve acil durumlarda hücresel ağların çöktüğü veya internet erişiminin bulunmadığı senaryolarda hayat kurtarıcı rotalandırma sağlayan **çevrim dışı (offline)** bir Android navigasyon uygulamasıdır. 

---

## 🚀 Öne Çıkan Özellikler

- **100% Çevrim Dışı Çalışma:** İnternet bağlantısı veya sunucu bağımlılığı olmadan tam işlevsel harita ve navigasyon.
- **Dinamik Rotalama:** Harita verileri üzerinden anlık ve güvenli alternatif güzergah hesaplama.
- **Kritik Nokta İşaretleme:** İnternet yokken bile güvenli toplanma alanlarını ve acil servis noktalarını görüntüleme.
- **Düşük Kaynak Kullanımı:** Mobil cihazların pil ve bellek sınırları göz önüne alınarak optimize edilmiş mimari.

---

## 🛠️ Kullanılan Teknolojiler ve Mimari

Bu proje, yüksek performanslı harita işleme ve çevrim dışı vektör çizimleri için aşağıdaki açık kaynak teknolojiler kullanılarak geliştirilmiştir:

| Bileşen | Teknoloji / Kütüphane | Açıklama |
| :--- | :--- | :--- |
| **Platform** | Native Android (Kotlin) | Yüksek performanslı mobil uygulama geliştirme |
| **Harita Motoru** | [Mapsforge](https://github.com/mapsforge/mapsforge) | Çevrim dışı harita işleme ve OpenStreetMap (OSM) vektör çizimi |
| **Rotalama Motoru** | [GraphHopper](https://github.com/graphhopper/graphhopper) | Cihaz üzerinde (on-device) hızlı yol tarifi ve rota hesaplama |
| **Harita Verisi** | [OpenStreetMap](https://www.openstreetmap.org/) | Açık kaynaklı, özelleştirilebilir coğrafi veri altyapısı |

---

## 🏗️ Proje Mimarisi & Çalışma Mantığı

AfetRota, internet bağlantısı olmadan harita render etme ve rota hesaplama süreçlerini şu şekilde yürütür:

1. **Çevrim Dışı Harita (.map):** OpenStreetMap verileri `Mapsforge` formatında cihazın yerel depolamasına taranır ve ekrana çizdirilir.
2. **Graf Tabanlı Rotalama:** `GraphHopper` önceden işlenmiş yol ağ grafiklerini yerel hafızadan okuyarak milisaniyeler içinde A noktası ile B noktası arasındaki en kısa/güvenli rotayı çıkarır.

## English

# 📍 AfetRota - Offline Emergency Navigation / Çevrim Dışı Acil Durum Navigasyonu

### 📑 Overview
**AfetRota** is an offline Android navigation application designed to provide life-saving routing during natural disasters and emergency situations where cellular networks are down or internet access is unavailable.

### 🚀 Key Features
- **100% Offline Capability:** Fully functional map rendering and navigation without requiring internet connection or server dependency.
- **On-Device Routing:** Real-time and secure alternative route calculation computed locally on the device.
- **Critical Points of Interest:** Display safe assembly areas and emergency service locations without cellular connectivity.
- **Optimized Performance:** Engineered with low resource consumption to preserve mobile device battery and memory in emergency conditions.

### 🛠️ Tech Stack & Architecture

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Platform** | Native Android (Kotlin) | High-performance mobile application development |
| **Map Engine** | [Mapsforge](https://github.com/mapsforge/mapsforge) | Offline map rendering using OpenStreetMap (OSM) vector data |
| **Routing Engine** | [GraphHopper](https://github.com/graphhopper/graphhopper) | Fast on-device route calculation and turn-by-turn navigation |
| **Data Source** | [OpenStreetMap](https://www.openstreetmap.org/) | Open-source geographic data infrastructure |

### 🏗️ How It Works
1. **Offline Map Processing (.map):** Pre-processed OpenStreetMap vector files are rendered locally via `Mapsforge`.
2. **Graph-Based Routing:** `GraphHopper` evaluates local road network graphs to calculate the shortest and safest path between two coordinates in milliseconds.
