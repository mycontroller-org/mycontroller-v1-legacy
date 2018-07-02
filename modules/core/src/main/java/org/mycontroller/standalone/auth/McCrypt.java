/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.auth;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class McCrypt {
    private static final String ALGORITHM = "Blowfish";
    private static final byte[] KEY = "myc crypt way".getBytes();

    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey());
            byte[] encryptedByteValue = cipher.doFinal(value.getBytes("UTF-8"));
            String encryptedValue64 = Base64.encodeBase64String(encryptedByteValue);
            return encryptedValue64;
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            return null;
        }

    }

    public static String decrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, generateKey());
            byte[] decryptedValue64 = Base64.decodeBase64(value);
            byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
            String decryptedValue = new String(decryptedByteValue, "UTF-8");
            return decryptedValue;
        } catch (IllegalBlockSizeException ex) {
            _logger.warn("Exception: '{}'", ex.getMessage());
            _logger.debug("Exception, ", ex);
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        return key;
    }

    public static void main(String[] args) {
        System.out.println("Enc" + encrypt("admin"));
    }
}
