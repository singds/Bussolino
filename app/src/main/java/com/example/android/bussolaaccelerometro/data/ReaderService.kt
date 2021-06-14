package com.example.android.bussolaaccelerometro.data

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.android.bussolaaccelerometro.main.MainActivity
import com.example.android.bussolaaccelerometro.R
import com.example.android.bussolaaccelerometro.main.MyApplication
import java.util.*
import kotlin.math.PI
import kotlin.math.atan2


/**
 * Un service che raccoglie i dati dai sensori e li rende disponibili alle altre componenti
 * dell'app.
 *
 * Il service è sempre attivo quando l'applicazione è in foreground.
 * Se è abilitato il monitoraggio in background il service viene mantenuto attivo alla chiusura
 * dell'applicazione. Il servizio vene messo nello stato foreground alla chiusura dell'activity.
 * Questo comunicare ad andorid l'importanza del servizio evitando così che venga terminato.
 *
 * Services that have been running for a long time (such as 30 minutes or more) may be demoted in
 * importance to allow their process to drop to the cached list.
 * A cached process is one that is not currently needed, so the system is free to kill it.
 * È facile che un processo nella cached list sia terminato quando l'utente apre diverse applicazioni.
 * Per garantire che il mio service possa operare in background per lungo periodo devo metterlo
 * nello stato foreground.
 *
 * Il service stesso funge da target per le callback di notifica dei sensori.
 */
class ReaderService : Service(),
        SensorEventListener
{
    private lateinit var repo:Repository

    private lateinit var sensorManager: SensorManager

    /**
     * L'accellerometro di default del dispositivo.
     */
    private lateinit var sensorAccelerometro: Sensor

    /**
     * Il magnetometro di default del dispositivo.
     */
    private lateinit var sensorMagnetometro: Sensor

    /**
     * Ultima lettura dell'accelerometro.
     */
    private var lastAccel = FloatArray(3)

    /**
     * Ultima lettura del magnetometro.
     */
    private var lastMagne = FloatArray(3)

    /**
     * Peso del filtro sulle componenti grezze del magnetometro.
     * Intervallo ammissibile [0-1].
     * Più basso il valore più pesante il filtro.
     */
    private val pesoFiltroMagne = 0.04f

    /**
     * Peso del filtro sulle componenti grezze dell'accelerometro.
     * Intervallo ammissibile [0-1].
     * Più basso il valore più pesante il filtro.
     */
    private val pesoFiltroAccel = 0.08f

    /**
     * Un Handler del main looper. Usato per mettere in esecuzione ad intervalli regolari il
     * metodo di campionamento.
     */
    private lateinit var handler:Handler

    /**
     * Alla creazione mi predispongo a ricevere i dati dei sensori.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "on create")

        repo = (application as MyApplication).repository

        // L'oggetto Application può restare allocato anche quando nessuna componente dell'applicazione
        // è attiva. Android decide quando fare pulizia ed eliminare l'istanza di Application.
        // Quando l'applicazione viene avviata da 0 potrei trovare il repository allocato durante
        // un avvio precedente dell'app. Quindi faccio pulizia dei dati nel repository.
        repo.clearSampleList()

        sensorManager = getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        sensorAccelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // leggo i dati dei sensori con frequenza 10Hz
        val usSamplingRate = 20 * 1000
        sensorManager.registerListener(this, sensorMagnetometro, usSamplingRate)
        sensorManager.registerListener(this, sensorAccelerometro, usSamplingRate)

        handler = Handler(mainLooper)
        handler.post(sampleTickRunnable)
    }

    /**
     * onStartCommand funge da interfaccia di controllo del servizio da parte dell'activity.
     * Il servizio può essere messo in esecuzione o messo in esecuzione in background.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "service start [flags=${flags}][startId=$startId]")

        intent?.let {
            when(it.action)
            {
                ACTION_RUN_IN_BACKGROUND ->
                {
                    // l'intent è configurato per lanciare l'applicazione come se fosse selezionata
                    // dal launcher:
                    // - se c'è già un'activity in background viene riaperta.
                    // - se nessuna activity è un background viene lanciata una nuova main activity.
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = Intent.ACTION_MAIN
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)

                    // un PendingIntent incapsula un intent che puo essere eseguito da un'altra
                    // applicazione per conto di questa.
                    val pendingIntent = PendingIntent.getActivity(this,0,intent,0)

                    // mi predispongo per l'esecuzione in background
                    val notification = NotificationCompat.Builder(this, MainActivity.ID_NOTIF_CH_MAIN)
                            .setSmallIcon(R.drawable.ic_bussola_notification)
                            .setContentTitle(getString(R.string.monitoraggio_in_corso))
                            .setContentText(getString(R.string.bussolino_ti_sta_monitorando))
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(pendingIntent)
                            .build()
                    // notification id must not be 0
                    startForeground(MainActivity.ID_NOTIF_READING, notification)
                }

                ACTION_START ->
                {
                    // quando l'activity torna in foreground tolgo il servizio esce dallo stato
                    // foreground rimuovendo la notifica.
                    stopForeground(true)
                }
                else -> {
                }
            }
        }

        // se il servizio viene killato dal sistema non ricrearlo fino a quando non viene
        // esplicitamente richiesto da un'altro componente (Context.startService(Intent))
        return START_NOT_STICKY
    }

    /**
     * Runnable eseguito ricorsivamente.
     * Raccoglie i dati dei sensori 2 volte al secondo.
     * Ogni campione è un oggetto che racchiude i dati di ogni sensore e il timestamp del
     * campionamento.
     */
    private val sampleTickRunnable = object :Runnable{
        override fun run() {
            repo.putSensorSampleToList(getLastSample())
            handler.postDelayed(this, MS_FRA_CAMPIONI.toLong())
        }
    }

    /**
     * Alla distruzione disabilito il timer che campiona i sensori ad intervallo regolare e
     * de-registro tutti i listener dei sensori.
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "on destroy")

        handler.removeCallbacks(sampleTickRunnable)
        sensorManager.unregisterListener(this)
    }

    /**
     * Questo servizio non accetta connessioni in bind.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(LOG_TAG, "on task removed")

        stopSelf()
    }

    /**
     * Invocato dal sistema quando sono disponibili nuovi dati per accelerometro o magnetometro.
     * Quando ricevo nuovi dati filtro ogni componente e memorizzo i valori filtrati in campi
     * della classe.
     * - L'accelerometro restituisce una misura in m/s².
     * - Il magnetometro restituisce una misura in uT.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event?.apply {
            if (sensor.type == Sensor.TYPE_ACCELEROMETER) {
                lastAccel[0] = filtroAccel(lastAccel[0], values[0])
                lastAccel[1] = filtroAccel(lastAccel[1], values[1])
                lastAccel[2] = filtroAccel(lastAccel[2], values[2])

                repo.putSensorSampleToCurrent(getLastSample())
            }
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                lastMagne[0] = filtroMagne(lastMagne[0], values[0])
                lastMagne[1] = filtroMagne(lastMagne[1], values[1])
                lastMagne[2] = filtroMagne(lastMagne[2], values[2])
            }
        }
    }

    /**
     * Invocato dal sistema quando cambia l'accuratezza del sensore.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        sensor?.let {
        }
    }

    /**
     * Restituisce un oggetto con i dati più recenti di ogni sensore.
     * L'oggetto contiene le 3 accelerazioni (x,y,z) e l'angolo di rotazione del dispositivo
     * rispetto al nord magnetico.
     *
     * Per dettagli sulla convenzione della misura dell'angolo fra dispositivo e nord magnetico
     * vedere [getGradiNord].
     *
     * @return un oggetto con i dati più recenti di ogni sensore.
     */
    private fun getLastSample(): SensorSample
    {
        val magneX = lastMagne[0]
        val magneY = lastMagne[1]

        return SensorSample(
                getGradiNord(magneX, magneY),
                lastAccel[0],
                lastAccel[1],
                lastAccel[2],
                Date()
        )
    }

    /**
     * Filtra le componenti del magnetometro.
     * Il filtro sul magnetometro è più pesante di quello sull'accelerometro per regioni grafiche.
     * @param oldval valore filtrato precedente
     * @param newval nuovo valore da filtrare
     * @return nuovo valore filtrato
     */
    private fun filtroMagne(oldval: Float?, newval: Float):Float {
        return (oldval ?: 0.0f) * (1f - pesoFiltroMagne) + newval * pesoFiltroMagne
    }

    /**
     * Filtra le componenti dell'accelerometro.
     * @param oldval valore filtrato precedente
     * @param newval nuovo valore da filtrare
     * @return nuovo valore filtrato
     */
    private fun filtroAccel(oldval: Float?, newval: Float):Float {
        return (oldval ?: 0.0f) * (1f - pesoFiltroAccel) + newval * pesoFiltroAccel
    }

    /**
     * In funzione dei valori x,y del magnetometro calcola l'angolo fra il dispositivo e il nord
     * magnetico.
     * L'angolo di rotazione del dispositivo rispetto al nord magnetico è l'angolo formato fra
     * l'asse y del sistema di riferimento del dispositivo e la direzione del nord magnetico.
     * L'angolo assume valori nel range [0°-360°].
     * Quando lo smartphone è posizionato su un tavolo con display rivolto al celo e lato superiore
     * che punta a nord, l'angolo misurato è 0.
     * Da questa posizione:
     * - ruotando il dispositivo in senso orario l'angolo cresce in modo continuo.
     * - ruotando il dispositivo in senso antiorario l'angolo, dopo essere passato per 359°, decresce.
     *
     * La misura dell'angolo del dispositivo rispetto al nord è l'angolo di cui bisogna ruotare il
     * dispositivo in senso antiorario affinchè il suo asse y sia diretto verso il nord magnetico.
     *
     * @param magX la misura del campo magnetico lungo l'asse x del device (in uT).
     * @param magY la misura del campo magnetico lungo l'asse y del device (in uT).
     * @return angolo fra asse y del dispositivo e nord magnetico
     */
    private fun getGradiNord(magX: Float, magY: Float):Float {
        return ((-atan2(magX, magY) * 180f / PI.toFloat()) + 360) % 360
    }

    companion object
    {
        /**
         * Service Action: Mette in esecuzione il servizio. Se il servizio è già in esecuzione
         * foreground viene mantenuto in esecuzione ma rimosso dallo stato foreground.
         */
        const val ACTION_START = "start"

        /**
         * Service Action: Mette il servizio in esecuzione in background assicurando che non venga
         * terminato dal sistema. La sopravvivenza del servizio è garantita dall'ingresso nello
         * stato foreground.
         */
        const val ACTION_RUN_IN_BACKGROUND = "background"

        /**
         * Intervallo di salvataggio dei campioni nello storico.
         */
        const val MS_FRA_CAMPIONI = 500

        const val LOG_TAG = "ReaderService"
    }
}