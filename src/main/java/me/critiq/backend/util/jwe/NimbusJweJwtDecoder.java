package me.critiq.backend.util.jwe;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
//@Component
public class NimbusJweJwtDecoder implements JwtDecoder {
    @Value("${jwt.key.public}")
    private RSAPublicKey publicKey;

    @Value("${jwt.key.private}")
    private RSAPrivateKey privateKey;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            log.info("jwe = {}", token);
            // 1. 解密jwe
            var jweObject = JWEObject.parse(token);
            jweObject.decrypt(new RSADecrypter(privateKey));

            // 2. 获取jwt
            var signedJWT = jweObject.getPayload().toSignedJWT();
            if (signedJWT == null) {
                throw new JwtException("Nested JWT did not contain a signed JWT");
            }

            if (!signedJWT.verify(new RSASSAVerifier(publicKey))) {
                throw new JwtException("Invalid JWT signature");
            }
            log.info("jwt = {}", signedJWT.serialize());

            var claims = signedJWT.getJWTClaimsSet();

            return Jwt.withTokenValue(signedJWT.serialize())
                    .headers(h -> h.putAll(signedJWT.getHeader().toJSONObject()))
                    .claims(c -> c.putAll(claims.toJSONObject()))
                    .build();

        } catch (Exception e) {
            throw new JwtException("Nested JWS+JWE decoding failed", e);
        }
    }
}
