Questa applicazione è stata realizzata come progetto finale per il corsi di
Elementi di Programmazione di sistemi Embedded dell'Università di Padova, anno
2020/21. Il corso è stato tenuto dal prof. Fantozzi Carlo.

L'applicazione è costituita principalmente da due Fragment e un Service.  

HomeFragment visualizza i dati realtime di accelerometro e magnetometro.  

ChartFragment visualizza i grafici delle accelerazioni e dell'orientamento
rispetto al nord magnetico registrati negli ultimi 5 minuti con frequenza di 2Hz.
Per la visualizzazione dei grafici ho utilizzato la libraria MPAndroidChart.

ReaderService campiona i dati dei sensori quando l'app è attiva o quando si
trova in background.

Maggiori dettagli su ciascuna classe si trovano appena sopra la definizione della
classe stessa.
