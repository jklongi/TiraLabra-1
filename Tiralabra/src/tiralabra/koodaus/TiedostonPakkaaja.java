/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tiralabra.koodaus;

import tiralabra.tiedostonkasittely.TiedostonKirjoittaja;
import tiralabra.tiedostonkasittely.TiedostonLukija;
import tiralabra.tietorakenteet.Jono;
import tiralabra.tietorakenteet.Node;
import tiralabra.tietorakenteet.PrioriteettiJono;
import tiralabra.tietorakenteet.Puu;




/**
 * Pakkaaja hoitaa tiedoston pakkaamisen
 * @author joonaslongi
 */

public class TiedostonPakkaaja {
    
    private TiedostonKirjoittaja kirjoittaja;
    private TiedostonLukija lukija;
    private Puu puu;
    private PrioriteettiJono jono;
    private ToistojenLaskija laskija;
    private String tiedosto;
    private String[] reitit;
    private int[] toistot;
    private Jono bittiJono;
    
    /**
     * Alustaa kirjoittajan, prioriteettijonon, laskijan, lukijan ja jonon.
     * @param uusiTiedosto uusi nimi pakatulle tiedostolle
     * @param tiedosto pakattava tiedosto
     */
    
    public TiedostonPakkaaja(String uusiTiedosto, String tiedosto){
        this.kirjoittaja = new TiedostonKirjoittaja(uusiTiedosto);
        this.jono = new PrioriteettiJono();
        this.laskija = new ToistojenLaskija();
        this.tiedosto = tiedosto;
        this.lukija = new TiedostonLukija(tiedosto);
        this.bittiJono = new Jono();
        
    }
    
    /**
     * Kutsuu pakkaukseen vaadittavia metodeita.
     */
    
    public void pakkaa(){
        muodostaJono();
        muodostaPuuJaReitit();
        kirjoitaAlkutiedot();
        kirjoitaTiedosto();
    }
    
    /**
     * Kirjoittaa pakattavan tiedoston alkuun purkua varten 
     * vaadittavat tiedot. Ensin tekstissä esiintyvien eri kirjainten
     * määrä, sitten kaikkien kirjainten määrä yhteendä ja viimeisenä
     * muodostetaan jokaiselle esiintyneelle merkille "sanakirja", eli
     * kirjoitetaan kirjain ja tämän jälkeen sen reitti puussa. Kaikki
     * nämä on eroteltu kauttaviivalla '/' jotta lukiessa tiedetään mihin asti
     * luetaan.
     */
    
    public void kirjoitaAlkutiedot(){
        kirjoitaTekstia(""+laskeEriKirjaimet());
        kirjoittaja.kirjoita('/');
        kirjoitaTekstia("" + laskeKaikkiKirjaimet());
        kirjoittaja.kirjoita('/');
        kirjoitaTekstia(""+ muodostaSanakirja());
    }
    /**
     * käyttää laskijaa ja laskee merkkien toistot tiedostossa ja
     * laittaa ne jonoon puun kokoamista varten.
     */
    
    public void muodostaJono(){
        laskija.laske(tiedosto);
        this.toistot = laskija.getToistot();
        for (int i = 0; i < 256; i ++){
            if(toistot[i] > 0){
                Node node = new Node(i,toistot[i]);
                jono.lisaa(node);
            }
        }
    }
    
    /**
     * Muodostaa luodusta jonosta uuden puun ja kirjainten reitit.
     */
    
    public void muodostaPuuJaReitit(){
        this.puu = new Puu(jono);
        puu.kokoa();
        puu.muodostaReitit(puu.getRoot(), "");
        this.reitit = puu.getReitit();
    }
    
    /**
     * Metodi jolla voi kirjoittaa helposti tekstia tiedostoon.
     * Kirjoittaja ei siis hyväksy Stringejä.
     * @param teksti 
     */
    
    public void kirjoitaTekstia(String teksti){
        for(int i = 0; i < teksti.length(); i++){
            kirjoittaja.kirjoita(teksti.charAt(i));
        }
    }
    
    /**
     * Laskee reitit taulukosta kuinka moni ei ole null, eli kuinka
     * monta eri merkkiä tiedostossa on käytetty.
     * @return summa
     */
    
    public int laskeEriKirjaimet(){
        int summa = 0;
        for(String x: reitit){
            if(x != null){
                summa++;
            }
        }
        return summa;
    }
    
    /**
     * Laskee toistot taulukosta, kuinka monta kirjainta tiedostossa
     * on kaikenkaikkiaan käytetty.
     * @return summa
     */
    
    public int laskeKaikkiKirjaimet(){
        int summa = 0;
        for(int x = 0; x < toistot.length; x++){
            if(toistot[x] > 0){
                summa += toistot[x];
            }
        }
        return summa;
    }
    
    /**
     * Muodostaa pakatun tiedoston alkuun sijoitettavan sanakirjan 
     * käyttäen reitit taulukkoa. Ne jotka eivät ole null, kirjoitetaan
     * ensin itse merkki, sitten reitti ja erotellaan '/'. Lopuksi saadaan
     * yksi iso String joka sisältää kaikki reitit jakirjaimet, joita tiedostossa
     * oli käytetty.
     * @return sanakirja
     */
    
    public String muodostaSanakirja(){
        String sanakirja = "";
        for(int i = 0; i < reitit.length; i++){
            if(reitit[i] != null){
                sanakirja += (char)i;
                sanakirja += reitit[i];
                sanakirja += '/';
            }
           
        } 
        return sanakirja;
    }
    
    /**
     * Hoitaa itse tiedoston pakkaus osuuden kirjoittamisen.
     * Tiedostoon kirjoitetaan jonon avulla kokonaisia tavuja,
     * 8 bittiä kerrallaan ja lopuksi lisätään ylimääräistä
     * roskaa täydentämään viimeinen tavu.
     */
    
    public void kirjoitaTiedosto(){
        while(lukija.vapaana() > 0){
            String koodi = reitit[lukija.lue()];
            for(int i = 0; i < koodi.length(); i ++){
                if(koodi.charAt(i) == '0'){
                    bittiJono.lisaa(false);
                } else {
                    bittiJono.lisaa(true);
                }
            }
            while(bittiJono.getKoko() > 8){
                kirjoittaja.kirjoita(kirjoitaTavu());
            }
        }
        int roskaa = 8 - bittiJono.getKoko();
        kirjoittaja.kirjoita(kirjoitaTavu());
        kirjoitaTekstia("" + roskaa);
    }
    
    
    /**
     * Kirjoittaa 8 bitistä yhden tavun jonon avulla
     * @return 
     */
    
    public int kirjoitaTavu(){
        int tavu = 0;
        boolean bitti = false;
        for(int i = 0 ; i < 8; i ++){
            if(!bittiJono.tyhja()){
                bitti = bittiJono.ota();
            }
            if(bitti){
                tavu +=(1 << (7-i));
            }
        }
        return tavu;
    }
    
    /**
     * Laskija getter.
     * @return laskija
     */
    
    public ToistojenLaskija getLaskija(){
        return this.laskija;
    }
    
    /**
     * Jono getter.
     * @return jono
     */
    
    public PrioriteettiJono getJono(){
        return this.jono;
    }
    
}
