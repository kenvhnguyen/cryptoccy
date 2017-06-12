package com.ing.fx.scrooge_coin;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Crypto {

    /**
     * @return true if {@code signature} is a valid digital signature of {@code message} under the
     *         key {@code pubKey}. Internally, this uses RSA signature, but the student does not
     *         have to deal with any of the implementation details of the specific signature
     *         algorithm
     */
    public static boolean verifySignature(PublicKey pubKey, byte[] message, byte[] signature) {
        /** nek:
         * Digital signature API has the following scheme:
         * - Generates two keys of the signer:
         * (sk, pk) = generateKeys(keysize)
         * - The signer signs her message using the secret signing key sk:
         * signature = sign(secret_key, message)
         * - To verify if a signature is valid, use the public key and the message:
         * isValid = verify(pk, message, signature)
         * The scheme or algorithm must have the unforgeability property!!
         * TRICK: instead of message use the hash of the message!
         * Blockchain: sign the hash-pointer! -> the signature protect the whole chain
         * Bitcoin uses ECDSA: Elliptic Curve Digital Signature Algorithm: very hairy math!
         * It has very good randomness which is essential in avoiding leaking sk
         * either via generating keys or signing keys
         * */
        Signature sig = null;
        try {
            sig = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sig.initVerify(pubKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        try {
            sig.update(message);
            return sig.verify(signature); // Nek: verify if this signature is really the hash of the given message under the initiated key
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;

    }
}
