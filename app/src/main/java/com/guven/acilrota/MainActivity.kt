package com.guven.acilrota

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// --- GRAPHHOPPER ---
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.util.Parameters
import com.graphhopper.PathWrapper

// --- MAPSFORGE ---
import org.mapsforge.core.graphics.Style
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.datastore.MapDataStore
import org.mapsforge.map.layer.overlay.Circle
import org.mapsforge.map.layer.overlay.Polyline
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme

import com.google.android.material.bottomsheet.BottomSheetDialog

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

data class AcilNokta(val isim: String, val lat: Double, val lon: Double, val tur: String)

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var mapView: MapView
    private lateinit var btnLocate: Button
    private lateinit var locationManager: LocationManager
    private lateinit var tvRouteInfo: TextView

    // Ses ve Alarm Değişkenleri
    private var mediaPlayer: android.media.MediaPlayer? = null
    private lateinit var audioManager: android.media.AudioManager
    private var isSosPlaying = false
    private var originalVolume = 0

    // Menü Butonları
    private lateinit var btnMenu: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var btnHospital: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    private lateinit var btnPolice: com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    private lateinit var btnIdentify: com.google.android.material.floatingactionbutton.FloatingActionButton

    private var isMenuOpen = false
    private val MAP_FILE_NAME = "dogu.map"
    private val PBF_FILE_NAME = "dogu.pbf"
    private var selectedMode = "car"
    private var isHopperReady = false

    @Volatile
    private var hopper: GraphHopper? = null

    // Konumlar ve Çizimler
    private var startPoint: LatLong? = null
    private var locationCircle: Circle? = null
    private var targetMarker: Circle? = null
    private val activePolylines = ArrayList<Polyline>()

    // Acil Durum Noktaları Listesi
    private val acilNoktalar = listOf(
        // ERZURUM
        AcilNokta("Erzurum Şehir Hastanesi", 39.8872513, 41.2343253, "HASTANE"),
        AcilNokta("Erzurum Atatürk Araştırma Hastanesi", 39.8966735,41.2383416 ,"HASTANE"),
        AcilNokta("Erzurum Emniyet İl Müd.", 39.8993186, 41.2950389, "POLIS MERKEZİ"),
        // ERZİNCAN
        AcilNokta("Erzincan Şehir Hast.", 39.744657, 39.492438, "HASTANE"),
        AcilNokta("Erzincan Emniyet Müd.", 39.747563, 39.4710524, "POLIS MERKEZİ"),
        // BİNGÖL
        AcilNokta("Bingöl Devlet Hastanesi", 38.8935977, 40.5127093, "HASTANE"),
        AcilNokta("Bingöl Emniyet Müd.", 38.8957301, 40.4979663, "POLIS MERKEZİ"),
        // TUNCELİ
        AcilNokta("Tunceli Devlet Hastanesi", 39.0921733, 39.5343488, "HASTANE"),
        AcilNokta("Tunceli Emniyet Müd.", 39.1073466, 39.54709, "POLIS MERKEZİ"),
        // ELAZIĞ
        AcilNokta("Elazığ Fethi Sekin Şehir Hast.", 38.6890214, 39.2714805, "HASTANE"),
        AcilNokta("Fırat Üni. Hastanesi", 38.6809232, 39.2047656, "HASTANE"),
        AcilNokta("Elazığ Emniyet Müd.", 38.6759174, 39.1723371, "POLIS MERKEZİ")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidGraphicFactory.createInstance(this.application)
        setContentView(R.layout.activity_main)

        // View Bağlamaları
        mapView = findViewById(R.id.mapView)
        btnLocate = findViewById(R.id.btnLocate)
        btnMenu = findViewById(R.id.btnMenu)
        btnHospital = findViewById(R.id.btnHospital)
        btnPolice = findViewById(R.id.btnPolice)
        btnIdentify = findViewById(R.id.btnAcil)
        tvRouteInfo = findViewById(R.id.tvRouteInfo)
        mapView.mapScaleBar.isVisible = true
        mapView.setZoomLevelMin(8.toByte())
        mapView.setZoomLevelMax(20.toByte())

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Toast.makeText(this, "Sistem açılıyor...", Toast.LENGTH_SHORT).show()

        Executors.newSingleThreadExecutor().execute {
            val mapFile = checkAndCopyAsset(MAP_FILE_NAME)
            val pbfFile = checkAndCopyAsset(PBF_FILE_NAME)

            runOnUiThread {
                if (mapFile != null) loadMap(mapFile)
                setupMapGestures()
            }
            if (pbfFile != null) loadGraphHopperLegacy(pbfFile)
        }

        // Tıklama Olayları
        btnLocate.setOnClickListener { checkLocationPermissionAndLocate() }

        //Ses yöneticisi
        audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

        findViewById<Button>(R.id.btnEnkaz).setOnClickListener {
            toggleSosAlarm()
        }
        btnMenu.setOnClickListener { toggleMenu() }

        btnHospital.setOnClickListener {
            showLocationList("HASTANE")
            toggleMenu()
        }
        btnPolice.setOnClickListener {
            showLocationList("POLIS MERKEZİ")
            toggleMenu()
        }
        btnIdentify.setOnClickListener {
            showAcilMudahaleSistemi()
        }
    }

    private fun toggleMenu() {
        if (isMenuOpen) {
            btnHospital.visibility = View.GONE
            btnPolice.visibility = View.GONE
            btnMenu.setImageResource(android.R.drawable.ic_menu_search)
        } else {
            btnHospital.visibility = View.VISIBLE
            btnPolice.visibility = View.VISIBLE
            btnMenu.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }
        isMenuOpen = !isMenuOpen
    }

    private fun showLocationList(tur: String) {
        if (startPoint == null) {
            Toast.makeText(this, "Lütfen önce konumunuzu belirleyin!", Toast.LENGTH_LONG).show()
            return
        }

        val mevcutKonum = startPoint!!
        val siraliListe = acilNoktalar
            .filter { it.tur == tur }
            .map { nokta ->
                val mesafe = hesaplaMesafe(mevcutKonum.latitude, mevcutKonum.longitude, nokta.lat, nokta.lon)
                Pair(nokta, mesafe)
            }
            .sortedBy { it.second }

        if (siraliListe.isEmpty()) {
            Toast.makeText(this, "Bulunamadı!", Toast.LENGTH_SHORT).show()
            return
        }

        val listeMetinleri = ArrayList<String>()
        for (item in siraliListe) {
            val nokta = item.first
            val km = item.second * 111.0
            listeMetinleri.add("${nokta.isim} (${String.format("%.1f", km)} km)")
        }

        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_location_list, null)
        val tvTitle = view.findViewById<TextView>(R.id.tvListTitle)
        val listView = view.findViewById<ListView>(R.id.listViewLocations)

        tvTitle.text = if (tur == "HASTANE") "🏥 En Yakın Hastaneler" else "👮 En Yakın Karakollar"
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listeMetinleri)

        listView.setOnItemClickListener { _, _, position, _ ->
            val secilen = siraliListe[position].first
            calcRoute(mevcutKonum.latitude, mevcutKonum.longitude, secilen.lat, secilen.lon)
            updateTargetMarker(LatLong(secilen.lat, secilen.lon))
            Toast.makeText(this, "Hedef: ${secilen.isim}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private var lastTargetLat: Double? = null
    private var lastTargetLon: Double? = null

    private fun updateRouteIfTargetExists() {
        if (startPoint != null && lastTargetLat != null) {
            calcRoute(startPoint!!.latitude, startPoint!!.longitude, lastTargetLat!!, lastTargetLon!!)
        }
    }

    private fun calcRoute(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double) {
        if (!isHopperReady || hopper == null) {
            Toast.makeText(this, "Sistem yükleniyor...", Toast.LENGTH_SHORT).show()
            return
        }

        for (line in activePolylines) { mapView.layerManager.layers.remove(line) }
        activePolylines.clear()

        lastTargetLat = toLat
        lastTargetLon = toLon

        // 1. ROTA: EN KISA
        val reqShort = GHRequest(fromLat, fromLon, toLat, toLon)
            .setVehicle("car")
            .setWeighting("shortest")
        val rspShort = hopper!!.route(reqShort)
        if (!rspShort.hasErrors()) {
            drawPath(rspShort.best, Color.RED, 5f)
        }

        // 2. ROTA: SARSINTIISZ
        val reqSafe = GHRequest(fromLat, fromLon, toLat, toLon)
            .setVehicle("car")
            .setWeighting("curvature")
        reqSafe.hints.put(Parameters.Routing.U_TURN_COSTS, 300)
        reqSafe.hints.put(Parameters.Routing.HEADING_PENALTY, 500)

        val rspSafe = hopper!!.route(reqSafe)
        if (!rspSafe.hasErrors()) {
            drawPath(rspSafe.best, Color.parseColor("#4CAF50"), 8f)
        }

        // 3. ROTA: ANA ROTA
        val reqFast = GHRequest(fromLat, fromLon, toLat, toLon)
            .setVehicle("car")
            .setWeighting("fastest")
            .setAlgorithm(Parameters.Algorithms.ASTAR_BI)

        val rspFast = hopper!!.route(reqFast)
        if (!rspFast.hasErrors()) {
            val path = rspFast.best
            drawPath(path, Color.BLUE, 12f)

            val mesafe = path.distance / 1000
            val sure = path.time / 60000

            tvRouteInfo.text = "En Hızlı: ${sure} dk. | Mesafe: ${String.format("%.1f", mesafe)} km"
            tvRouteInfo.setTextColor(Color.BLUE)
        } else {
            tvRouteInfo.text = "Rota hesaplanamadı!"
            tvRouteInfo.setTextColor(Color.RED)
        }
    }

    private fun drawPath(path: PathWrapper, color: Int, width: Float) {
        val paint = AndroidGraphicFactory.INSTANCE.createPaint()
        paint.color = color
        paint.strokeWidth = width
        paint.setStyle(Style.STROKE)

        val polyline = Polyline(paint, AndroidGraphicFactory.INSTANCE)
        val latLongs = polyline.latLongs
        val points = path.points

        for (i in 0 until points.size()) {
            latLongs.add(LatLong(points.getLatitude(i), points.getLongitude(i)))
        }

        mapView.layerManager.layers.add(polyline)
        activePolylines.add(polyline)
    }

    private fun hesaplaMesafe(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val latDiff = lat1 - lat2
        val lonDiff = lon1 - lon2
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff)
    }

    private fun updateTargetMarker(latLong: LatLong) {
        if (targetMarker != null) mapView.layerManager.layers.remove(targetMarker)
        val paintFill = AndroidGraphicFactory.INSTANCE.createPaint()
        paintFill.color = Color.GREEN
        paintFill.setStyle(Style.FILL)
        val paintStroke = AndroidGraphicFactory.INSTANCE.createPaint()
        paintStroke.color = Color.BLACK
        paintStroke.strokeWidth = 2f
        paintStroke.setStyle(Style.STROKE)
        targetMarker = Circle(latLong, 15f, paintFill, paintStroke)
        mapView.layerManager.layers.add(targetMarker)
    }

    private fun updateLocationCircle(latLong: LatLong, isGps: Boolean) {
        startPoint = latLong
        if (locationCircle != null) mapView.layerManager.layers.remove(locationCircle)
        val color = if (isGps) Color.BLUE else Color.RED
        val paintFill = AndroidGraphicFactory.INSTANCE.createPaint()
        paintFill.color = color
        paintFill.setStyle(Style.FILL)
        val paintStroke = AndroidGraphicFactory.INSTANCE.createPaint()
        paintStroke.color = Color.WHITE
        paintStroke.strokeWidth = 2f
        paintStroke.setStyle(Style.STROKE)
        locationCircle = Circle(latLong, 10f, paintFill, paintStroke)
        mapView.layerManager.layers.add(locationCircle)
        mapView.setCenter(latLong)
        mapView.setZoomLevel(15.toByte())
    }

    private fun setupMapGestures() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val projection = mapView.mapViewProjection
                if (projection != null) {
                    val latLong = projection.fromPixels(e.x.toDouble(), e.y.toDouble())
                    if (latLong != null) {
                        updateLocationCircle(latLong, false)
                        Toast.makeText(applicationContext, "Konum Seçildi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        mapView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event); false }
    }

    private fun checkLocationPermissionAndLocate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLocation()
        }
    }



    private fun getLocation() {
        try {
            Toast.makeText(this, "GPS aranıyor...", Toast.LENGTH_SHORT).show()

            // En son bilinen konumu al
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (lastKnownLocation != null) {
                val latLong = LatLong(lastKnownLocation.latitude, lastKnownLocation.longitude)
                updateLocationCircle(latLong, true)
            }

            // Canlı güncelleme iste
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, this)

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {

        val latLong = LatLong(location.latitude, location.longitude)
        updateLocationCircle(latLong, isGps = true)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) getLocation()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun loadGraphHopperLegacy(pbfFile: File) {
        val graphFolder = File(filesDir, "graph-cache")
        try {
            hopper = GraphHopperOSM().forMobile()
            hopper!!.setDataReaderFile(pbfFile.absolutePath)
            hopper!!.graphHopperLocation = graphFolder.absolutePath
            hopper!!.encodingManager = com.graphhopper.routing.util.EncodingManager.create("car")
            hopper!!.chFactoryDecorator.isEnabled = false
            hopper!!.chFactoryDecorator.setDisablingAllowed(true)
            hopper!!.setMemoryMapped()
            hopper!!.importOrLoad()
            runOnUiThread {
                isHopperReady = true
                Toast.makeText(this, "✅ Yol Verileri Hazır", Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun checkAndCopyAsset(fileName: String): File? {
        val targetFile = File(filesDir, fileName)
        if (targetFile.exists()) return targetFile
        try {
            assets.open(fileName).use { i -> FileOutputStream(targetFile).use { o -> i.copyTo(o) } }
            return targetFile
        } catch (e: Exception) { return null }
    }

    private fun loadMap(mapFile: File) {
        try {
            val mapDataStore: MapDataStore = MapFile(mapFile)
            val tileCache = AndroidUtil.createTileCache(this, "mapcache", mapView.model.displayModel.tileSize, 1f, mapView.model.frameBufferModel.overdrawFactor)
            val tileRendererLayer = TileRendererLayer(tileCache, mapDataStore, mapView.model.mapViewPosition, false, true, false, AndroidGraphicFactory.INSTANCE)
            tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT)
            mapView.layerManager.layers.add(tileRendererLayer)
            mapView.setCenter(LatLong(39.9055, 41.2658))
            mapView.setZoomLevel(10.toByte())
        } catch (e: Exception) { }
    }

    private fun toggleSosAlarm() {
        if (isSosPlaying) {
            // ALARMI DURDUR
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isSosPlaying = false

            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, originalVolume, 0)
            findViewById<Button>(R.id.btnEnkaz).text = "Düdük"
            findViewById<Button>(R.id.btnEnkaz).setBackgroundColor(Color.parseColor("#FF9800"))
            Toast.makeText(this, "Düdük Durduruldu", Toast.LENGTH_SHORT).show()

        } else {
            // ALARMI BAŞLAT
            originalVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)

            // Sesi ZORLA en yüksek seviyeye al
            val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, maxVolume, 0)

            mediaPlayer = android.media.MediaPlayer.create(this, R.raw.duduk)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            isSosPlaying = true

            findViewById<Button>(R.id.btnEnkaz).text = "Düdüğü Sustur"
            findViewById<Button>(R.id.btnEnkaz).setBackgroundColor(Color.RED)
            Toast.makeText(this, "SOS Alarmı Başladı!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    class BilgiAdapter(private val adimlar: List<BilgiAdimi>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BilgiAdapter.ViewHolder>() {

        class ViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val ivGorsel: android.widget.ImageView = view.findViewById(R.id.ivRehberGorsel)
            val tvBaslik: android.widget.TextView = view.findViewById(R.id.tvRehberBaslik)
            val tvAciklama: android.widget.TextView = view.findViewById(R.id.tvRehberAciklama)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bilgi_sayfasi, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val adim = adimlar[position]
            holder.tvBaslik.text = adim.baslik
            holder.tvAciklama.text = adim.aciklama
            holder.ivGorsel.setImageResource(adim.gorselResId)
        }

        override fun getItemCount() = adimlar.size
    }

    private fun showAcilMudahaleSistemi() {
        val secimDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val secimView = layoutInflater.inflate(R.layout.dialog_acil_secim, null)
        val listView = secimView.findViewById<android.widget.ListView>(R.id.lvAcilSecenekler)

        val veriler = getAcilVeriler()
        val isimler = veriler.map { it.isim }

        listView.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, isimler)

        listView.setOnItemClickListener { _, _, position, _ ->
            secimDialog.dismiss()
            showBilgiDeposu(veriler[position].adimlar)
        }

        secimDialog.setContentView(secimView)
        secimDialog.show()
    }

    private fun showBilgiDeposu(adimlar: List<BilgiAdimi>) {
        val rehberDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val containerView = layoutInflater.inflate(R.layout.dialog_rehber_container, null)

        val viewPager = containerView.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPagerRehber)

        viewPager.adapter = BilgiAdapter(adimlar)

        rehberDialog.setContentView(containerView)
        rehberDialog.show()
    }

    private fun getAcilVeriler(): List<AcilDurum> {
        return listOf(
            AcilDurum("İlk Yardım", listOf(
                BilgiAdimi("112'i Arayın", "Sağlık güçlerine haber veriniz.", R.drawable.ic_firstaid01),
                BilgiAdimi("Solunumu Kontrol Et", "Burun ve ağız kısmını dinleyerek veya elinizi ıslatıp nefes alınıyor ya da alınmıyor diye kontrol edin", R.drawable.ic_firstaid02),
                BilgiAdimi("Çeneyi Kaldır ve Nefesi Kontrol Et", "Çeneyi hafifçe kaldırıp açın ve nefesi kontrol edin", R.drawable.ic_firstaid03),
                BilgiAdimi("Kurtarma Nefesi Ver", "Normal bir nefes alın ve ağzınızı hastanın ağzının üzerine tam hava sızmayacak şekilde kapatın. Göğsün kalktığını görene kadar yaklaşık 1 saniye boyunca üfleyin.", R.drawable.ic_firstaid04),
                BilgiAdimi("Kalp Masajı Yap", "Eğer hastanın nefes alışverişi gelmediyse kalp masajı yapmaya başlayın", R.drawable.ic_firstaid05)
            )),
            AcilDurum("Dış Kanama", listOf(
                BilgiAdimi("112'i Arayın", "Sağlık güçlerine haber veriniz.", R.drawable.ic_firstaid01),
                BilgiAdimi("Temiz Bezle Müdahale", "Kanayan yer üzerine temiz bir bezle bastırılır", R.drawable.ic_diskanama01),
                BilgiAdimi("Kanama Durmazsa", "Kanama durmazsa ikinci bir bez koyarak basıncı artırılır", R.drawable.ic_diskanama02),
                BilgiAdimi("Kanama Durmazsa Alternatif", "Gerekirse bandaj ile sararak basınç uygulanır", R.drawable.ic_diskanama03),
                BilgiAdimi("Kanayan bölge yukarı kaldırılır", "Kanayan yere en yakın basınç noktasına baskı uygulanır", R.drawable.ic_diskanama04),
                ))
        )
    }
}