/*

 * JBoss.java - Blowfish encryption/decryption tool with JBoss default password

 *    Daniel Martin Gomez <daniel @ ngssoftware.com> - 03/Sep/2009

 *

 * This file may be used under the terms of the GNU General Public License

 * version 2.0 as published by the Free Software Foundation:

 *   http://www.gnu.org/licenses/gpl-2.0.html

 */

import java.math.BigInteger;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class JBossDatabaseCrypto {
	public static void main(String[] args) throws Exception {
		if ((args.length != 2) ||
		!(args[0].equals("-e") | args[0].equals("-d"))) {
			System.out.println(
			"Usage:\n\tjava JBoss <-e|-d> <encrypted_password>");
			return;
		}
		String mode = args[0];
		byte[] kbytes = "jaas is the way".getBytes();
		SecretKeySpec key = new SecretKeySpec(kbytes, "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		String out = null;
		if (mode.equals("-e")) {
			String secret = args[1];
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encoding = cipher.doFinal(secret.getBytes());
			out = new BigInteger(encoding).toString(16);
		} else {
			BigInteger secret = new BigInteger(args[1], 16);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] encoding = cipher.doFinal(secret.toByteArray());
			out = new String(encoding);
		}
		System.out.println(out);
	}
	
	public String encryption(String plaintext) throws Exception {
		byte[] kbytes = "jaas is the way".getBytes();
		SecretKeySpec key = new SecretKeySpec(kbytes, "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] encoding = cipher.doFinal(plaintext.getBytes());
		String ciphertext = new BigInteger(encoding).toString(16);
		return ciphertext;
	}
	
	public String decryption(String ciphertext) throws Exception {
		byte[] kbytes = "jaas is the way".getBytes();
		SecretKeySpec key = new SecretKeySpec(kbytes, "Blowfish");
		Cipher cipher = Cipher.getInstance("Blowfish");
		BigInteger secret = new BigInteger(ciphertext, 16);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] encoding = cipher.doFinal(secret.toByteArray());
		String plaintext = new String(encoding);
		return plaintext;
	}
}
