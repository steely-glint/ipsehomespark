/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ipseorama.ipsehomespark.certmangle;

import com.ipseorama.base.certHolders.JksCertMaker;
import com.phono.srtplight.Log;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Westhawk Ltd<thp@westhawk.co.uk>
 */
public class AddFriend {

    static String gate = "MIIC4TCCAckCCCf0QY3raoNaMA0GCSqGSIb3DQEBCwUAMDMxMTAvBgNVBAMMKDhDRjNFOTA0MjExN0YzN0NGRDA3N0E2OUNCNTkzOTYwN0U1ODBFNzQwHhcNMTUxMjA5MTU1MjI4WhcNMTUxMjI3MTYzMjU3WjAzMTEwLwYDVQQDDCg4Q0YzRTkwNDIxMTdGMzdDRkQwNzdBNjlDQjU5Mzk2MDdFNTgwRTc0MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlbRbfyQScZlPwytp/a0gNFJdhIq91kvpCqJ4nShnjAdbXBy7Uqi6Hbm9qXyGVY0XL3ir9PZjQ0J/QY7aZMusPcR6R+utQKlEhYGCC2L7oVkjCCVbdSPY3rmxZbxzJ60gqj3vRPsmQ9mBDD4hfcKVEd00kOvVCRrzuSmVFn2zzWZslVENSJOnSDxCMtsxg5+etaVP6rs/DmdYyfxo20RGjpG//U8cRwP1jIRP/AIsSdGLrlX6wtzsIcIdaW8khfcgj5uYfDCcxCPq3SDILFXz1JgApoPigXWFlGpO3w+tqI1A89LYtd1ugx0B3QkljGclqyvFuzhYxkuE5npQTMnwiQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBNXHYTgm/e2ulOIunRThGCp6We1mVwwOdH+MJ8vNzqYS3y4cJg/XUKQ6Z82k5awNfJbWlOyzIEwuVL/sadyICqG72Leil/PrVbwxPCkotkg01F5G/KVqhP6PVZ2bpHkLISUHBu/UggRFL2mvkbQRYmCjpW+3J7mRXz3rFd0sw9ursjBwlSjnBbOfnD7lZCfBZLEIMTccH09QyPsIOmAhBNBZicOcYup0AuXlZNX0n/0F0S1lBAcVi8EmKY5SDZ6YwB/BEhxenUT4H0wWsmFemLRt8HLVUep0ZzNdBclWKIUVztqpVW+k0w8s8sGJExXcxWEVbGzY7PMWT+JO2i6Cl7";
    static String gatefinger = "36:3D:E3:9A:84:71:E8:FC:A8:4E:1E:C8:16:74:01:8B:8B:C3:EF:76:00:DC:1D:F6:6C:1D:2F:C4:7E:19:74:04";
    static String bone = "MIIC4TCCAckCCGCiiST8dUpyMA0GCSqGSIb3DQEBCwUAMDMxMTAvBgNVBAMMKDQ0NEM5QzY1NzZCOEFEQzMxQjA4RDkwQURDQjY1OEJFMjE3MDk2OUYwHhcNMTUxMjA5MTY1NzEzWhcNMTUxMjI3MTczNzQyWjAzMTEwLwYDVQQDDCg0NDRDOUM2NTc2QjhBREMzMUIwOEQ5MEFEQ0I2NThCRTIxNzA5NjlGMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm3u13NJ6mB4joSSvFZcHaumZsFkzR5u5iB9QiEVxC+ncSBDbMOyPekwzBM9xMNNQv0CxeewIX1TYVcNzacduDCfb1htR6V5dMxZqE6iU6GtMN3NnfKmT31xoYSz2tBQ0mU0R9f6ji2xDewOQkAcPFZd7BRZJTqL7jdkRoQ82eDNSvbQsDWMi9gV6Dho7sd8POxODykCvvvRO7oHhRR+bAJzCkMzQ4slrX9/tYeuWwIzmfkPy0bJ5aXJZuWgPMlYGAvOFV8s7eTPeO4l0msI99z7a7iDJLqtPpR05hLr00lK2qNmr7CuthZCvOUphh9HHphvFGULgAZgKEe1Zn68UaQIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQBEzkn4p/xvJ6agNvL9JBAL9vXwKQpI7/IxHOvjjz5OrhLNkvyM1iGbzEzhDzT30x99W+th9fzojEgtzC1lcqWto/DRWNPRTf00xd6MS26mRna+h+gcfEJiytyJLokVlgpiYIOc0chfNzFkgipIObRz9pv8SW3ww5EzMW+cukyGP62bmAZf4UvsHLN6PjticEer8oT9jZ26qUfgc8H5twO65gZWYHfQOKXVYMkgx5Ru4lLn6oU3iKBcLEwICiXCGXGRsFKrD4+1xN39r8tMPhR2WPqe83+qwjPQMbj4DB4E7/HnropRfk7rBf7iHXssL+SIXS7xxgYAZvVa8Z0DDay7";
    static String bonefinger = "2D:07:88:82:CF:AC:8A:74:D5:6B:15:2A:16:2C:AE:2F:19:6D:2A:AC:7B:C9:03:00:DB:01:80:03:1F:C5:6E:11";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Log.setLevel(Log.DEBUG);
            JksCertMaker s = new JksCertMaker();
            String me = s.getPrint(true);
            if (!me.equalsIgnoreCase(gatefinger)) {
                byte[] fmb = biz.source_code.Base64Coder.decode(gate);
                org.bouncycastle.asn1.x509.Certificate fc = org.bouncycastle.asn1.x509.Certificate.getInstance(fmb);
                s.putFriendCert("gate", fc);
            }
            if (!me.equalsIgnoreCase(bonefinger)) {
                byte[] fmb = biz.source_code.Base64Coder.decode(bone);
                org.bouncycastle.asn1.x509.Certificate fc = org.bouncycastle.asn1.x509.Certificate.getInstance(fmb);
                s.putFriendCert("bone", fc);
            }
            s.listFriends();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
