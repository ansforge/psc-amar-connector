/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.asynclistener.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Fournit le certificat client mTLS pour les appels sortants vers l'API AMAR d'INGROUP.
 *
 * <p>Le certificat client et sa clé privée sont fournis au format PEM (même convention que
 * pscload dans Vault : la clé est une clé PKCS#8 non chiffrée). Le chargement se fait en pur
 * JDK (pas de BouncyCastle) : {@link CertificateFactory} pour la chaîne de certificats et
 * {@link PKCS8EncodedKeySpec} pour la clé.
 *
 * <p>Le serveur AMAR étant sur une AC publique (truststore JDK par défaut), on ne configure
 * aucun {@code TrustManager} custom. Si aucun certificat n'est configuré (dev local),
 * {@link #createRequestFactory()} renvoie {@link Optional#empty()} et les appels restent en
 * TLS simple, sans mTLS.
 */
@Slf4j
@Component
public class AmarMtlsSslContextFactory {

	/** Alias et mot de passe du KeyStore PKCS12 reconstruit en mémoire (jamais persisté). */
	private static final String KEY_ALIAS = "amar";
	private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

	private static final int CONNECT_TIMEOUT_MS = 5_000;
	private static final int READ_TIMEOUT_MS = 10_000;

	@Value("${amar.cert.path:}")
	private String certPath;

	@Value("${amar.key.path:}")
	private String keyPath;

	/**
	 * Construit une {@link ClientHttpRequestFactory} présentant le certificat client mTLS.
	 *
	 * @return la factory mTLS si un certificat client est configuré et présent, sinon vide.
	 */
	public Optional<ClientHttpRequestFactory> createRequestFactory() {
		return buildSslContext().map(sslContext -> {
			MtlsClientHttpRequestFactory factory = new MtlsClientHttpRequestFactory(sslContext);
			factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
			factory.setReadTimeout(READ_TIMEOUT_MS);
			return factory;
		});
	}

	/**
	 * Charge le certificat client et sa clé dans un {@link SSLContext}.
	 *
	 * @return le contexte SSL, ou vide si aucun certificat n'est configuré / présent.
	 */
	Optional<SSLContext> buildSslContext() {
		if (!StringUtils.hasText(certPath) || !StringUtils.hasText(keyPath)) {
			log.info("mTLS AMAR désactivé : amar.cert.path / amar.key.path non configurés.");
			return Optional.empty();
		}
		File certFile = new File(certPath);
		File keyFile = new File(keyPath);
		if (!certFile.exists() || !keyFile.exists()) {
			log.warn("mTLS AMAR désactivé : certificat ou clé introuvable (cert={}, key={}).", certPath, keyPath);
			return Optional.empty();
		}
		try {
			Certificate[] chain = loadCertificateChain(certFile);
			PrivateKey privateKey = loadPrivateKey(keyFile);

			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(null, null);
			keyStore.setKeyEntry(KEY_ALIAS, privateKey, KEYSTORE_PASSWORD, chain);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, KEYSTORE_PASSWORD);

			SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

			log.info("mTLS AMAR activé : certificat client chargé depuis {} ({} certificat(s) dans la chaîne).",
					certPath, chain.length);
			return Optional.of(sslContext);
		} catch (GeneralSecurityException | IOException e) {
			throw new IllegalStateException("Impossible de charger le certificat client mTLS AMAR (cert=" + certPath
					+ ", key=" + keyPath + ")", e);
		}
	}

	private Certificate[] loadCertificateChain(File certFile) throws IOException, CertificateException {
		try (InputStream in = new FileInputStream(certFile)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Collection<? extends Certificate> certs = cf.generateCertificates(in);
			if (certs.isEmpty()) {
				throw new CertificateException("Aucun certificat X.509 trouvé dans " + certFile);
			}
			return certs.toArray(new Certificate[0]);
		}
	}

	private PrivateKey loadPrivateKey(File keyFile) throws IOException, GeneralSecurityException {
		String pem = new String(Files.readAllBytes(keyFile.toPath()), StandardCharsets.UTF_8);
		String base64 = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		byte[] der = Base64.getDecoder().decode(base64);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
		return KeyFactory.getInstance("RSA").generatePrivate(spec);
	}

	/**
	 * {@link SimpleClientHttpRequestFactory} qui applique le {@link SSLContext} mTLS à chaque
	 * connexion HTTPS. Le RestTemplate du client AMAR généré s'appuie sur
	 * {@code HttpsURLConnection} ; on y pose donc le {@code SSLSocketFactory} porteur du
	 * certificat client.
	 */
	static class MtlsClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

		private final SSLContext sslContext;

		MtlsClientHttpRequestFactory(SSLContext sslContext) {
			this.sslContext = sslContext;
		}

		@Override
		protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
			if (connection instanceof HttpsURLConnection) {
				((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
			}
			super.prepareConnection(connection, httpMethod);
		}
	}
}
