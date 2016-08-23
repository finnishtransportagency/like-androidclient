# Liikenneviraston LIKE-järjestelmä #

Tämä projekti on LIKE-hankkeen tietoja keräävä Android-sovellus. Tämä Android-sovellus kerää paikka- ja kulkutapatietoja, sekä lähettää niitä tallennettavaksi backendille.

## Paikkatieto ##

Sijaintien keräämiseksi käytetään laitteen GPS-sirua ja käyttöjärjestelmän tarjoamaa paikannuspalvelua.

## Kulkutapatieto ##

Kulkutapojen määrittämiseksi käytetään laitteen päässä pääasiassa Google Play Services-kirjaston mukana tulevia palveluja.

## Konfiguraatio ##

Sovelluksen konfiguraatio ja https-liikennettä varten tarvittava sertifikaatti pitää räätälälöidä tapauskohtaisesti sopivaksi. Konfiguraatiotiedosto löytyy hakemistosta application/app/src/main/java/fi/livi/like/client/android/Configuration.java. Luomasi sertifikaatin voit polkuineen lisätä konfiguraatiotiedostoon.