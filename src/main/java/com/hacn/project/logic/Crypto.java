package com.hacn.project.logic;

/**
 * Created by familia on 10/04/2016.
 */
public interface Crypto {


    byte[] encrypt(byte[] bytes) throws Exception;

    byte[] decrypt(byte[] message) throws Exception;


}
