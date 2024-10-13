package es.ujaen.dae.sociosclub.util;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class CodificadorMd5 {
    private CodificadorMd5(){
    }

    public static String codificar(@NotNull String texto){
        String textoCodificado = null;

        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(texto.getBytes());
            textoCodificado = Base64.getEncoder().withoutPadding().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e){
        }
        return textoCodificado;
    }
}
