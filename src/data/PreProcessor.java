package data;

import java.util.*;
import model.UserRecord;

public class PreProcessor {
    private Map<String, Integer> sehirIndeksi = new HashMap<>();
    
    private double enDusukSkor = Double.MAX_VALUE, enYuksekSkor = -Double.MAX_VALUE, skorAraligi = 1.0;
    
    private double enDusukYas = Double.MAX_VALUE, enYuksekYas = -Double.MAX_VALUE, yasAraligi = 1.0;
    
    private int sehirSayisi = 0;
    private boolean hazirMi = false;

    public void egit(List<UserRecord> egitimVerisi) {
        sehirIndeksi.clear();
        enDusukSkor = Double.MAX_VALUE; enYuksekSkor = -Double.MAX_VALUE;
        enDusukYas = Double.MAX_VALUE; enYuksekYas = -Double.MAX_VALUE;

        for (UserRecord r : egitimVerisi) {
            sehirIndeksi.putIfAbsent(r.getCity(), sehirIndeksi.size());
            
            double skor = r.getSpendingScore();
            enDusukSkor = Math.min(enDusukSkor, skor);
            enYuksekSkor = Math.max(enYuksekSkor, skor);
            
            double yas = r.getAge();
            enDusukYas = Math.min(enDusukYas, yas);
            enYuksekYas = Math.max(enYuksekYas, yas);
        }
        
        skorAraligi = (enYuksekSkor - enDusukSkor == 0) ? 1 : (enYuksekSkor - enDusukSkor);
        yasAraligi = (enYuksekYas - enDusukYas == 0) ? 1 : (enYuksekYas - enDusukYas);
        
        sehirSayisi = sehirIndeksi.size();
        hazirMi = true;
    }

    public double[] donustur(UserRecord kullanici) {
        if (!hazirMi) {
            throw new IllegalStateException("PreProcessor eğitilmedi! Önce egit() çağrılmalı.");
        }

        double cinsiyetDegeri = (kullanici.getGender().equalsIgnoreCase("E") || kullanici.getGender().equalsIgnoreCase("Male")) ? 1.0 : 0.0;
        
        double normSkor = (kullanici.getSpendingScore() - enDusukSkor) / skorAraligi;
        double normYas = (kullanici.getAge() - enDusukYas) / yasAraligi;

        double[] ozellikler = new double[3 + sehirSayisi];
        ozellikler[0] = cinsiyetDegeri;
        ozellikler[1] = normSkor;
        ozellikler[2] = normYas;

        if (sehirIndeksi.containsKey(kullanici.getCity())) {
            ozellikler[3 + sehirIndeksi.get(kullanici.getCity())] = 1.0;
        }
        
        return ozellikler;
    }
}