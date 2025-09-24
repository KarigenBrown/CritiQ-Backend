package me.critiq.backend.util.jwe;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
//@Component
public class NimbusJweJwtEncoder implements JwtEncoder {
    @Value("${jwt.key.public}")
    private RSAPublicKey publicKey;

    @Value("${jwt.key.private}")
    private RSAPrivateKey privateKey;

    @Override
    public Jwt encode(JwtEncoderParameters parameters) throws JwtEncodingException {
        try {
            // 1.封装jwt
            var jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            var claims = parameters.getClaims().getClaims();
            var builder = new JWTClaimsSet.Builder();
            claims.forEach(builder::claim);
            var jwtClaimsSet = builder.build();

            var signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);
            signedJWT.sign(new RSASSASigner(privateKey));
            log.info("jwt = {}", signedJWT.serialize());

            // 2.加密jwt输出jwe
            var jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                    .contentType("JWT")
                    .build();

            var payload = new Payload(signedJWT);
            var jweObject = new JWEObject(jweHeader, payload);
            jweObject.encrypt(new RSAEncrypter(publicKey));

            var token = jweObject.serialize();
            log.info("jwe = {}", token);

            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(jweHeader.toJSONObject()))
                    .claims(c -> c.putAll(jwtClaimsSet.toJSONObject()))
                    .build();

        } catch (Exception e) {
            throw new JwtEncodingException("Nested JWS+JWE Encoding failed", e);
        }
    }
}
