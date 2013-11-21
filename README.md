# CollaborativePDFAnnotation

Masterarbeit: Implementierung eines Workflows zur collaborativen PDF-Annotation


## Installation
Um das Projekt zu bauen ist maven notwendig.

Bevor es gebaut werden kann ist von OpenNLP der aktuelle Snapshot (Version 1.6.0) notwendig (dieser stellt Konverter zu den Annotationen von brat rapid annotation tool bereit). Der von mir verwendete Build ist als Archiv im Verzeichnis ````/lib/```` zu finden und kann in das lokale Maven-Repository entpackt werden.

Mit dem Befehl ```mvn clean install``` im Root-Verzeichnis des Projektes wird das Web-Archiv ````CollaborativePDFAnnotation-web.war```` gebaut.
Dieses kann in einem Servlet-Container wie Tomcat deployed werden kann.

Die Application läuft dann direkt im root des Containers. 
Das Dispatcher-Servlet selbst behandelt alle Anfragen auf den ````/pages/*```` Ordner.

