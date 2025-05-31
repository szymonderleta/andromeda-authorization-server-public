# Help
## 📚 Reference Documentation

For more details, refer to the following resources:

- [Official Apache Maven Documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.2/maven-plugin/reference/html/)
- [How to Create an OCI Image](https://docs.spring.io/spring-boot/docs/3.2.2/maven-plugin/reference/html/#build-image)

---

## 🔐 Adding a Self-Signed Certificate to Java Truststore

### 1. Download the Certificate

Use the following command to extract the certificate from a running service (e.g., milkyway.local):
```bash
echo | openssl s_client -connect milkyway.local:8555 -servername milkyway.local | \
sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > selfsigned.crt
```

### 2. Create the truststore.jks File

Use the keytool (included with JDK) to import the certificate into a Java truststore:
```bash
keytool -importcert -trustcacerts -file selfsigned.crt -keystore truststore.jks -storepass changeit -alias milkyway
```
Replace:

- selfsigned.crt with your actual certificate filename.

- truststore.jks is the newly created file that will contain the trusted certificate.

### 3. Configure IntelliJ IDEA to Use the Truststore

You can specify the truststore path in the VM options of your project.

1. Open Run > Edit Configurations... in IntelliJ IDEA.

2. Select your run configuration (e.g., cloud-authorization).

3. In the VM options field, add the following:
```bash
-Djavax.net.ssl.trustStore=/full/path/to/truststore.jks
-Djavax.net.ssl.trustStorePassword=changeit
```
    Replace /full/path/to/truststore.jks with the absolute path to your truststore.jks file (e.g., /home/user/truststore.jks).

4. Save and restart the project.

### 4. (Alternative) Add the Certificate Globally to the JDK

If you prefer to make the certificate trusted for all applications using the same JDK:

1. Locate your default cacerts file:

- On Linux, it is usually found in:
        /usr/lib/jvm/java-{version}/lib/security/cacerts

- Or check IntelliJ under File > Project Structure > SDKs to find the JDK path.

2. Import the certificate into cacerts:
```bash
sudo keytool -importcert -keystore /path/to/cacerts -storepass changeit -file selfsigned.crt -alias milkyway
```
    Replace /path/to/cacerts with the actual path to your cacerts file (e.g., /usr/lib/jvm/java-11-openjdk/lib/security/cacerts).

3. Confirm the prompt by typing yes.
